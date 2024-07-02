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

    public static HttpVersion fromRepresentation(String representation) {
        for (HttpVersion version : HttpVersion.values()) {
            if (version.getRepresentation().equals(representation)) {
                return version;
            }
        }
        throw new IllegalArgumentException("No enum constant with representation " + representation);
    }
}
