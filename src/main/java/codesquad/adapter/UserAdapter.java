package codesquad.adapter;

import codesquad.db.UserDb;
import codesquad.db.UserSession;
import codesquad.http.*;
import codesquad.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAdapter implements Adapter {
    private static final Logger logger = LoggerFactory.getLogger(UserAdapter.class);

    @Override
    public boolean supports(String path) {
        return path.startsWith("/user");
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        if (request.getPath().equals("/user/create") && request.getMethod() == HttpMethod.POST) {
            HttpBody body = request.getBody();

            //TODO parameter 처리
            Parameters parameters = body.getParameters();
            String userId = parameters.getParameter("userId");
            String password = parameters.getParameter("password");
            String name = parameters.getParameter("name");
            String email = parameters.getParameter("email");

            if (UserDb.exists(userId)) {
                return HttpResponse.createIllegalArgumentResponse(request);
            }

            UserDb.add(User.of(userId, password, name, email));

            UserDb.print();
            return HttpResponse.createRedirectResponse(request, "/index.html");
        } else if (request.getPath().equals("/user/login") && request.getMethod() == HttpMethod.POST) {
            logger.info("login start");
            HttpBody body = request.getBody();

            Parameters parameters = body.getParameters();
            String userId = parameters.getParameter("userId");
            String password = parameters.getParameter("password");

            if (!UserDb.exists(userId)) {
                return HttpResponse.createIllegalArgumentResponse(request);
            }

            User user = UserDb.get(userId);
            if (user.getPassword().equals(password)) {
                String sessionId = UserSession.create(userId);

                logger.debug("로그인 성공 " + sessionId);
                HttpHeaders httpHeaders = new HttpHeaders();
//                httpHeaders.put(HttpHeaders.SET_COOKIE, String.format("sid=%s; Path=/", sessionId));

                HttpCookies cookies = new HttpCookies();
                HttpCookie cookie = new HttpCookie("sid", sessionId, "/");
                cookies.setCookie(cookie);
                return HttpResponse.createOkResponse(request, httpHeaders, null, null, cookies);
            } else {
                return HttpResponse.createRedirectResponse(request, "/login/error.html");
            }
        }

        throw new IllegalArgumentException("처리 가능한 메소드가 존재하지 않습니다." + request.getPath().toString());
    }
}
