package codesquad.service;

import codesquad.db.UserDb;
import codesquad.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class UserDbServiceTest {
    private UserDbService userDbService;

    @BeforeEach
    public void setUp() {
        userDbService = new UserDbService();
        UserDb.refresh();
    }

    @Test
    public void 주어진_유저가_있을때_유저를_추가한다() {
        // given
        User user = User.of("user1", "password1", "User One", "user1@example.com");

        // when
        userDbService.add(user);

        // then
        assertTrue(userDbService.exists("user1"));
        assertEquals(user, userDbService.getUser("user1"));
    }

    @Test
    public void 주어진_중복된유저가_있을때_예외를_발생시킨다() {
        // given
        User user = User.of("user1", "password1", "User One", "user1@example.com");
        userDbService.add(user);

        // when
        // then
        assertThrows(IllegalArgumentException.class, () -> userDbService.add(user));
    }

    @Test
    public void 모든_유저를_반환한다() {
        // given
        User user1 = User.of("user1", "password1", "User One", "user1@example.com");
        User user2 = User.of("user2", "password2", "User Two", "user2@example.com");
        userDbService.add(user1);
        userDbService.add(user2);

        // when
        List<User> users = userDbService.getUsers();

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
        User user = userDbService.getUser(nonExistentUserId);

        // then
        assertNull(user);
    }

    @Test
    public void 유저가_존재하는지_확인한다() {
        // given
        User user = User.of("user1", "password1", "User One", "user1@example.com");
        userDbService.add(user);

        // when
        boolean exists = userDbService.exists("user1");

        // then
        assertTrue(exists);
    }

    @Test
    public void 유저가_존재하지_않는지_확인한다() {
        // given
        String nonExistentUserId = "nonexistent";

        // when
        boolean exists = userDbService.exists(nonExistentUserId);

        // then
        assertFalse(exists);
    }
}
