package de.fxhz.test.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This shall demonstrate the usage of testcontainers in combination with mssql.
 * Currently, there is a problem, I am unable to resolve.
 * <p>
 * Establishing a database connection using jdbc is resulting in a hang of the test.
 */
@Slf4j
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class IntegrationTest {


    @Container
    static MSSQLServerContainer<?> mssqlserver = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2019-latest")
            .acceptLicense()
            .waitingFor(Wait.forListeningPort())  // Wait until the port is ready
            .waitingFor(Wait.forLogMessage(".*Started.*", 1));  // Wait for a specific log message (optional)

    private static String jdbcUrl;
    private static String username;
    private static String password;

    @DynamicPropertySource
    static void neo4jProperties(DynamicPropertyRegistry registry) {
        jdbcUrl = mssqlserver.getJdbcUrl();
        username = mssqlserver.getUsername();
        password = mssqlserver.getPassword();

        log.info(jdbcUrl);

        registry.add("spring.datasource.url", () -> jdbcUrl + ";trustServerCertificate=true");
        registry.add("spring.datasource.username", () -> username);
        registry.add("spring.datasource.password", () -> password);
        registry.add("spring.datasource.migrate", () -> "true");
    }

    @Autowired
    private TodoRepository todoRepository;

    private TodoRestApi restApi;

    @Test
    void name() {
        restApi = new TodoRestApi(todoRepository);
        restApi.addToList("Test todo");
        var todos = restApi.getList();
        assertTrue(todos.contains("Test todo"));

        String query = "SELECT id, task FROM todos";

        // Try-with-resources to ensure resources are closed properly
        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            // Process the result set
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String task = resultSet.getString("task");
                System.out.println("ID: " + id + ", Task: " + task);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
