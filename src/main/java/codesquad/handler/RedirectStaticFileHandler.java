package codesquad.handler;

import codesquad.adapter.Adapter;
import codesquad.adapter.UserAdapter;
import codesquad.db.UserDb;
import codesquad.db.UserSession;
import codesquad.http.*;
import codesquad.model.User;
import codesquad.reader.StaticFileReaderSpec;
import codesquad.render.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RedirectStaticFileHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(RedirectStaticFileHandler.class);
    private StaticFileReaderSpec staticFileReader;
    private List<String> whitelist;
    private final List<Adapter> adapters;


    public RedirectStaticFileHandler(
            StaticFileReaderSpec staticFileReader,
            List<String> whitelist,
            List<Adapter> adapters
    ) {
        this.staticFileReader = staticFileReader;
        this.whitelist = whitelist;
        this.adapters = adapters;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        String indexPath = request.getPath() + "/index.html";

        if (indexPath.equals("/index.html")) {
            return handleIndexHtml(request);
        } else if (indexPath.equals("/user/list/index.html")) {
            return handleUserListHtml(request);
        }

        return readFileAndCreateResponse(request);
    }

    private HttpResponse handleUserListHtml(HttpRequest request) {
        Map<String, String> userInfosMap = new HashMap<>();

        StringBuilder html = new StringBuilder();
        List<User> users = UserDb.getUsers();
        for (User user : users) {
            html.append("<tr>");
            html.append("<td>").append(user.getUserId()).append("</td>");
            html.append("<td>").append(user.getPassword()).append("</td>");
            html.append("<td>").append(user.getName()).append("</td>");
            html.append("<td>").append(user.getEmail()).append("</td>");
            html.append("</tr>");
        }

        logger.debug("users : " + users.toString());
        userInfosMap.put("USERS", html.toString());

        return createDynamicResponse(request, "/user/list/index.html", userInfosMap);
    }

    private HttpResponse handleIndexHtml(HttpRequest request) {
        Map<String, String> userInfoMap = new HashMap<>();

        HttpCookies httpCookies = request.getHttpCookies();
        HttpCookie cookie = httpCookies.getCookie("sid");

        if (cookie == null || UserSession.getUserId(UUID.fromString(cookie.getValue())) == null) {
            userInfoMap.put("GREETING", getGuestGreeting());
        } else {
            String userId = UserSession.getUserId(UUID.fromString(cookie.getValue()));
            UserAdapter userAdapter = new UserAdapter();
            User userInfo = userAdapter.getUserInfo(request, userId);

            if (userInfo == null) {
                return readFileAndCreateResponse(request);
            }

            userInfoMap.put("GREETING", getUserGreeting(userInfo.getName()));
        }

        return createDynamicResponse(request, "/index.html", userInfoMap);
    }

    private String getGuestGreeting() {
        return "<li class=\"header__menu__item\">\n" +
                "    <a class=\"btn btn_contained btn_size_s\" href=\"/login\">로그인</a>\n" +
                "</li>\n" +
                "<li class=\"header__menu__item\">\n" +
                "    <a class=\"btn btn_ghost btn_size_s\" href=\"/registration\">회원 가입</a>\n" +
                "</li>";
    }

    private String getUserGreeting(String userName) {
        return String.format("<li class=\"header__menu__item\">\n" +
                "    <div>안녕하세요 %s</div>\n" +
                "</li>", userName);
    }

    private HttpResponse createDynamicResponse(HttpRequest request, String templatePath, Map<String, String> userInfoMap) {
        try {
            String template = staticFileReader.readFileLines(templatePath);
            String render = TemplateEngine.render(template, userInfoMap);

            return new HttpResponse.Builder(request, HttpStatus.OK)
                    .body(HttpBody.of(render.getBytes(), MimeType.HTML))
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Error creating dynamic response for " + templatePath, e);
        }
    }

    private HttpResponse readFileAndCreateResponse(HttpRequest request) {
        try {
            String template = staticFileReader.readFileLines(request.getPath() + "/index.html");
            String render = TemplateEngine.render(template, new HashMap<>());
            MimeType contentType = MimeType.HTML;

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.put(HttpHeaders.CONTENT_TYPE, contentType.getMimeType());
            httpHeaders.put(HttpHeaders.CONTENT_LENGTH, String.valueOf(render.getBytes() != null ? render.getBytes().length : 0));

            return new HttpResponse.Builder(request, HttpStatus.OK)
                    .headers(httpHeaders)
                    .body(HttpBody.of(render.getBytes(), contentType))
                    .build();
        } catch (IOException ex) {
            logger.error("Error reading file: " + request.getPath(), ex);
            return new HttpResponse.Builder(request, HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        return whitelist.contains(request.getPath());
    }
}
