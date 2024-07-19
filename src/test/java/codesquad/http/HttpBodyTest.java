package codesquad.http;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class HttpBodyTest {

    @Test
    void parseJsonToObject() {
        String json = "{" +
                "\"userId\":\"john_doe\"," +
                "\"password\":\"secret\"," +
                "\"name\":\"John Doe\"," +
                "\"email\":\"john@example.com\"," +
                "\"address\":{\"street\":\"123 Main St\",\"city\":\"Anytown\"}," +
                "\"phones\":[{\"type\":\"home\",\"number\":\"123-456-7890\"},{\"type\":\"work\",\"number\":\"987-654-3210\"}]" +
                "}";
        HttpBody httpBody = HttpBody.of(json.getBytes(), MimeType.APPLICATION_JSON);

        try {
            TestUser user = httpBody.parse(TestUser.class);
            assertEquals("john_doe", user.getUserId());
            assertEquals("secret", user.getPassword());
            assertEquals("John Doe", user.getName());
            assertEquals("john@example.com", user.getEmail());
            assertEquals("123 Main St", user.getAddress().getStreet());
            assertEquals("Anytown", user.getAddress().getCity());
            List<Phone> phones = user.getPhones();
            assertEquals(2, phones.size());
            assertEquals("home", phones.get(0).getType());
            assertEquals("123-456-7890", phones.get(0).getNumber());
            assertEquals("work", phones.get(1).getType());
            assertEquals("987-654-3210", phones.get(1).getNumber());
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }
}
