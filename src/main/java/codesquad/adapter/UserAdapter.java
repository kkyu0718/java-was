package codesquad.adapter;

import codesquad.annotation.RequestMapping;
import codesquad.db.UserDb;
import codesquad.http.*;
import codesquad.model.User;
import codesquad.service.UserDbService;
import codesquad.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAdapter implements Adapter {
    private static final Logger logger = LoggerFactory.getLogger(UserAdapter.class);
    private static String LOGIN_SUCESS_PAGE = "/index.html";
    private static String SIGNUP_SUCESS_PAGE = "/index.html";
    private static String LOGIN_FAIL_PAGE = "login/error.html";
    private UserDbService userDbService;
    private UserSessionService userSessionService;

    public UserAdapter(UserDbService userDbService, UserSessionService userSessionService) {
        this.userDbService = userDbService;
        this.userSessionService = userSessionService;
    }

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

        if (userDbService.exists(userId)) {
            logger.error("아이디가 중복되는 유저입니다." + userId);
            return new HttpResponse.Builder(request, HttpStatus.ILLEGAL_ARGUMENT)
                    .build();
        }

        userDbService.add(User.of(userId, password, name, email));
        logger.debug(UserDb.print());

        return new HttpResponse.Builder(request, HttpStatus.FOUND)
                .header("Location", SIGNUP_SUCESS_PAGE)
                .build();
    }

    @RequestMapping(path = "/user/login", method = "POST")
    public HttpResponse login(HttpRequest request) {
        logger.info("login start");
        logger.info("UserDb : ", UserDb.print());

        HttpBody body = request.getBody();
        Parameters parameters = body.getParameters();
        String userId = parameters.getParameter("userId");
        String password = parameters.getParameter("password");

        // 유저가 존재하지 않는 경우
        if (!userDbService.exists(userId)) {
            logger.error("유저가 존재하지 않습니다. " + userId);
            return new HttpResponse.Builder(request, HttpStatus.FOUND)
                    .redirect(LOGIN_FAIL_PAGE)
                    .build();
        }

        User user = UserDb.get(userId);
        if (user.getPassword().equals(password)) {
            String sessionId = userSessionService.createSession(userId);
            logger.debug("로그인 성공 " + sessionId);

            HttpCookie cookie = new HttpCookie.Builder("sid", sessionId)
                    .path("/")
                    .build();

            return new HttpResponse.Builder(request, HttpStatus.FOUND)
                    .redirect(LOGIN_SUCESS_PAGE)
                    .cookie(cookie)
                    .build();
        } else {
            logger.error("로그인 실패 비밀번호가 일치하지 않습니다." + userId + " " + user.getPassword() + "input password " + password);
            return new HttpResponse.Builder(request, HttpStatus.FOUND)
                    .redirect(LOGIN_FAIL_PAGE)
                    .build();
        }
    }


}