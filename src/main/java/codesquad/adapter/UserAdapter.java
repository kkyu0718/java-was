package codesquad.adapter;

import codesquad.annotation.RequestMapping;
import codesquad.annotation.Session;
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

    @RequestMapping(path = "/user/create", method = "POST")
    public HttpResponse createUser(HttpRequest request) {
        logger.debug("createUser start");
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

        return new HttpResponse.Builder(request, HttpStatus.FOUND)
                .redirect("/index.html")
                .build();
    }

    @RequestMapping(path = "/user/login", method = "POST")
    public HttpResponse login(HttpRequest request) {
        logger.info("login start");
        logger.info("userDb", UserDb.print());

        HttpBody body = request.getBody();
        Parameters parameters = body.getParameters();
        String userId = parameters.getParameter("userId");
        String password = parameters.getParameter("password");

        // 유저가 존재하지 않는 경우
        if (!UserDb.exists(userId)) {
            logger.debug("존재하지 않는 유저" + userId);
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
            logger.debug("로그인 실패" + userId + " " + user.getPassword() + "input password " + password);
            return new HttpResponse.Builder(request, HttpStatus.FOUND)
                    .redirect("/login/error.html")
                    .build();
        }
    }

    @RequestMapping(path = "/user/info", method = "GET")
    public User getUserInfo(HttpRequest request, @Session String userId) {
        if (userId == null) {
            return null;
        }

        return UserDb.get(userId);
    }
}