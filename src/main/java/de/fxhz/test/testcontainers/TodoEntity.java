package de.fxhz.test.testcontainers;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Database representation for task-entries (Can't use the correct word inhere :D).
 * Also used as DTO.
 */
@Entity
@Table(name="todos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TodoEntity {

    /** Generated Identifier by the database.*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The task entry*/
    @Size(max = 1000)
    private String task;
}
