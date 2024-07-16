package codesquad.service;

import codesquad.db.DbConfig;
import codesquad.exception.NotFoundException;
import codesquad.model.User;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDbServiceJdbc implements UserDbServiceSpec {
    private DbConfig dbConfig;

    public UserDbServiceJdbc(DbConfig dbConfig) {
        this.dbConfig = dbConfig;
    }

    @Override
    public User getUser(String userId) {
        String sql = "SELECT * FROM USER WHERE user_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return User.of(
                            rs.getString("user_id"),
                            rs.getString("password"),
                            rs.getString("name"),
                            rs.getString("email")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get user", e);
        }
        throw new NotFoundException("User not found: " + userId);
    }

    @Override
    public List<User> getUsers() {
        String sql = "SELECT * FROM USER";
        List<User> users = new ArrayList<>();
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(User.of(
                        rs.getString("user_id"),
                        rs.getString("password"),
                        rs.getString("name"),
                        rs.getString("email")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get users", e);
        }
        return users;
    }

    @Override
    public boolean exists(String userId) {
        String sql = "SELECT COUNT(*) FROM USER WHERE user_id = ?";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check user existence", e);
        }
        return false;
    }

    @Override
    public void add(User user) {
        String sql = "INSERT INTO USER (user_id, password, name, email) VALUES (?, ?, ?, ?)";
        try (Connection conn = dbConfig.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUserId());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getName());
            pstmt.setString(4, user.getEmail());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add user", e);
        }
    }
}