package codesquad.service;

import codesquad.model.Comment;
import codesquad.model.dao.CommentCreateDao;

import java.util.List;

public interface CommentServiceSpec {
    void createComment(CommentCreateDao dao);

    Comment getComment(Long id);

    List<Comment> getComments();
}
