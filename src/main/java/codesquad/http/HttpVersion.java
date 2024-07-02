package codesquad.http;

public enum HttpVersion {
    HTTP11("HTTP/1.1");

    private String representation;

    HttpVersion(String representation) {
        this.representation = representation;
    }

    public String getRepresentation() {
        return representation;
    }
}
