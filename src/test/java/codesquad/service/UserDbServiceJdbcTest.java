package codesquad.service;

import codesquad.db.DbConfig;
import codesquad.exception.NotFoundException;
import codesquad.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDbServiceJdbcTest {
    private UserDbServiceJdbc userDbService;
    private DbConfig dbConfig;

    @BeforeEach
    void setUp() throws SQLException {
        dbConfig = new DbConfig("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        userDbService = new UserDbServiceJdbc(dbConfig);

        // 테스트 데이터베이스 설정
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS User (user_id VARCHAR(50) PRIMARY KEY, password VARCHAR(50), name VARCHAR(50), email VARCHAR(100))");
            stmt.execute("INSERT INTO USER VALUES ('user1', 'pass1', 'User One', 'user1@example.com')");
            stmt.execute("INSERT INTO USER VALUES ('user2', 'pass2', 'User Two', 'user2@example.com')");
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS User");
        }
    }

    @Test
    void 존재하는_유저를_정상적으로_조회한다() {
        User user = userDbService.getUser("user1");
        assertNotNull(user);
        assertEquals("user1", user.getUserId());
        assertEquals("pass1", user.getPassword());
        assertEquals("User One", user.getName());
        assertEquals("user1@example.com", user.getEmail());
    }

    @Test
    void 존재하지않는_유저를_조회하면_Not_Found_Exception_발생한다() {
        assertThrows(NotFoundException.class, () -> userDbService.getUser("nonexistent"));
    }

    @Test
    void 존재하는_모든_유저를_정상적으로_조회한다() {
        List<User> users = userDbService.getUsers();
        assertEquals(2, users.size());
        assertTrue(users.stream().anyMatch(u -> u.getUserId().equals("user1")));
        assertTrue(users.stream().anyMatch(u -> u.getUserId().equals("user2")));
    }

    @Test
    void 유저의_존재_여부를_확인한다() {
        assertTrue(userDbService.exists("user1"));
        assertFalse(userDbService.exists("nonexistent"));
    }

    @Test
    void 유저를_추가했을때_정상적으로_저장된다() {
        User newUser = User.of("user3", "pass3", "User Three", "user3@example.com");
        userDbService.add(newUser);

        User retrievedUser = userDbService.getUser("user3");
        assertNotNull(retrievedUser);
        assertEquals(newUser.getUserId(), retrievedUser.getUserId());
        assertEquals(newUser.getName(), retrievedUser.getName());
        assertEquals(newUser.getEmail(), retrievedUser.getEmail());
    }
}