package codesquad.model.dao;

public class UserCreateDao {
    private String userId;
    private String password;
    private String email;
    private String name;

    public UserCreateDao() {
    }

    public UserCreateDao(String userId, String password, String email, String name) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }
}
