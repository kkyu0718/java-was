package codesquad.http;

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

    public static HttpResponse createOkResponse(HttpRequest request, byte[] bytes, MimeType contentType) {
        HttpHeaders resHeaders = new HttpHeaders();
        resHeaders.put(HttpHeaders.HTTP_VERSION, request.getHttpVersion().getRepresentation());
        resHeaders.put(HttpHeaders.CONTENT_TYPE, contentType.getMimeType());

        return new HttpResponse(request, HttpStatus.OK, resHeaders, new HttpBody(bytes));
    }

    public static HttpResponse createErrorResponse(HttpRequest request) {
        HttpHeaders resHeaders = new HttpHeaders();
        resHeaders.put(HttpHeaders.HTTP_VERSION, request.getHttpVersion().getRepresentation());

        return new HttpResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, resHeaders, new HttpBody(null));
    }


    public static HttpResponse createNotFoundResponse(HttpRequest request) {
        HttpHeaders resHeaders = new HttpHeaders();
        resHeaders.put(HttpHeaders.HTTP_VERSION, request.getHttpVersion().getRepresentation());

        return new HttpResponse(request, HttpStatus.NOT_FOUND, resHeaders, new HttpBody(null));
    }
}
