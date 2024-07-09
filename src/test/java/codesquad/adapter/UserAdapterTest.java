package codesquad.adapter;

import codesquad.db.UserDb;
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
        String path = "/user/create";
        boolean supports = userAdapter.supports(path);
        assertEquals(true, supports);

        String invalidPath = "/invalid/path";
        supports = userAdapter.supports(invalidPath);
        assertEquals(false, supports);
    }

    @Test
    public void UserAdapter가_주어졌을때_create메소드를_통해_유저를_저장한다() {
        byte[] bytes1 = "userId=id1&password=1234&name=kyu1&email=email1".getBytes();
        byte[] bytes2 = "userId=id2&password=1234&name=kyu2&email=email2".getBytes();

        HttpRequest request1 = new HttpRequest(HttpMethod.POST, "/user/create", HttpVersion.HTTP11, new HttpHeaders(), new HttpBody(bytes1, MimeType.X_WWW_FORM_URLENCODED), null);
        HttpRequest request2 = new HttpRequest(HttpMethod.POST, "/user/create", HttpVersion.HTTP11, new HttpHeaders(), new HttpBody(bytes2, MimeType.X_WWW_FORM_URLENCODED), null);

        HttpResponse response1 = userAdapter.handle(request1);
        HttpResponse response2 = userAdapter.handle(request2);

        assertEquals(HttpStatus.FOUND, response1.getStatus());
        assertEquals(HttpStatus.FOUND, response2.getStatus());
        assertEquals(2, UserDb.size());
    }

    @Test
    public void UserAdapter가_주어졌을때_create메소드를_실행하면_메인화면으로_리다이렉트된다() {
        byte[] bytes = "userId=id1&password=1234&name=kyu1&email=email1".getBytes();
        HttpRequest request = new HttpRequest(HttpMethod.POST, "/user/create", HttpVersion.HTTP11, new HttpHeaders(), new HttpBody(bytes, MimeType.X_WWW_FORM_URLENCODED), null);

        HttpResponse response = userAdapter.handle(request);

        assertEquals(HttpStatus.FOUND, response.getStatus());
        assertEquals("/index.html", response.getHeaders().get("Location"));
    }


}