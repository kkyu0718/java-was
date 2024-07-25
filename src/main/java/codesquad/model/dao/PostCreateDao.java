package codesquad.model.dao;

import codesquad.model.MultipartFile;

public class PostCreateDao {
    private String postContent;
    private MultipartFile file;
    private String userId;
    private String imageUrl;

    public PostCreateDao() {
    }

    public PostCreateDao(String userId, String content, MultipartFile file, String imageUrl) {
        this.userId = userId;
        this.postContent = content;
        this.file = file;
        this.imageUrl = imageUrl;
    }

    public String getUserId() {
        return userId;
    }

    public String getContent() {
        return postContent;
    }

    public MultipartFile getFile() {
        return file;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "PostCreateDao{" +
                "userId='" + userId + '\'' +
                ", content='" + postContent + '\'' +
                ", image_url='" + imageUrl + '\'' +
                ", file='" + (file != null ? file.getFileName() : "null") + '\'' +
                '}';
    }
}
