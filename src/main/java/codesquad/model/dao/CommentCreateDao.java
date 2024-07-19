package codesquad.model.dao;

public class CommentCreateDao {
    private String userId;
    private String content;

    public CommentCreateDao(String userId, String content) {
        this.userId = userId;
        this.content = content;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }
}
