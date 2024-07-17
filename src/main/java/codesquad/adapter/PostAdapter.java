package codesquad.adapter;

import codesquad.annotation.RequestMapping;
import codesquad.http.*;
import codesquad.model.dao.PostCreateDao;
import codesquad.service.PostServiceSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostAdapter implements Adapter {
    private static final Logger logger = LoggerFactory.getLogger(PostAdapter.class);

    private PostServiceSpec postService;

    public PostAdapter(PostServiceSpec postService) {
        this.postService = postService;
    }

    @Override
    public boolean supports(String path) {
        return path.startsWith("/posts");
    }

    @RequestMapping(path = "/posts/create", method = HttpMethod.POST)
    public HttpResponse createPost(HttpRequest request) {
        logger.debug("createPost start");
        HttpBody body = request.getBody();
        PostCreateDao dao = body.parse(PostCreateDao.class);
        dao.setUserId(request.getHeader("userId"));

        logger.debug("PostCreateDao : {}", dao);
        postService.createPost(dao);
        logger.debug("Post created successfully");

        return new HttpResponse.Builder(request, HttpStatus.OK)
                .build();
    }

}
