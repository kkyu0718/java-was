package codesquad.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserSession {
    private static final Logger logger = LoggerFactory.getLogger(UserSession.class);
    private static final Map<UUID, String> sessions = new HashMap<>();

    private UserSession() {
    }

    public static boolean contains(UUID sessionId) {
        return sessions.containsKey(sessionId);
    }

    public static boolean isActive(String userId) {
        return sessions.containsValue(userId);
    }

    public static String create(String userId) {
        UUID uuid = UUID.randomUUID();
        sessions.put(uuid, userId);

        return uuid.toString();
    }

    public static void refresh() {
        sessions.clear();
    }

    public static void print() {
        for (UUID uuid : sessions.keySet()) {
            logger.debug(uuid + ": " + sessions.get(uuid));
        }
    }

    public static String getUserId(UUID sessionId) {
        return sessions.get(sessionId);
    }
}
