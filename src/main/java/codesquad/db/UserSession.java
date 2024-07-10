package codesquad.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UserSession {
    private static final Logger logger = LoggerFactory.getLogger(UserSession.class);
    private static final Map<String, UUID> sessions = new HashMap<>();

    private UserSession() {
    }

    public static boolean contains(String userId) {
        return sessions.containsKey(userId);
    }

    public static String create(String userId) {
        UUID uuid = UUID.randomUUID();
        sessions.put(userId, uuid);

        return uuid.toString();
    }

    public static void refresh() {
        sessions.clear();
    }

    public static void print() {
        for (String userId : sessions.keySet()) {
            logger.debug(userId + ": " + sessions.get(userId));
        }
    }
}
