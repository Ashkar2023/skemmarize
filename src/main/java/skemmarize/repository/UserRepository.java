package skemmarize.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Optional;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import skemmarize.model.User;
import skemmarize.repository.mapper.UserRowMapper;

@Repository
public class UserRepository {

    private final JdbcTemplate jdbc;

    public UserRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Optional<User> findByEmail(String email) {
        String query = "SELECT * FROM users WHERE email = ? LIMIT 1";

        try {
            User user = jdbc.queryForObject(query, new UserRowMapper(), email);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public User save(String email, String username, String avatarUrl) {
        String query = "INSERT INTO users (email, username, avatar) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Let database exceptions bubble up to service layer
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, email);
            ps.setString(2, username);
            ps.setString(3, avatarUrl);
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        User freshUser = new User(email, username);
        freshUser.setId(id);
        freshUser.setAvatar(avatarUrl);

        return freshUser;
    }
}
