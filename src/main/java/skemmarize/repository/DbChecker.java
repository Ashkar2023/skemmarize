package skemmarize.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
public class DbChecker {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init(){
        boolean isConnected = testConnection();
        System.out.println("Database connection status: " + isConnected);
    }

    private boolean testConnection() {
        try {
            return jdbcTemplate.queryForObject("SELECT 1", Integer.class) == 1;
        } catch (Exception e) {
            System.out.println(e.getMessage() + " " + e.getCause() + " " + e.getStackTrace());
            return false;
        }
    }
}
