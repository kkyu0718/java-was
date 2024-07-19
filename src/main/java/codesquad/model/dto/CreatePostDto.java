package codesquad.model.dto;

public class CreatePostDto {
    private String postContent;
    private byte[] file;

    public CreatePostDto(String postContent, byte[] file) {
        this.postContent = postContent;
        this.file = file;
    }

    public String getPostContent() {
        return postContent;
    }

    public byte[] getFile() {
        return file;
    }
}
