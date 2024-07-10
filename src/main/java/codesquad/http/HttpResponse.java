package codesquad.http;

import codesquad.utils.StringUtils;

public class HttpResponse {
    private HttpRequest request;
    private HttpStatus status;
    private HttpHeaders headers;
    private HttpBody body;
    private HttpCookies httpCookies;

    public HttpResponse(HttpRequest request, HttpStatus status, HttpHeaders headers, HttpBody body) {
        this.request = request;
        this.status = status;
        this.headers = headers;
        this.body = body;
        this.httpCookies = new HttpCookies();
    }

    public HttpResponse(HttpRequest request, HttpStatus status, HttpHeaders headers, HttpBody body, HttpCookies cookies) {
        this.request = request;
        this.status = status;
        this.headers = headers;
        this.body = body;
        this.httpCookies = cookies;
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

    public HttpCookies getHttpCookies() {
        return httpCookies;
    }

    public void setCookie(HttpCookie cookie) {
        httpCookies.setCookie(cookie);
    }

    public static HttpResponse createOkResponse(HttpRequest request, HttpHeaders httpHeaders, byte[] bytes, MimeType contentType) {
        return new HttpResponse(request, HttpStatus.OK, httpHeaders, new HttpBody(bytes, contentType));
    }

    public static HttpResponse createOkResponse(HttpRequest request, HttpHeaders httpHeaders, byte[] bytes, MimeType contentType, HttpCookies cookies) {
        return new HttpResponse(request, HttpStatus.OK, httpHeaders, new HttpBody(bytes, contentType), cookies);
    }

    public static HttpResponse createErrorResponse(HttpRequest request) {
        HttpHeaders resHeaders = new HttpHeaders();

        return new HttpResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, resHeaders, new HttpBody(null, MimeType.NONE));
    }


    public static HttpResponse createNotFoundResponse(HttpRequest request) {
        HttpHeaders resHeaders = new HttpHeaders();

        return new HttpResponse(request, HttpStatus.NOT_FOUND, resHeaders, new HttpBody(null, MimeType.NONE));
    }

    public static HttpResponse createNoContentResponse(HttpRequest request) {
        HttpHeaders resHeaders = new HttpHeaders();

        return new HttpResponse(request, HttpStatus.NO_CONTENT, resHeaders, null);
    }

    public static HttpResponse createRedirectResponse(HttpRequest request, String location) {
        HttpHeaders resHeaders = new HttpHeaders();
        resHeaders.put("Location", location);

        return new HttpResponse(request, HttpStatus.FOUND, resHeaders, null);  // 302 Found
    }

    public static HttpResponse createIllegalArgumentResponse(HttpRequest request) {
        HttpHeaders httpHeaders = new HttpHeaders();

        return new HttpResponse(request, HttpStatus.ILLEGAL_ARGUMENT, httpHeaders, null);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("----response----").append(StringUtils.LINE_SEPERATOR);
        sb.append("status: ").append(status).append(StringUtils.LINE_SEPERATOR);
        sb.append("*headers*").append(StringUtils.LINE_SEPERATOR);
        sb.append(headers.toString());

        if (body != null && body.getBytes() != null) {
            sb.append("*body*").append(StringUtils.LINE_SEPERATOR);
            sb.append(body);
        }

        return sb.toString();
    }
}
