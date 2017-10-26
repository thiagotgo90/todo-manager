package org.tgo.todomanager.controller;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;
import org.tgo.todomanager.model.Todo;
import org.tgo.todomanager.repository.TodoRepository;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

@RestController
@RequestMapping("/todo")
public class TodoController {

	private static final String BUCKET = "todo-attached-files";
	
	private TodoRepository repository;
	
	@Autowired
	public TodoController(TodoRepository repository) {
		this.repository = repository;
	}

	@GetMapping
	public ResponseEntity<List<Todo>> recuperarTodos() {
		
		List<Todo> todos = repository.findAll();
		
		if(todos == null || todos.isEmpty()) {
			return ResponseEntity.notFound().build();
		}
		
		AmazonS3 s3client = AmazonS3ClientBuilder.standard().build();
		
		todos.stream()
			.forEach((todo) -> {
				URL url = s3client.generatePresignedUrl(
						new GeneratePresignedUrlRequest(BUCKET, todo.getAttachment())
						.withExpiration(new Date(new Date().getTime() + (1000 * 60* 60))));
				todo.setAttachment(url.toString());});
		
		return ResponseEntity.ok(todos);
	}
	
	
	@GetMapping(value = {"/{id}"})
	public ResponseEntity<Todo> recuperarPorId(@PathVariable Integer id) {
		
		Todo todo = repository.findById(id);
		
		if (todo == null) {
			return ResponseEntity.notFound().build();
		}
		
		AmazonS3 s3client = AmazonS3ClientBuilder.standard().build();
		
		URL url = s3client.generatePresignedUrl(
				new GeneratePresignedUrlRequest(BUCKET, todo.getAttachment())
				.withExpiration(new Date(new Date().getTime() + (1000 * 60* 60))));
		todo.setAttachment(url.toString());
		
		return ResponseEntity.ok(todo);
	}

	@RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data")
	public ResponseEntity<Object> salvar(Todo todo, @RequestParam(name = "anexo", required = false) MultipartFile anexo)
			throws IOException {

		String attachment = UUID.randomUUID().toString();
		AmazonS3 s3client = AmazonS3ClientBuilder.standard().build();

		ObjectMetadata objectMetadata = new ObjectMetadata();
		objectMetadata.addUserMetadata("OriginalFilename", anexo.getOriginalFilename());
		s3client.putObject(new PutObjectRequest(BUCKET, attachment, anexo.getInputStream(), objectMetadata));

		todo.setAttachment(attachment);
		
		repository.save(todo);
		
		UriComponents uriComponents = 
				ServletUriComponentsBuilder
				.fromCurrentRequest()
				.path("/{id}").buildAndExpand(todo.getId());

		 return ResponseEntity.created(uriComponents.toUri()).build();
	}
}
