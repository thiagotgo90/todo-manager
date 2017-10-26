package org.tgo.todomanager.repository;

import java.util.List;

import org.springframework.data.repository.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.tgo.todomanager.model.Todo;

@Transactional(propagation = Propagation.NOT_SUPPORTED)
@org.springframework.stereotype.Repository
public interface TodoRepository extends Repository<Todo, Integer> {

	List<Todo> findAll();

	Todo findById(Integer id);

	@Transactional(propagation = Propagation.REQUIRED)
	Todo save(Todo todo);

}
