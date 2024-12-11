package de.fxhz.test.testcontainers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

/** Endpoint to request and modify tasks*/
@SuppressWarnings("java:S1135")
@RestController
@RequiredArgsConstructor
public class TodoRestApi {

    private final TodoRepository todoRepository;

    /**
     * Returns the task list.
     * @return The task list.
     */
    @GetMapping("")
    public List<String> getList(){
        return StreamSupport.stream(todoRepository.findAll().spliterator(), false)
                .map(TodoEntity::getTask)
                .toList();
    }

    /**
     * Adds a new task to the list. For convenience, this is a GET method,
     * so it can be used by navigating in the browser only.
     * @param todo The task to add.
     */
    @GetMapping("add")
    public ResponseEntity<String> addToList(@RequestParam("todo") String todo) {
        Assert.hasText(todo, "Task needs some text");
        Assert.isTrue(todo.length() <= 1000, "Task exceeds 1000 characters.");
        todoRepository.save(new TodoEntity(null, todo));

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create("/"));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }
}
