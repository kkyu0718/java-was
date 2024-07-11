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

            Parameters parameters = body.getParameters();
            String userId = parameters.getParameter("userId");
            String password = parameters.getParameter("password");
            String name = parameters.getParameter("name");
            String email = parameters.getParameter("email");

            if (UserDb.exists(userId)) {
                return new HttpResponse.Builder(request, HttpStatus.ILLEGAL_ARGUMENT).build();
            }

            UserDb.add(User.of(userId, password, name, email));

            UserDb.print();

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.put("Location", "/index.html");
            return new HttpResponse.Builder(request, HttpStatus.FOUND)
                    .headers(httpHeaders)
                    .build();
        } else if (request.getPath().equals("/user/login") && request.getMethod() == HttpMethod.POST) {
            logger.info("login start");
            HttpBody body = request.getBody();

            Parameters parameters = body.getParameters();
            String userId = parameters.getParameter("userId");
            String password = parameters.getParameter("password");

            if (!UserDb.exists(userId)) {
                return new HttpResponse.Builder(request, HttpStatus.ILLEGAL_ARGUMENT).build();
            }

            User user = UserDb.get(userId);
            if (user.getPassword().equals(password)) {
                String sessionId = UserSession.create(userId);

                logger.debug("로그인 성공 " + sessionId);

                HttpCookie cookie = new HttpCookie.Builder("sid", sessionId)
                        .path("/")
                        .build();
                return new HttpResponse.Builder(request, HttpStatus.FOUND)
                        .redirect("/index.html")
                        .cookie(cookie)
                        .build();
            } else {
                return new HttpResponse.Builder(request, HttpStatus.FOUND)
                        .redirect("/login/error.html")
                        .build();
            }
        }

        throw new IllegalArgumentException("처리 가능한 메소드가 존재하지 않습니다." + request.getPath().toString());
    }
}
