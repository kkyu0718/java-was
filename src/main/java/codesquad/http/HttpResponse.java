package codesquad.http;

import codesquad.utils.StringUtils;

public class HttpResponse {
    private HttpRequest request;
    private HttpStatus status;
    private HttpHeaders headers;
    private HttpBody body;
    private HttpCookies httpCookies;

    private HttpResponse(Builder builder) {
        this.request = builder.request;
        this.status = builder.status;
        this.headers = builder.headers;
        this.body = builder.body;
        this.httpCookies = builder.httpCookies;
    }

    public static class Builder {
        private final HttpRequest request;
        private final HttpStatus status;
        private HttpHeaders headers;
        private HttpBody body;
        private HttpCookies httpCookies;

        public Builder(HttpRequest request, HttpStatus status) {
            this.request = request;
            this.status = status;
            this.headers = new HttpHeaders();
            this.body = HttpBody.empty();
            this.httpCookies = new HttpCookies();
        }

        public HttpResponse.Builder headers(HttpHeaders headers) {
            this.headers.extend(headers);
            return this;
        }

        public HttpResponse.Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder body(HttpBody body) {
            this.body = body;
            return this;
        }

        public HttpResponse.Builder cookies(HttpCookies cookies) {
            this.httpCookies.extend(cookies);
            return this;
        }

        public HttpResponse.Builder cookie(HttpCookie cookie) {
            this.httpCookies.setCookie(cookie);
            return this;
        }

        public HttpResponse.Builder redirect(String path) {
            this.headers.put("Location", path);
            return this;
        }

        public HttpResponse build() {
            return new HttpResponse(this);
        }
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

//    public static HttpResponse createOkResponse(HttpRequest request, HttpHeaders httpHeaders, byte[] bytes, MimeType contentType) {
//        return new HttpResponse(request, HttpStatus.OK, httpHeaders, new HttpBody(bytes, contentType));
//    }
//
//    public static HttpResponse createOkResponse(HttpRequest request, HttpHeaders httpHeaders, byte[] bytes, MimeType contentType, HttpCookies cookies) {
//        return new HttpResponse(request, HttpStatus.OK, httpHeaders, new HttpBody(bytes, contentType), cookies);
//    }
//
//    public static HttpResponse createErrorResponse(HttpRequest request) {
//        HttpHeaders resHeaders = new HttpHeaders();
//
//        return new HttpResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, resHeaders, new HttpBody(null, MimeType.NONE));
//    }
//
//
//    public static HttpResponse createNotFoundResponse(HttpRequest request) {
//        HttpHeaders resHeaders = new HttpHeaders();
//
//        return new HttpResponse(request, HttpStatus.NOT_FOUND, resHeaders, new HttpBody(null, MimeType.NONE));
//    }
//
//    public static HttpResponse createNoContentResponse(HttpRequest request) {
//        HttpHeaders resHeaders = new HttpHeaders();
//
//        return new HttpResponse(request, HttpStatus.NO_CONTENT, resHeaders, null);
//    }
//
//    public static HttpResponse createRedirectResponse(HttpRequest request, String location) {
//        HttpHeaders resHeaders = new HttpHeaders();
//        resHeaders.put("Location", location);
//
//        return new HttpResponse(request, HttpStatus.FOUND, resHeaders, null);  // 302 Found
//    }
//
//    public static HttpResponse createIllegalArgumentResponse(HttpRequest request) {
//        HttpHeaders httpHeaders = new HttpHeaders();
//
//        return new HttpResponse(request, HttpStatus.ILLEGAL_ARGUMENT, httpHeaders, null);
//    }

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
