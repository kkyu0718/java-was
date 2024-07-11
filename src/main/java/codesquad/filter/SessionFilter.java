package codesquad.filter;

import codesquad.db.UserSession;
import codesquad.http.HttpCookie;
import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;
import codesquad.http.HttpStatus;
import codesquad.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SessionFilter implements Filter {
    private UserSessionService userSessionService;

    public SessionFilter(UserSessionService userSessionService) {
        this.userSessionService = userSessionService;
    }

    private static final Logger log = LoggerFactory.getLogger(SessionFilter.class);
    private static final List<String> sessionNotNeededPaths = List.of(
            "/",
            "/index.html",
            "/registration",
            "/registration/index.html",
            "/article",
            "/article/index.html",
            "/comment",
            "/comment/index.html",
            "/main",
            "/main/index.html",
            "/login",
            "/login/index.html",
            "/login/error.html",
            "/user/login",
            "/user/create");

    private static final List<String> staticFileExtensions = List.of(
            ".css",
            ".js",
            ".png",
            ".jpg",
            ".jpeg",
            ".gif",
            ".svg",
            ".woff",
            ".woff2",
            ".ttf",
            ".eot",
            ".ico");

    @Override
    public HttpResponse doFilter(HttpRequest request, FilterChain chain) {
        String path = request.getPath();
        log.debug("Filtering path: " + path);

        // 세션이 필요 없는 경로인 경우 바로 다음 필터로 넘김
        if (sessionNotNeededPaths.contains(path) || isStaticFile(path)) {
            log.debug("Session not needed for this path");
            return chain.doFilter(request);
        }

        Optional<HttpCookie> cookie = request.getHttpCookies().getCookie("sid");

        // 세션이 유효한 경우 header에 userId를 넣어줌
        if (cookie.isPresent() && userSessionService.isActiveSession(UUID.fromString(cookie.get().getValue()))) {
            String userId = UserSession.getUserId(UUID.fromString(cookie.get().getValue()));
            request.getHeaders().put("userId", userId);
            log.debug("Session is active, added userId to headers: " + userId);

            return chain.doFilter(request);
        }

        // 인증이 안된 유저이면서 세션이 필요한 페이지인 경우 로그인 페이지로 리다이렉트
        log.debug("Session is not active, redirecting to login");
        return new HttpResponse.Builder(request, HttpStatus.FOUND)
                .redirect("/login")
                .build();
    }

    private boolean isStaticFile(String path) {
        for (String extension : staticFileExtensions) {
            if (path.endsWith(extension)) {
                return true;
            }
        }
        return false;
    }
}