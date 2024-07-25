package codesquad.service;

import codesquad.db.UserDb;
import codesquad.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserDbServiceMemoryTest {
    private UserDbServiceMemory userDbServiceMemory;

    @BeforeEach
    public void setUp() {
        userDbServiceMemory = new UserDbServiceMemory();
        UserDb.refresh();
    }

    @Test
    public void 주어진_유저가_있을때_유저를_추가한다() {
        // given
        User user = User.of("user1", "password1", "User One", "user1@example.com");

        // when
        userDbServiceMemory.add(user);

        // then
        assertTrue(userDbServiceMemory.exists("user1"));
        assertEquals(user, userDbServiceMemory.getUser("user1"));
    }

    @Test
    public void 주어진_중복된유저가_있을때_예외를_발생시킨다() {
        // given
        User user = User.of("user1", "password1", "User One", "user1@example.com");
        userDbServiceMemory.add(user);

        // when
        // then
        assertThrows(IllegalArgumentException.class, () -> userDbServiceMemory.add(user));
    }

    @Test
    public void 모든_유저를_반환한다() {
        // given
        User user1 = User.of("user1", "password1", "User One", "user1@example.com");
        User user2 = User.of("user2", "password2", "User Two", "user2@example.com");
        userDbServiceMemory.add(user1);
        userDbServiceMemory.add(user2);

        // when
        List<User> users = userDbServiceMemory.getUsers();

        // then
        assertEquals(2, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
    }

    @Test
    public void 유저가_존재하지_않을때_반환한다() {
        // given
        String nonExistentUserId = "nonexistent";

        // when
        User user = userDbServiceMemory.getUser(nonExistentUserId);

        // then
        assertNull(user);
    }

    @Test
    public void 유저가_존재하는지_확인한다() {
        // given
        User user = User.of("user1", "password1", "User One", "user1@example.com");
        userDbServiceMemory.add(user);

        // when
        boolean exists = userDbServiceMemory.exists("user1");

        // then
        assertTrue(exists);
    }

    @Test
    public void 유저가_존재하지_않는지_확인한다() {
        // given
        String nonExistentUserId = "nonexistent";

        // when
        boolean exists = userDbServiceMemory.exists(nonExistentUserId);

        // then
        assertFalse(exists);
    }
}
