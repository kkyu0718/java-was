package codesquad.http;

public enum MimeType {
    HTML("html", "text/html"),
    CSS("css", "text/css"),
    JS("js", "text/javascript"),
    ICO("ico", "image/x-icon"),
    PNG("png", "image/png"),
    JPG("jpg", "image/jpg"),
    JPEG("jpeg", "image/jpeg"),
    SVG("svg", "image/svg+xml"),
    X_WWW_FORM_URLENCODED(null, "application/x-www-form-urlencoded"),
    NONE(null, null),
    TEXT_PLAIN("text", "text/plain"),
    APPLICATION_JSON("json", "application/json"),
    MULTIPART_FORM_DATA(null, "multipart/form-data");
    private String ext;
    private String mimeType;
    private String boundary;

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

    public String getBoundary() {
        return boundary;
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
        String type = other.split(";")[0];
        for (MimeType mimeType : MimeType.values()) {
            if (type.equals(mimeType.mimeType)) {
                if (mimeType == MimeType.MULTIPART_FORM_DATA) {
                    mimeType.boundary = other.substring(other.indexOf("boundary=") + 9);
                }
                return mimeType;
            }
        }

        throw new IllegalArgumentException("No mimetype enum constant with mimetype " + other);
    }
}