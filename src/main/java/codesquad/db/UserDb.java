package codesquad.db;

import codesquad.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserDb {
    private static List<User> users = new ArrayList<>();

    private UserDb() {
    }

    public static void add(User user) {
        users.add(user);
    }

    public static int size() {
        return users.size();
    }
}
