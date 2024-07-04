package codesquad.http;

import codesquad.global.Path;

public enum MimeType {
    HTML("html", "text/html"),
    CSS("css", "text/css"),
    JS("js", "text/javascript"),
    ICO("ico", "image/x-icon"),
    PNG("png", "image/png"),
    JPG("jpg", "image/jpeg"),
    SVG("svg", "image/svg+xml");

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

    public static MimeType fromExt(Path file) {
        String ext = file.getExt();
        for (MimeType mimeType : MimeType.values()) {
            if (ext.equals(mimeType.ext)) {
                return mimeType;
            }
        }

        throw new IllegalArgumentException("No mimetype enum constant with extension" + ext);
    }
}