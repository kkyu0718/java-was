package codesquad.db;

import codesquad.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserDb {
    private static Map<String, User> users = new ConcurrentHashMap();
    private static final Logger logger = LoggerFactory.getLogger(UserDb.class);

    private UserDb() {
    }

    public static void add(User user) {
        users.put(user.getUserId(), user);
    }

    public static int size() {
        return users.size();
    }

    public static void print() {
        for (User user : users.values()) {
            logger.debug(user.toString() + "\n");
        }
    }
}
