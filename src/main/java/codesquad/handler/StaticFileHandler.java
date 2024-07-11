package codesquad.handler;

import codesquad.adapter.Adapter;
import codesquad.adapter.UserAdapter;
import codesquad.annotation.Session;
import codesquad.db.UserSession;
import codesquad.http.*;
import codesquad.model.User;
import codesquad.reader.StaticFileReaderSpec;
import codesquad.render.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StaticFileHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(StaticFileHandler.class);
    private StaticFileReaderSpec staticFileReader;
    private List<Adapter> adapters;

    public StaticFileHandler(StaticFileReaderSpec staticFileReader, List<Adapter> adapters) {
        this.staticFileReader = staticFileReader;
        this.adapters = adapters;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        // 정적 파일 로딩
        String path = request.getPath();
        try {
            if (!isValid(path)) {
                return new HttpResponse.Builder(request, HttpStatus.NOT_FOUND).build();
            }
        } catch (IOException e) {
            throw new RuntimeException("error reading file " + path);
        }
        // 정적 파일 처리
        HttpResponse response = readFileAndCreateResponse(request);


        // 동적 요청 처리
        if (path.equals("/index.html")) {
            Map<String, String> userInfoMap = new HashMap<>();

            HttpCookies httpCookies = request.getHttpCookies();
            HttpCookie cookie = httpCookies.getCookie("sid");
            if (!UserSession.contains(UUID.fromString(cookie.getValue()))) {
                userInfoMap.put("GREETING", "<li class=\"header__menu__item\">\n" +
                        "                <a class=\"btn btn_contained btn_size_s\" href=\"/login\">로그인</a>\n" +
                        "            </li>\n" +
                        "            <li class=\"header__menu__item\">\n" +
                        "                <a class=\"btn btn_ghost btn_size_s\" href=\"/registration\">\n" +
                        "                    회원 가입\n" +
                        "                </a>\n" +
                        "            </li>");

                try {
                    String template = staticFileReader.readFileLines("/index.html");
                    String render = TemplateEngine.render(template, userInfoMap);

                    return new HttpResponse.Builder(request, HttpStatus.OK)
                            .body(HttpBody.of(render.getBytes(), MimeType.HTML))
                            .build();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            String userId = UserSession.getUserId(UUID.fromString(cookie.getValue()));

            UserAdapter userAdapter = new UserAdapter();
            User userInfo = userAdapter.getUserInfo(request, userId);

            // Assuming userInfo is an instance of User
            userInfoMap.put("userId", userInfo.getUserId());
            userInfoMap.put("password", userInfo.getPassword()); // Consider the security implications
            userInfoMap.put("name", userInfo.getName());
            userInfoMap.put("email", userInfo.getEmail());
            userInfoMap.put("GREETING", String.format("<li class=\"header__menu__item\">\n" +
                    "            <div>안녕하세요 %s</div>\n" +
                    "          </li>", userInfo.getName()));

            try {
                String template = staticFileReader.readFileLines("/index.html");
                String render = TemplateEngine.render(template, userInfoMap);

                return new HttpResponse.Builder(request, HttpStatus.OK)
                        .body(HttpBody.of(render.getBytes(), MimeType.HTML))
                        .build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }

        return readFileAndCreateResponse(request);
    }

    private boolean isValid(String path) throws IOException {
        return staticFileReader.exists(path);
    }

    private HttpResponse readFileAndCreateResponse(HttpRequest request) {
        try {
            String template = staticFileReader.readFileLines(request.getPath());
            String render = TemplateEngine.render(template, new HashMap<>());
            MimeType contentType = MimeType.fromExt(request.getExt());

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.put(HttpHeaders.CONTENT_TYPE, contentType.getMimeType());
            httpHeaders.put(HttpHeaders.CONTENT_LENGTH, String.valueOf(render.getBytes() != null ? render.getBytes().length : 0));

            return new HttpResponse.Builder(request, HttpStatus.OK)
                    .headers(httpHeaders)
                    .body(HttpBody.of(render.getBytes(), contentType))
                    .build();
        } catch (IOException ex) {
            logger.error("Error reading file: " + request.getPath());
            return new HttpResponse.Builder(request, HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    private Object[] buildMethodArguments(Method method, HttpRequest request) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (param.getType() == HttpRequest.class) {
                args[i] = request;
            } else if (param.isAnnotationPresent(Session.class)) {
                args[i] = request.getHeader("userId");
            } else {
                // 다른 파라미터 처리 로직
            }
        }

        return args;
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        return request.isFilePath() || adapters.stream().anyMatch(adapter -> adapter.supports(request.getPath()));
    }
}
