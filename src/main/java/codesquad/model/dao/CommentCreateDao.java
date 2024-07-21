package codesquad.model.dao;

public class CommentCreateDao {
    private Integer postId;
    private String userId;
    private String content;

    public CommentCreateDao(Integer postId, String userId, String content) {
        this.postId = postId;
        this.userId = userId;
        this.content = content;
    }

    public CommentCreateDao() {
    }

    public Integer getPostId() {
        return postId;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
