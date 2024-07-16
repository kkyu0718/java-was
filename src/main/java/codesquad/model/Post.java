package codesquad.model;

public class Post {
    private Long id;
    private String userId;
    private String content;

    public Post(Long id, String userId, String content) {
        this.id = id;
        this.userId = userId;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
