package codesquad.db;

import codesquad.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserDb {
    private static Map<String, User> users = new ConcurrentHashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(UserDb.class);

    private UserDb() {
    }

    public static void add(User user) {
        if (users.containsKey(user.getUserId())) {
            throw new IllegalArgumentException("이미 존재하는 유저입니다.");
        }
        users.put(user.getUserId(), user);
    }

    public static User get(String userId) {
        return users.get(userId);
    }

    public static int size() {
        return users.size();
    }

    public static void print() {
        for (User user : users.values()) {
            logger.debug(user.toString() + "\n");
        }
    }

    public static boolean exists(String userId) {
        return users.containsKey(userId);
    }

    public static void refresh() {
        users.clear();
    }
}
