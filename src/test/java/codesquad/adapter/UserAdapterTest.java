package codesquad.adapter;

import codesquad.db.UserDb;
import codesquad.db.UserSession;
import codesquad.http.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserAdapterTest {
    private UserAdapter userAdapter;

    @BeforeEach
    public void setUp() {
        userAdapter = new UserAdapter();
        UserSession.refresh();
        UserDb.refresh();
    }

    @Test
    public void prefix가_user인_api를_제공한다() {
        String path = "/user/create";
        boolean supports = userAdapter.supports(path);
        assertEquals(true, supports);

        String invalidPath = "/invalid/path";
        supports = userAdapter.supports(invalidPath);
        assertEquals(false, supports);
    }

    @Test
    public void create메소드를_통해_유저를_저장한다() {
        byte[] bytes1 = "userId=id1&password=1234&name=kyu1&email=email1".getBytes();
        byte[] bytes2 = "userId=id2&password=1234&name=kyu2&email=email2".getBytes();

        HttpRequest request1 = new HttpRequest.Builder(HttpMethod.POST, "/user/create", HttpVersion.HTTP11)
                .body(HttpBody.of(bytes1, MimeType.X_WWW_FORM_URLENCODED))
                .build();
        HttpRequest request2 = new HttpRequest.Builder(HttpMethod.POST, "/user/create", HttpVersion.HTTP11)
                .body(HttpBody.of(bytes2, MimeType.X_WWW_FORM_URLENCODED))
                .build();

        HttpResponse response1 = userAdapter.handle(request1);
        HttpResponse response2 = userAdapter.handle(request2);

        assertEquals(HttpStatus.FOUND, response1.getStatus());
        assertEquals(HttpStatus.FOUND, response2.getStatus());
        assertEquals(2, UserDb.size());
    }

    @Test
    public void create메소드를_통해_유저를_저장할때_이미_존재하는_유저이면_400에러를_던진다() {
        String sameId = "똑같은 아이디~";
        byte[] bytes1 = String.format("userId=%s&password=1234&name=kyu1&email=email1", sameId).getBytes();
        byte[] bytes2 = String.format("userId=%s&password=1234&name=kyu2&email=email2", sameId).getBytes();

        HttpRequest request1 = new HttpRequest.Builder(HttpMethod.POST, "/user/create", HttpVersion.HTTP11)
                .body(HttpBody.of(bytes1, MimeType.X_WWW_FORM_URLENCODED))
                .build();
        HttpRequest request2 = new HttpRequest.Builder(HttpMethod.POST, "/user/create", HttpVersion.HTTP11)
                .body(HttpBody.of(bytes2, MimeType.X_WWW_FORM_URLENCODED))
                .build();

        HttpResponse response1 = userAdapter.handle(request1);
        HttpResponse response2 = userAdapter.handle(request2);

        assertEquals(HttpStatus.FOUND, response1.getStatus());
        assertEquals(HttpStatus.ILLEGAL_ARGUMENT, response2.getStatus());
        assertEquals(1, UserDb.size());
    }

    @Test
    public void create메소드를_실행하면_메인화면으로_리다이렉트된다() {
        byte[] bytes = "userId=id1&password=1234&name=kyu1&email=email1".getBytes();
        HttpRequest request = new HttpRequest.Builder(HttpMethod.POST, "/user/create", HttpVersion.HTTP11)
                .body(HttpBody.of(bytes, MimeType.X_WWW_FORM_URLENCODED))
                .build();

        HttpResponse response = userAdapter.handle(request);

        assertEquals(HttpStatus.FOUND, response.getStatus());
        assertEquals("/index.html", response.getHeaders().get("Location"));
    }

    @Test
    public void create메소드는_GET으로_동작하지_않는다() {
        byte[] bytes = "userId=id1&password=1234&name=kyu1&email=email1".getBytes();

        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/user/create", HttpVersion.HTTP11)
                .body(HttpBody.of(bytes, MimeType.X_WWW_FORM_URLENCODED))
                .build();

        Assertions.assertThrows(IllegalArgumentException.class, () -> userAdapter.handle(request));
    }

    @Test
    public void login메소드는_회원가입했던_유저에_대해서_세션을_만들어준다() {
        String userId = "id1";
        byte[] signupBytes = String.format("userId=%s&password=1234&name=kyu1&email=email1", userId).getBytes();

        HttpRequest signupRequest = new HttpRequest.Builder(HttpMethod.POST, "/user/create", HttpVersion.HTTP11)
                .body(HttpBody.of(signupBytes, MimeType.X_WWW_FORM_URLENCODED))
                .build();
        HttpResponse signupResponse = userAdapter.handle(signupRequest);

        Assertions.assertEquals(302, signupResponse.getStatus().getStatusCode());

        byte[] loginBytes = "userId=id1&password=1234".getBytes();
        HttpRequest loginRequest = new HttpRequest.Builder(HttpMethod.POST, "/user/login", HttpVersion.HTTP11)
                .body(HttpBody.of(loginBytes, MimeType.X_WWW_FORM_URLENCODED))
                .build();
        HttpResponse loginResponse = userAdapter.handle(loginRequest);

        Assertions.assertEquals(302, loginResponse.getStatus().getStatusCode());
        assertTrue(UserSession.contains(userId));
    }

    @Test
    public void login메소드는_회원가입안한_유저에_대해서_400에러를_던진다() {
        byte[] loginBytes = "userId=id1&password=1234".getBytes();
        HttpRequest loginRequest = new HttpRequest.Builder(HttpMethod.POST, "/user/login", HttpVersion.HTTP11)
                .body(HttpBody.of(loginBytes, MimeType.X_WWW_FORM_URLENCODED))
                .build();

        HttpResponse response = userAdapter.handle(loginRequest);

        Assertions.assertEquals(400, response.getStatus().getStatusCode());
    }

    @Test
    public void login메소드는_회원가입했던_유저에대해서_Set_Cookie헤더를_갖고_sid와_path를_포함한다() {
        byte[] signupBytes = "userId=id1&password=1234&name=kyu1&email=email1".getBytes();

        HttpRequest signupRequest = new HttpRequest.Builder(HttpMethod.POST, "/user/create", HttpVersion.HTTP11)
                .body(HttpBody.of(signupBytes, MimeType.X_WWW_FORM_URLENCODED))
                .build();
        HttpResponse signupResponse = userAdapter.handle(signupRequest);

        Assertions.assertEquals(302, signupResponse.getStatus().getStatusCode());

        byte[] loginBytes = "userId=id1&password=1234".getBytes();
        HttpRequest loginRequest = new HttpRequest.Builder(HttpMethod.POST, "/user/login", HttpVersion.HTTP11)
                .body(HttpBody.of(loginBytes, MimeType.X_WWW_FORM_URLENCODED))
                .build();
        HttpResponse loginResponse = userAdapter.handle(loginRequest);

        Assertions.assertEquals(302, loginResponse.getStatus().getStatusCode());

        HttpCookies httpCookies = loginResponse.getHttpCookies();
        Assertions.assertNotNull(httpCookies);

        assertTrue(httpCookies.contains("sid"));

        HttpCookie cookie = httpCookies.getCookie("sid");
        assertEquals(cookie.getPath(), "/");
    }
}
