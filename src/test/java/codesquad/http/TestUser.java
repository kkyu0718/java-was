package codesquad.http;

import java.util.List;

public class TestUser {
    private String userId;
    private String password;
    private String name;
    private String email;
    private Address address;
    private List<Phone> phones;

    public String getUserId() {
        return userId;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public List<Phone> getPhones() {
        return phones;
    }
}
