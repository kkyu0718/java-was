package codesquad.db;

import codesquad.model.User;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class UserDb {
    private static Map<String, User> users = new ConcurrentHashMap();

    private UserDb() {
    }

    public static void add(User user) {
        users.put(user.getUserId(), user);
    }

    public static int size() {
        return users.size();
    }
}
