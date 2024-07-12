package codesquad.service;

import codesquad.db.UserSession;

import java.util.UUID;

public class UserSessionService {
    public boolean isActiveSession(UUID sessionId) {
        return UserSession.contains(sessionId);
    }

    public String getUserId(UUID uuid) {
        return UserSession.getUserId(uuid);
    }

    public String createSession(String userId) {
        return UserSession.create(userId);
    }
}
