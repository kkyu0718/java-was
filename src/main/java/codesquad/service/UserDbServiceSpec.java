package codesquad.service;

import codesquad.model.User;

import java.util.List;

public interface UserDbServiceSpec {
    User getUser(String userId);

    List<User> getUsers();

    boolean exists(String userId);

    void add(User user);
}
