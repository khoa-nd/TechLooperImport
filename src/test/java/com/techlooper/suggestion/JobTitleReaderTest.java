package com.techlooper.suggestion;

import com.techlooper.configuration.VietnamworksDatabaseConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {VietnamworksDatabaseConfiguration.class})
public class JobTitleReaderTest {

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private JobTitleReader jobTitleReader;

    @Test
    public void testDatabaseConnection() throws SQLException {
        assertNotNull(jdbcTemplate.getDataSource().getConnection());
    }

    @Test
    public void testReadJobTitle() throws Exception {
        List<String> jobTitles = jobTitleReader.readJobTitle(0, 10);
        assertTrue(jobTitles.size() == 10);
    }

    @Test
    public void testGetTotalNumberOfRegistrationUsers() throws Exception {
        int totalUsers = jobTitleReader.getTotalNumberOfRegistrationUsers();
        assertTrue(totalUsers > 0);
    }
}