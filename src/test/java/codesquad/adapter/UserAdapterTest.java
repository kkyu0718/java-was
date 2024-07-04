package codesquad.adapter;

import codesquad.db.UserDb;
import codesquad.global.Path;
import codesquad.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserAdapterTest {
    private UserAdapter userAdapter;

    @BeforeEach
    public void setUp() {
        userAdapter = new UserAdapter();
    }

    @Test
    public void UserAdapter는_prefix가_user인_api를_제공한다() {
        Path path = Path.of("/user/create");
        boolean supports = userAdapter.supports(path);
        assertEquals(true, supports);

        Path invalidPath = Path.of("/invalid/path");
        supports = userAdapter.supports(invalidPath);
        assertEquals(false, supports);
    }

    @Test
    public void UserAdapter가_주어졌을때_create메소드를_통해_유저를_저장한다() {
        Parameters parameters1 = Parameters.of("userId=id1&password=1234&name=kyu1&email=email1");
        Parameters parameters2 = Parameters.of("userId=id2&password=1234&name=kyu2&email=email2");

        HttpRequest request1 = new HttpRequest(HttpMethod.POST, Path.of("/user/create"), HttpVersion.HTTP11, new HttpHeaders(), new HttpBody(null), parameters1);
        HttpRequest request2 = new HttpRequest(HttpMethod.POST, Path.of("/user/create"), HttpVersion.HTTP11, new HttpHeaders(), new HttpBody(null), parameters2);

        HttpResponse response1 = userAdapter.handle(request1);
        HttpResponse response2 = userAdapter.handle(request2);

        assertEquals(HttpStatus.OK, response1.getStatus());
        assertEquals(HttpStatus.OK, response2.getStatus());
        assertEquals(2, UserDb.size());
    }

    
}