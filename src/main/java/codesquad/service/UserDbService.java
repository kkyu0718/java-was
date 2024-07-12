package codesquad.service;

import codesquad.db.UserDb;
import codesquad.model.User;

import java.util.List;

public class UserDbService {
    public User getUser(String userId) {
        return UserDb.get(userId);
    }

    public List<User> getUsers() {
        return UserDb.getUsers();
    }

    public boolean exists(String userId) {
        return UserDb.exists(userId);
    }

    public void add(User user) {
        UserDb.add(user);
    }
}
