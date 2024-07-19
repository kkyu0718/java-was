package codesquad.model;

public class Comment {
    private Long commentId;
    private String userId;
    private String content;

    public Comment(Long commentId, String userId, String content) {
        this.commentId = commentId;
        this.userId = userId;
        this.content = content;
    }

    public Long getCommentId() {
        return commentId;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "commentId=" + commentId +
                ", userId='" + userId + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
