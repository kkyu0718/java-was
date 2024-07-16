package codesquad.http;

public enum MimeType {
    HTML("html", "text/html"),
    CSS("css", "text/css"),
    JS("js", "text/javascript"),
    ICO("ico", "image/x-icon"),
    PNG("png", "image/png"),
    JPG("jpg", "image/jpeg"),
    SVG("svg", "image/svg+xml"),
    X_WWW_FORM_URLENCODED(null, "application/x-www-form-urlencoded"),
    NONE(null, null),
    TEXT_PLAIN("text", "text/plain"),
    APPLICATION_JSON("json", "application/json");

    private String ext;
    private String mimeType;

    MimeType(String ext, String mimeType) {
        this.ext = ext;
        this.mimeType = mimeType;
    }

    public String getExt() {
        return ext;
    }

    public String getMimeType() {
        return mimeType;
    }

    public static MimeType fromExt(String ext) {
        for (MimeType mimeType : MimeType.values()) {
            if (ext.equals(mimeType.ext)) {
                return mimeType;
            }
        }

        throw new IllegalArgumentException("No mimetype enum constant with extension " + ext);
    }

    public static MimeType fromMimeType(String other) {
        for (MimeType mimeType : MimeType.values()) {
            if (other.equals(mimeType.mimeType)) {
                return mimeType;
            }
        }

        throw new IllegalArgumentException("No mimetype enum constant with mimetype " + other);
    }
}