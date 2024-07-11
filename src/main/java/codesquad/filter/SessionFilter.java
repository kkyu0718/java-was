package codesquad.filter;

import codesquad.db.UserSession;
import codesquad.http.*;

import java.util.UUID;

public class SessionFilter implements Filter {
    @Override
    public HttpResponse doFilter(HttpRequest request, FilterChain chain) {
        HttpCookies cookies = request.getHttpCookies();

        if (isActive(cookies)) {
            // header 에 userId 를 넣어줌
            request.getHeaders().put("userId", UserSession.getUserId(UUID.fromString(cookies.getCookie("sid").getValue()));
            return chain.doFilter(request);
        } else {
            return new HttpResponse.Builder(request, HttpStatus.FOUND)
                    .redirect("/login")
                    .build();
        }
    }

    private boolean isActive(HttpCookies cookies) {
        if (cookies.isEmpty()) {
            return false;
        }

        HttpCookie cookie = cookies.getCookie("sid");
        return cookie != null && UserSession.contains(UUID.fromString(cookie.getValue()));
    }
}