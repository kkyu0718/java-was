package codesquad.handler;

import codesquad.exception.InternalServerError;
import codesquad.exception.NotFoundException;
import codesquad.http.*;
import codesquad.model.Comment;
import codesquad.model.Post;
import codesquad.model.User;
import codesquad.reader.StaticFileReaderSpec;
import codesquad.render.TemplateEngine;
import codesquad.service.CommentServiceSpec;
import codesquad.service.PostServiceSpec;
import codesquad.service.UserDbServiceSpec;
import codesquad.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

import static codesquad.resource.StaticResourceFactory.*;

public class StaticFileHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(StaticFileHandler.class);
    private final List<StaticFileReaderSpec> staticFileReaders;
    private final UserSessionService userSessionService;
    private final UserDbServiceSpec userDbService;
    private final PostServiceSpec postService;
    private final CommentServiceSpec commentService;

    public StaticFileHandler(
            UserSessionService userSessionService,
            UserDbServiceSpec userDbService,
            PostServiceSpec postService,
            CommentServiceSpec commentService,
            StaticFileReaderSpec... staticFileReaders) {
        this.staticFileReaders = List.of(staticFileReaders);
        this.userSessionService = userSessionService;
        this.userDbService = userDbService;
        this.postService = postService;
        this.commentService = commentService;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        String path = request.getPath();

        if (request.getExt().equals("html")) {
            String template = loadFile(path);
            Map<String, String> paramMap = createParamMap(request);

            return createDynamicResponse(request, template, paramMap);
        }

        byte[] bytes = loadFileBytes(path);
        return new HttpResponse.Builder(request, HttpStatus.OK)
                .body(HttpBody.of(bytes, MimeType.fromExt(request.getExt())))
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(bytes.length))
                .header(HttpHeaders.CONTENT_TYPE, MimeType.fromExt(request.getExt()).getMimeType())
                .build();
    }

    private byte[] loadFileBytes(String path) {
        try {
            // 시스템 홈 경로와 static 폴더 경로 모두 탐색
            for (StaticFileReaderSpec reader : staticFileReaders) {
                if (reader.checkExistWithPrefix(path)) {
                    return reader.readFileBytesWithPrefix(path);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new InternalServerError("Error loading file " + path);
        }

        throw new NotFoundException("File not found: " + path);
    }

    private Map<String, String> createParamMap(HttpRequest request) {
        String path = request.getPath();
        Map<String, String> paramMap = new HashMap<>();

        if (path.equals("/index.html")) {
            Optional<HttpCookie> cookie = request.getHttpCookies().getCookie("sid");

            // 클라이언트에게 쿠키가 없거나 서버 세션에 존재하지 않는다면 GUEST
            if (cookie.isEmpty() || !userSessionService.isActiveSession(UUID.fromString(cookie.get().getValue()))) {
                paramMap.put("GREETING", GUEST_GREETING);
                paramMap.put("POSTS", NEED_LOGIN);
            } else {
                // 아니라면 인증된 유저
                String userId = userSessionService.getUserId(UUID.fromString(cookie.get().getValue()));
                User userInfo = userDbService.getUser(userId);
                List<Post> posts = postService.getPosts();

                paramMap.put("GREETING", getUserGreeting(userInfo.getName()));
                paramMap.put("POSTS", generatePostsHtml(posts));
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

    private String generatePostsHtml(List<Post> posts) {
        StringBuilder html = new StringBuilder();

        if (posts.isEmpty()) {
            return NO_POSTS;
        }

        for (Post post : posts) {
            User writer = userDbService.getUser(post.getUserId());
            List<Comment> commentsByPostId = commentService.getCommentsByPostId(post.getId());

            html.append("<div class=\"post\">")
                    .append("<div class=\"post__account\">")
                    .append("<p class=\"post__account__nickname\">").append(writer.getName()).append("</p>")
                    .append("</div>")
                    .append("<img class=\"post__img\" src=").append("\"").append(post.getImageUrl()).append("\"").append("/>")
                    .append("<div class=\"post__menu\">")
                    .append("<ul class=\"post__menu__personal\">")
                    .append("<li><button class=\"post__menu__btn\"><img src=\"./img/like.svg\"/></button></li>")
                    .append("<li><button class=\"post__menu__btn\"><img src=\"./img/sendLink.svg\"/></button></li>")
                    .append("</ul>")
                    .append("<button class=\"post__menu__btn\"><img src=\"./img/bookMark.svg\"/></button>")
                    .append("</div>")
                    .append("<p class=\"post__article\" id=\"post-article\">").append(post.getPostContent()).append("</p>");

            // 댓글 목록 추가
            html.append("<div class=\"comments-section\">");
            for (Comment comment : commentsByPostId) {
                User commentUser = userDbService.getUser(comment.getUserId());
                html.append("<div class=\"comment\">")
                        .append("<div class=\"comment__header\">")
                        .append("<p class=\"comment__user\">").append(commentUser.getName()).append("</p>")
                        .append("</div>")
                        .append("<p class=\"comment__content\">").append(comment.getContent()).append("</p>")
                        .append("</div>");
            }
            html.append("</div>");

            // 댓글 입력 폼 추가
            String commentInputId = "comment-content-" + post.getId();
            html.append("<div class=\"comment-form-container\">")
                    .append("<form class=\"comment-form\" onsubmit=\"submitComment(event, ").append(post.getId()).append(", '").append(commentInputId).append("')\">")
                    .append("<input id=\"").append(commentInputId).append("\" type=\"text\" class=\"comment-input\" placeholder=\"Add a comment...\"/>")
                    .append("<button class=\"comment-submit\" type=\"submit\">Submit</button>")
                    .append("</form>")
                    .append("</div>")
                    .append("</div>");
        }

        return html.toString();
    }
    
    private String loadFile(String path) {
        try {
            // 시스템 홈 경로와 static 폴더 경로 모두 탐색
            for (StaticFileReaderSpec reader : staticFileReaders) {
                logger.debug("reader: {}", reader.getClass().getName());
                if (reader.checkExistWithPrefix(path)) {
                    logger.debug("reader.checkExist {}", reader.checkExistWithPrefix(path));
                    return reader.readFileLinesWithPrefix(path);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new InternalServerError("Error loading file " + path);
        }

        throw new NotFoundException("File not found: " + path);
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
