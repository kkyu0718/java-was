package codesquad.service;

import codesquad.db.DbConfig;
import codesquad.exception.InternalServerError;
import codesquad.model.Post;
import codesquad.model.dao.PostCreateDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostServiceJdbc implements PostServiceSpec {
    private DbConfig dbConfig;

    public PostServiceJdbc(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    public void createPost(PostCreateDao dao) {
        String sql = "INSERT INTO Post (user_id, content, image_url) VALUES (?, ?, ?)";

        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, dao.getUserId());
            pstmt.setString(2, dao.getContent());
            pstmt.setString(3, dao.getFile() != null ? dao.getFile().getFileName() : null);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new InternalServerError("Failed to create post" + e);
        }
    }

    public Post getPost(Long id) {
        String sql = "SELECT * FROM Post WHERE id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Post(
                            rs.getLong("id"),
                            rs.getString("user_id"),
                            rs.getString("content"),
                            rs.getString("image_url")
                    );
                }
            }
        } catch (SQLException e) {
            throw new InternalServerError("Failed to get post: " + e.getMessage());
        }
        return null;
    }

    public List<Post> getPosts() {
        List<Post> posts = new ArrayList<>();
        String sql = "SELECT * FROM Post";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                posts.add(new Post(
                        rs.getLong("id"),
                        rs.getString("user_id"),
                        rs.getString("content"),
                        rs.getString("image_url")
                ));
            }
        } catch (SQLException e) {
            throw new InternalServerError("Failed to get posts: " + e.getMessage());
        }
        return posts;
    }
}
