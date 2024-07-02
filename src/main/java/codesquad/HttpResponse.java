package codesquad;

public class HttpResponse {
    private HttpRequest request;
    private HttpStatus status;
    private HttpHeaders headers;
    private HttpBody body;

    public HttpResponse(HttpRequest request, HttpStatus status, HttpHeaders headers, HttpBody body) {
        this.request = request;
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public HttpHeaders getHeaders() {
        return headers;
    }

    public HttpBody getBody() {
        return body;
    }
}
