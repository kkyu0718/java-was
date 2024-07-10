package codesquad.http;

public class HttpCookie {
    private String name;
    private String value;
    private String path;

    public HttpCookie(String name, String value) {
        this.name = name;
        this.value = value;
    }


    public HttpCookie(String name, String value, String path) {
        this.name = name;
        this.value = value;
        this.path = path;
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
