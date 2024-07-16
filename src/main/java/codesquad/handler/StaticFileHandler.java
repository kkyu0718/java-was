package codesquad.handler;

import codesquad.http.*;
import codesquad.model.User;
import codesquad.reader.StaticFileReaderSpec;
import codesquad.render.TemplateEngine;
import codesquad.service.UserDbServiceSpec;
import codesquad.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static codesquad.resource.StaticResourceFactory.GUEST_GREETING;
import static codesquad.resource.StaticResourceFactory.getUserGreeting;

public class StaticFileHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(StaticFileHandler.class);
    private final StaticFileReaderSpec staticFileReader;
    private final UserSessionService userSessionService;
    private final UserDbServiceSpec userDbService;

    public StaticFileHandler(
            StaticFileReaderSpec staticFileReader,
            UserSessionService userSessionService,
            UserDbServiceSpec userDbService) {
        this.staticFileReader = staticFileReader;
        this.userSessionService = userSessionService;
        this.userDbService = userDbService;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        String path = request.getPath();
        String template = loadFile(path);

        Map<String, String> paramMap = createParamMap(request);

        return createDynamicResponse(request, template, paramMap);
    }

    private Map<String, String> createParamMap(HttpRequest request) {
        String path = request.getPath();
        Map<String, String> paramMap = new HashMap<>();

        if (path.equals("/index.html")) {
            Optional<HttpCookie> cookie = request.getHttpCookies().getCookie("sid");

            // 클라이언트에게 쿠키가 없거나 서버 세션에 존재하지 않는다면 GUEST
            if (cookie.isEmpty() || !userSessionService.isActiveSession(UUID.fromString(cookie.get().getValue()))) {
                paramMap.put("GREETING", GUEST_GREETING);
            } else {
                // 아니라면 인증된 유저
                String userId = userSessionService.getUserId(UUID.fromString(cookie.get().getValue()));
                User userInfo = userDbService.getUser(userId);
                paramMap.put("GREETING", getUserGreeting(userInfo.getName()));
            }
        } else if (path.equals("/user/list/index.html")) {
            StringBuilder html = new StringBuilder();
            List<User> users = userDbService.getUsers();
            for (User user : users) {
                html.append("<tr>");
                html.append("<td>").append(user.getUserId()).append("</td>");
                html.append("<td>").append(user.getPassword()).append("</td>");
                html.append("<td>").append(user.getName()).append("</td>");
                html.append("<td>").append(user.getEmail()).append("</td>");
                html.append("</tr>");
            }

            paramMap.put("USERS", html.toString());
        }

        return paramMap;
    }

    private String loadFile(String templatePath) {
        try {
            return staticFileReader.readFileLines(templatePath);
        } catch (IOException e) {
            throw new RuntimeException("Error loading file " + templatePath, e);
        }
    }

    private HttpResponse createDynamicResponse(HttpRequest request, String template, Map<String, String> paramMap) {
        String render = TemplateEngine.render(template, paramMap);
        byte[] bytes = render.getBytes();
        return new HttpResponse.Builder(request, HttpStatus.OK)
                .body(HttpBody.of(bytes, MimeType.HTML))
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(bytes.length))
                .header(HttpHeaders.CONTENT_TYPE, MimeType.fromExt(request.getExt()).getMimeType())
                .build();
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        return request.isFilePath();
    }
}
