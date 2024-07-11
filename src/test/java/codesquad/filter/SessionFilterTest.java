package codesquad.filter;

import codesquad.http.*;
import codesquad.service.UserSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SessionFilterTest {
    private SessionFilter sessionFilter;
    private UserSessionService userSessionService;
    private FilterChain filterChain;

    @BeforeEach
    public void setUp() {
        userSessionService = new UserSessionService();
        sessionFilter = new SessionFilter(userSessionService);
        filterChain = new FilterChain() {
            @Override
            public HttpResponse doFilter(HttpRequest request) {
                return new HttpResponse.Builder(request, HttpStatus.OK).build();
            }
        };
    }

    @Test
    public void 세션이_필요없는_경로에서_필터를_통과한다() {
        // given
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/login", HttpVersion.HTTP11).build();

        // when
        HttpResponse response = sessionFilter.doFilter(request, filterChain);

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    public void 유효한_세션이_있을때_필터를_통과하고_userId_헤더를_추가한다() {
        // given
        String sessionId = userSessionService.createSession("testuser");

        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/user/list", HttpVersion.HTTP11)
                .cookie(new HttpCookie.Builder("sid", sessionId).build())
                .build();

        // when
        HttpResponse response = sessionFilter.doFilter(request, filterChain);

        // then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals("testuser", request.getHeader("userId"));
    }

    @Test
    public void 유효하지않은_세션이_있을때_로그인페이지로_리다이렉트한다() {
        // given
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/user/list", HttpVersion.HTTP11)
                .cookie(new HttpCookie.Builder("sid", UUID.randomUUID().toString()).build())
                .build();

        // when
        HttpResponse response = sessionFilter.doFilter(request, filterChain);

        // then
        assertEquals(HttpStatus.FOUND, response.getStatus());
        assertEquals("/login", response.getHeaders().get("Location"));
    }

    @Test
    public void 세션이_없을때_로그인페이지로_리다이렉트한다() {
        // given
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/user/list", HttpVersion.HTTP11).build();

        // when
        HttpResponse response = sessionFilter.doFilter(request, filterChain);

        // then
        assertEquals(HttpStatus.FOUND, response.getStatus());
        assertEquals("/login", response.getHeaders().get("Location"));
    }
}
