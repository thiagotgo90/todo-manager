package org.tgo.todomanager.repository;

import org.springframework.data.repository.CrudRepository;
import org.tgo.todomanager.model.Todo;

public interface TodoRepository extends CrudRepository<Todo, Integer> {

}
