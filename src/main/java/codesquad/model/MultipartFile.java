package codesquad.model;

public class MultipartFile {
    private String fileName;
    private String contentType;
    private byte[] content;

    public MultipartFile(String fileName, String contentType, byte[] content) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.content = content;
    }

    // Getters
    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        return content;
    }
}