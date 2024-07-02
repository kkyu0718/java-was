package codesquad;

public class HttpResponse {
    private HttpHeaders headers;
    private HttpBody body;

    public HttpResponse(HttpHeaders headers, HttpBody body) {
        this.headers = headers;
        this.body = body;
    }
}
