package codesquad.service;

import codesquad.exception.NotFoundException;
import codesquad.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UserDbServiceCsvTest {
    private UserDbServiceCsv userDbServiceCsv;
    private final String csvFilePath = System.getProperty("user.home") + "/database/users.csv";

    @BeforeEach
    void setup() {
        // Ensure the CSV file is deleted before each test
        try {
            Files.deleteIfExists(Paths.get(csvFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        userDbServiceCsv = new UserDbServiceCsv(csvFilePath);

        // Prepopulate with some users
        userDbServiceCsv.add(User.of("user1", "pass1", "User One", "user1@example.com"));
        userDbServiceCsv.add(User.of("user2", "pass2", "User Two", "user2@example.com"));
    }

    @AfterEach
    void cleanup() {
        try {
            Files.deleteIfExists(Paths.get(csvFilePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void 주어진_유저가_존재할_때_유저를_가져올_수_있다() {
        // when
        User user = userDbServiceCsv.getUser("user1");

        // then
        assertNotNull(user);
        assertEquals("user1", user.getUserId());
        assertEquals("pass1", user.getPassword());
        assertEquals("User One", user.getName());
        assertEquals("user1@example.com", user.getEmail());
    }

    @Test
    void 주어진_유저가_존재하지_않을_때_유저를_가져오려하면_NotFoundException이_발생한다() {
        // when & then
        assertThrows(NotFoundException.class, () -> userDbServiceCsv.getUser("nonexistentuser"));
    }

    @Test
    void 모든_유저를_가져올_수_있다() {
        // when
        List<User> users = userDbServiceCsv.getUsers();

        // then
        assertEquals(2, users.size());
        assertEquals("user1", users.get(0).getUserId());
        assertEquals("user2", users.get(1).getUserId());
    }

    @Test
    void 주어진_유저가_존재하는지_확인할_수_있다() {
        // when
        boolean exists = userDbServiceCsv.exists("user1");

        // then
        assertTrue(exists);
    }

    @Test
    void 주어진_유저가_존재하지_않는지_확인할_수_있다() {
        // when
        boolean exists = userDbServiceCsv.exists("nonexistentuser");

        // then
        assertFalse(exists);
    }

    @Test
    void 유저를_추가하면_성공한다() {
        // given
        User newUser = User.of("user3", "pass3", "User Three", "user3@example.com");

        // when
        userDbServiceCsv.add(newUser);

        // then
        User user = userDbServiceCsv.getUser("user3");
        assertNotNull(user);
        assertEquals("user3", user.getUserId());
        assertEquals("pass3", user.getPassword());
        assertEquals("User Three", user.getName());
        assertEquals("user3@example.com", user.getEmail());
    }
}
