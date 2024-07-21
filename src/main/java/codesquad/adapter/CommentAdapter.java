package codesquad.adapter;

import codesquad.annotation.RequestMapping;
import codesquad.http.*;
import codesquad.model.dao.CommentCreateDao;
import codesquad.service.CommentServiceSpec;
import codesquad.service.UserDbServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommentAdapter implements Adapter {
    private static final Logger logger = LoggerFactory.getLogger(CommentAdapter.class);
    private static final String COMMENT_SUCCESS_PAGE = "/index.html";
    private final CommentServiceSpec commentService;
    private final UserDbServiceSpec userDbService;

    public CommentAdapter(CommentServiceSpec commentService, UserDbServiceSpec userDbService) {
        this.commentService = commentService;
        this.userDbService = userDbService;
    }

    @Override
    public boolean supports(String path) {
        return path.startsWith("/comments");
    }

    @RequestMapping(path = "/comments", method = HttpMethod.POST)
    public HttpResponse createComment(HttpRequest request) {
        logger.debug("createComment start");
        HttpBody body = request.getBody();
        CommentCreateDao dao = body.parse(CommentCreateDao.class);
        int postId = dao.getPostId();
        String userId = request.getHeader("userId");
        dao.setUserId(userId);
        String content = dao.getContent();

        if (!userDbService.exists(userId)) {
            logger.error("유저가 존재하지 않습니다. " + userId);
            return new HttpResponse.Builder(request, HttpStatus.ILLEGAL_ARGUMENT)
                    .build();
        }

        logger.debug("comment create : " + postId + " " + userId + " " + content);
        commentService.createComment(dao);

        return new HttpResponse.Builder(request, HttpStatus.FOUND)
                .header("Location", COMMENT_SUCCESS_PAGE)
                .build();
    }
}
