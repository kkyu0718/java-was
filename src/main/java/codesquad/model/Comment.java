package codesquad.model;

public class Comment {
    private Integer commentId;
    private Integer postId;
    private String userId;
    private String content;

    public Comment(Integer commentId, Integer postId, String userId, String content) {
        this.commentId = commentId;
        this.postId = postId;
        this.userId = userId;
        this.content = content;
    }

    public Integer getCommentId() {
        return commentId;
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

    @Override
    public String toString() {
        return "Comment{" +
                "commentId=" + commentId +
                ", postId=" + postId +
                ", userId='" + userId + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
