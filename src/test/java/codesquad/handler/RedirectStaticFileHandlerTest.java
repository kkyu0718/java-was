package codesquad.handler;

import codesquad.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class RedirectStaticFileHandlerTest {
    private RedirectStaticFileHandler redirectStaticFileHandler;

    @BeforeEach
    public void setUp() {
        List<String> whitelist = List.of(
                "/",
                "/registration",
                "/article",
                "/comment",
                "/main",
                "/login",
                "/user/list"
        );
        redirectStaticFileHandler = new RedirectStaticFileHandler(whitelist);
    }

    @Test
    public void 주어진_리스트에_있는경로를_처리한다() {
        // given
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/login", HttpVersion.HTTP11).build();

        // when
        boolean canHandle = redirectStaticFileHandler.canHandle(request);

        // then
        assertTrue(canHandle);
    }

    @Test
    public void 주어진_리스트에_없는경로를_처리하지_않는다() {
        // given
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/nonexistent", HttpVersion.HTTP11).build();

        // when
        boolean canHandle = redirectStaticFileHandler.canHandle(request);

        // then
        assertFalse(canHandle);
    }

    @Test
    public void 주어진_경로를_루트일때_리다이렉트한다() {
        // given
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/", HttpVersion.HTTP11).build();

        // when
        HttpResponse response = redirectStaticFileHandler.handle(request);

        // then
        assertEquals(HttpStatus.FOUND, response.getStatus());
        assertEquals("/index.html", response.getHeaders().get("Location"));
    }

    @Test
    public void 주어진_경로를_리다이렉트한다() {
        // given
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/login", HttpVersion.HTTP11).build();

        // when
        HttpResponse response = redirectStaticFileHandler.handle(request);

        // then
        assertEquals(HttpStatus.FOUND, response.getStatus());
        assertTrue(response.getHeaders().get("Location").endsWith("/login/index.html"));
    }
}
