package de.fxhz.test.testcontainers;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** Repository to access the todos database table. */
@Repository
public interface TodoRepository extends CrudRepository<TodoEntity, Long> {
}
