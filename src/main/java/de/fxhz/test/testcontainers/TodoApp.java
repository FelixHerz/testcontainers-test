package de.fxhz.test.testcontainers;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Application for testcontainer usage demonstration. */
@SpringBootApplication
public class TodoApp {

    /**
     * Starting point. Fires up the spring context.
     * @param args Args aren't used.
     */
    public static void main(String[] args) {
        SpringApplication.run(TodoApp.class);
    }
}
