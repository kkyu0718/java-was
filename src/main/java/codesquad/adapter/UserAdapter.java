package codesquad.adapter;

import codesquad.annotation.RequestMapping;
import codesquad.db.UserDb;
import codesquad.http.*;
import codesquad.model.User;
import codesquad.model.dao.UserCreateDao;
import codesquad.model.dao.UserLoginDao;
import codesquad.service.UserDbServiceSpec;
import codesquad.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserAdapter implements Adapter {
    private static final Logger logger = LoggerFactory.getLogger(UserAdapter.class);
    private static String LOGIN_SUCESS_PAGE = "/index.html";
    private static String SIGNUP_SUCESS_PAGE = "/index.html";
    private static String LOGIN_FAIL_PAGE = "/login/error.html";
    private UserDbServiceSpec userDbService;
    private UserSessionService userSessionService;

    public UserAdapter(UserDbServiceSpec userDbService, UserSessionService userSessionService) {
        this.userDbService = userDbService;
        this.userSessionService = userSessionService;
    }

    @Override
    public boolean supports(String path) {
        return path.startsWith("/user");
    }

    @RequestMapping(path = "/user/create", method = HttpMethod.POST)
    public HttpResponse createUser(HttpRequest request) {
        logger.debug("createUser start");
        HttpBody body = request.getBody();
        UserCreateDao dao = body.parse(UserCreateDao.class);
        String userId = dao.getUserId();
        String password = dao.getPassword();
        String name = dao.getName();
        String email = dao.getEmail();

        if (userDbService.exists(userId)) {
            logger.error("아이디가 중복되는 유저입니다." + userId);
            return new HttpResponse.Builder(request, HttpStatus.ILLEGAL_ARGUMENT)
                    .build();
        }

        userDbService.add(User.of(userId, password, name, email));

        logger.debug("user list");
        for (User user : userDbService.getUsers()) {
            logger.debug(user.toString());
        }

        return new HttpResponse.Builder(request, HttpStatus.FOUND)
                .header("Location", SIGNUP_SUCESS_PAGE)
                .build();
    }

    @RequestMapping(path = "/user/login", method = HttpMethod.POST)
    public HttpResponse login(HttpRequest request) {
        logger.info("login start");
        logger.info("UserDb : ", UserDb.print());

        HttpBody body = request.getBody();
        UserLoginDao dao = body.parse(UserLoginDao.class);
        String userId = dao.getUserId();
        String password = dao.getPassword();

        // 유저가 존재하지 않는 경우
        if (!userDbService.exists(userId)) {
            logger.error("유저가 존재하지 않습니다. " + userId);
            return new HttpResponse.Builder(request, HttpStatus.FOUND)
                    .redirect(LOGIN_FAIL_PAGE)
                    .build();
        }

        User user = userDbService.getUser(userId);
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