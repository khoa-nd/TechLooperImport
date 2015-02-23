package com.techlooper.repository;

import com.techlooper.configuration.VietnamworksDatabaseConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.sql.SQLException;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {VietnamworksDatabaseConfiguration.class})
public class VietnamworksUserRepositoryTest {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testDatabaseConnection() throws SQLException {
        assertNotNull(jdbcTemplate.getDataSource().getConnection());
    }
}