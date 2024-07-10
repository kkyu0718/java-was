package codesquad.http;

public class HttpCookie {
    private String name;
    private String value;
    private String path;

    private HttpCookie(Builder builder) {
        this.name = builder.name;
        this.value = builder.value;
        this.path = builder.path;
    }

    public static class Builder {
        private final String name;
        private final String value;
        private String path;

        public Builder(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public Builder path(String path) {
            this.path = path;
            return this;
        }

        public HttpCookie build() {
            return new HttpCookie(this);
        }
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getPath() {
        return path;
    }
}
