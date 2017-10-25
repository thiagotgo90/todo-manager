package org.tgo.todomanager.controller;

import java.io.IOException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import org.tgo.todomanager.model.Todo;
import org.tgo.todomanager.repository.TodoRepository;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
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
	public Todo recuperar() {
		return new Todo();
	}

	@RequestMapping(method = RequestMethod.POST, consumes = "multipart/form-data")
	public ResponseEntity<Object> salvar(Todo todo, @RequestParam(name = "anexo", required = false) MultipartFile anexo)
			throws IOException {

		String attachment = UUID.randomUUID().toString();
		AmazonS3 s3client = AmazonS3ClientBuilder.standard().build();

		s3client.putObject(
				new PutObjectRequest(BUCKET, attachment, anexo.getInputStream(), new ObjectMetadata()));

		todo.setAttachment(attachment);
		
		repository.save(todo);

		return ResponseEntity.accepted().body(todo);
	}
}
