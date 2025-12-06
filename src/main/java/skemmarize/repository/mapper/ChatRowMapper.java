package skemmarize.repository.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import skemmarize.model.Chat;

public class ChatRowMapper implements RowMapper<Chat> {
    @Override
    public Chat mapRow(ResultSet rs, int RowNum) throws SQLException {
        Chat chat = new Chat(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("image_url"),
                rs.getString("response")
            );
        return chat;
    }
}
