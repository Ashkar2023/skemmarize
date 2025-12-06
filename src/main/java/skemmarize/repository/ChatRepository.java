package skemmarize.repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import skemmarize.model.Chat;
import skemmarize.repository.mapper.ChatRowMapper;

@Repository
public class ChatRepository {

    private final JdbcTemplate jdbcTemplate;

    public ChatRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Chat> findByUserId(Long userId) {
        String query = "SELECT * FROM chats WHERE user_id = ?";
        return jdbcTemplate.query(query, new ChatRowMapper(), userId);
    }

    public Chat save(Long userId, String imageUrl, String response) {
        String query = "INSERT INTO chats (user_id, image_url, response) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        // Insert the new chat into the database and return the corresponding Chat object
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, userId);
            ps.setString(2, imageUrl);
            ps.setString(3, response);
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();
        return new Chat(id, userId, imageUrl, response);
    }
}
