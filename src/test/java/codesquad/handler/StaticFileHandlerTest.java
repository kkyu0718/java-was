package codesquad.handler;

import codesquad.adapter.StaticFileAdapterSpec;
import codesquad.http.*;
import codesquad.model.User;
import codesquad.service.UserDbService;
import codesquad.service.UserSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static codesquad.resource.StaticResourceFactory.GUEST_GREETING;
import static org.junit.jupiter.api.Assertions.*;

public class StaticFileHandlerTest {
    private StaticFileHandler staticFileHandler;
    private StaticFileAdapterSpec staticFileReader;
    private UserSessionService userSessionService;
    private UserDbService userDbService;

    @BeforeEach
    public void setUp() {
        staticFileReader = new StaticFileAdapterSpec() {
            @Override
            public String readFileLines(String path) {
                if (path.equals("/index.html")) {
                    return "<html><body>{GREETING}</body></html>";
                } else if (path.equals("/user/list/index.html")) {
                    return "<html><body><table>{USERS}</table></body></html>";
                } else {
                    return "<html><body>File not found</body></html>";
                }
            }
        };
        userSessionService = new UserSessionService();
        userDbService = new UserDbService();
        staticFileHandler = new StaticFileHandler(staticFileReader, userSessionService, userDbService);
    }

    @Test
    public void 주어진_정적파일경로가_있을때_파일을_읽고_응답한다() {
        // given
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/index.html", HttpVersion.HTTP11).build();

        // when
        HttpResponse response = staticFileHandler.handle(request);

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertNotNull(response.getBody());
    }

    @Test
    public void 주어진_쿠키가_없을때_인덱스페이지에_GUEST_GREETING을_표시한다() {
        // given
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/index.html", HttpVersion.HTTP11).build();

        // when
        HttpResponse response = staticFileHandler.handle(request);

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(new String(response.getBody().getBytes()).contains(GUEST_GREETING));
    }

    @Test
    public void 주어진_세션쿠키가_유효할때_인덱스페이지에_유저이름을_표시한다() {
        // given
        String sessionId = userSessionService.createSession("testuser");
        userDbService.add(User.of("testuser", "password", "테스트유저", "test@example.com"));

        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/index.html", HttpVersion.HTTP11)
                .cookie(new HttpCookie.Builder("sid", sessionId).build())
                .build();

        // when
        HttpResponse response = staticFileHandler.handle(request);

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertTrue(new String(response.getBody().getBytes()).contains("안녕하세요 테스트유저"));
    }

    @Test
    public void 주어진_유저목록을_표시한다() {
        // given
        userDbService.add(User.of("user1", "password1", "유저1", "user1@example.com"));
        userDbService.add(User.of("user2", "password2", "유저2", "user2@example.com"));

        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/user/list/index.html", HttpVersion.HTTP11).build();

        // when
        HttpResponse response = staticFileHandler.handle(request);

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
        String responseBody = new String(response.getBody().getBytes());
        assertTrue(responseBody.contains("<td>user1</td>"));
        assertTrue(responseBody.contains("<td>user2</td>"));
        assertTrue(responseBody.contains("<td>유저1</td>"));
        assertTrue(responseBody.contains("<td>유저2</td>"));
    }
}
