package codesquad.model;

public class Post {
    private Long id;
    private String userId;
    private String postContent;
    private String imageUrl;

    public Post(Long id, String userId, String postContent, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.postContent = postContent;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getPostContent() {
        return postContent;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", content='" + postContent + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                '}';
    }
}
