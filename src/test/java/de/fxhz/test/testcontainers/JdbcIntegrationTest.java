package de.fxhz.test.testcontainers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.MSSQLServerContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test with a test container and JdbcTest works fine.
 */
@Testcontainers
@JdbcTest
class JdbcIntegrationTest {

    @Container
    @ServiceConnection
    private static MSSQLServerContainer sqlserver = new MSSQLServerContainer(
            "mcr.microsoft.com/mssql/server:2019-latest")
            .acceptLicense();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        String query = "SELECT id, task FROM todos";
        this.jdbcTemplate.update("insert into todos (task) values ('Get this test running')");
        var records = this.jdbcTemplate.queryForList(query);
        assertThat(records).hasSize(1);
    }

}
