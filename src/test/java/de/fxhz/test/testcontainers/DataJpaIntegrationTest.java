package de.fxhz.test.testcontainers;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This shall demonstrate the usage of testcontainers in combination with mssql.
 * <p>
 * <strong>Currently, there is a problem, I am unable to resolve.</strong>
 * <p>
 * Establishing a database connection using jdbc is resulting in a hang of the test.
 */
@Slf4j
@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DataJpaIntegrationTest {


    public static final String TEST_TASK = "Test todo";
    public static final String TASK_PROPERTY = "task";
    public static final String ID_PROPERTY = "id";
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
        jdbcUrl = mssqlserver.getJdbcUrl() + ";trustServerCertificate=true;queryTimeout=5;loginTimeout=5";
        username = mssqlserver.getUsername();
        password = mssqlserver.getPassword();

        log.info(jdbcUrl);

        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", () -> username);
        registry.add("spring.datasource.password", () -> password);
        registry.add("spring.datasource.migrate", () -> "true");
    }

    @Autowired
    private TodoRepository todoRepository;

    private TodoRestApi restApi;

    @BeforeEach
    void setUp() {
        restApi = new TodoRestApi(todoRepository);
    }

    @Test
    void testAddAndListWithSqlServer() {

        // Test
        restApi.addToList(TEST_TASK);
        var todos = restApi.getList();

        // Verify
        assertTrue(todos.contains(TEST_TASK));
        verifyValuesInDatabaseWithoutSpringTechnologies();
    }

    private static void verifyValuesInDatabaseWithoutSpringTechnologies() {
        String query = "SELECT id, task FROM todos";

        var tasksInDatabase = new ArrayList<String>();

        try (var connection = DriverManager.getConnection(jdbcUrl, username, password);
             var statement = connection.createStatement();
             var resultSet = statement.executeQuery(query)) {

            tasksInDatabase = extractTasksInDatabaseFromResult(resultSet);

        } catch (SQLException e) {
            logDatabaseAccessFailed(e);
        }
        assertTrue(tasksInDatabase.contains(TEST_TASK));
    }

    private static ArrayList<String> extractTasksInDatabaseFromResult(ResultSet resultSet) throws SQLException {
        var tasksInDatabase = new ArrayList<String>();
        log.info("%s\t%s".formatted(ID_PROPERTY, TASK_PROPERTY));
        while (resultSet.next()) {
            var id = resultSet.getInt(ID_PROPERTY);
            var task = resultSet.getString(TASK_PROPERTY);
            log.info("%d\t%s".formatted(id, task));
            tasksInDatabase.add(task);
        }
        return tasksInDatabase;
    }

    private static void logDatabaseAccessFailed(SQLException e) {
        log.error("Accessing the database with JDBC failed: %s - %s".formatted(e.getClass().getSimpleName(), e.getMessage()), e);
    }
}
