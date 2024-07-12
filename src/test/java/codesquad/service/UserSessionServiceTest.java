package codesquad.service;

import codesquad.db.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class UserSessionServiceTest {
    private UserSessionService userSessionService;

    @BeforeEach
    public void setUp() {
        userSessionService = new UserSessionService();
        UserSession.refresh();
    }

    @Test
    public void 주어진_유저아이디로_세션을_생성한다() {
        // given
        String userId = "testuser";

        // when
        String sessionId = userSessionService.createSession(userId);

        // then
        assertNotNull(sessionId);
        assertTrue(userSessionService.isActiveSession(UUID.fromString(sessionId)));
        assertEquals(userId, userSessionService.getUserId(UUID.fromString(sessionId)));
    }

    @Test
    public void 주어진_유효한_세션이_활성화되어있다() {
        // given
        String userId = "testuser";
        String sessionId = userSessionService.createSession(userId);
        UUID uuid = UUID.fromString(sessionId);

        // when
        boolean isActive = userSessionService.isActiveSession(uuid);

        // then
        assertTrue(isActive);
    }

    @Test
    public void 주어진_유효하지않은_세션이_활성화되어있지않다() {
        // given
        UUID invalidSessionId = UUID.randomUUID();

        // when
        boolean isActive = userSessionService.isActiveSession(invalidSessionId);

        // then
        assertFalse(isActive);
    }

    @Test
    public void 주어진_유효한_세션아이디로_유저아이디를_가져온다() {
        // given
        String userId = "testuser";
        String sessionId = userSessionService.createSession(userId);
        UUID uuid = UUID.fromString(sessionId);

        // when
        String retrievedUserId = userSessionService.getUserId(uuid);

        // then
        assertEquals(userId, retrievedUserId);
    }

    @Test
    public void 주어진_유효하지않은_세션아이디로_유저아이디를_가져올_수_없다() {
        // given
        UUID invalidSessionId = UUID.randomUUID();

        // when
        String retrievedUserId = userSessionService.getUserId(invalidSessionId);

        // then
        assertNull(retrievedUserId);
    }
}
