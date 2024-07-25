package codesquad.service;

import codesquad.model.Post;
import codesquad.model.dao.PostCreateDao;

import java.util.List;

public interface PostServiceSpec {
    void createPost(PostCreateDao dao);

    Post getPost(Long id);

    List<Post> getPosts();
}
