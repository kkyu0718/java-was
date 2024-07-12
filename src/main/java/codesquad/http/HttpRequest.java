package codesquad.http;

import codesquad.utils.StringUtils;

public class HttpRequest {
    private HttpMethod method;
    private String path;
    private HttpVersion httpVersion;
    private HttpHeaders headers;
    private HttpBody body;
    private Parameters parameters;
    private HttpCookies httpCookies;

    private HttpRequest(Builder builder) {
        this.method = builder.method;
        this.path = builder.path;
        this.httpVersion = builder.httpVersion;
        this.headers = builder.headers;
        this.body = builder.body;
        this.parameters = builder.parameters;
        this.httpCookies = builder.httpCookies;
    }

    public static class Builder {
        private HttpMethod method;
        private String path;
        private HttpVersion httpVersion;
        private HttpHeaders headers;
        private HttpBody body;
        private Parameters parameters;
        private HttpCookies httpCookies;

        public Builder(HttpMethod method, String path, HttpVersion httpVersion) {
            this.method = method;
            this.path = path;
            this.httpVersion = httpVersion;
            this.body = HttpBody.empty();
            this.headers = new HttpHeaders();
            this.parameters = new Parameters();
            this.httpCookies = new HttpCookies();
        }

        public Builder headers(HttpHeaders headers) {
            this.headers.extend(headers);
            return this;
        }

        public Builder header(String key, String value) {
            this.headers.put(key, value);
            return this;
        }

        public Builder body(HttpBody body) {
            this.body = body;
            return this;
        }

        public Builder parameters(Parameters parameters) {
            this.parameters.extend(parameters);
            return this;
        }

        public Builder parameter(String key, String value) {
            this.parameters.addParameter(key, value);
            return this;
        }

        public Builder cookies(HttpCookies cookies) {
            this.httpCookies.extend(cookies);
            return this;
        }

        public Builder cookie(HttpCookie cookie) {
            this.httpCookies.setCookie(cookie);
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(this);
        }
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public HttpBody getBody() {
        return body;
    }

    public HttpVersion getHttpVersion() {
        return httpVersion;
    }

    public HttpHeaders getHeaders() {
        return this.headers;
    }

    public Parameters getParameters() {
        return parameters;
    }

    public HttpCookies getHttpCookies() {
        return httpCookies;
    }

    public boolean isFilePath() {
        return path.split("\\.").length == 2;
    }

    public String getExt() {
        if (isFilePath()) {
            return path.split("\\.")[1];
        }

        throw new IllegalArgumentException("확장자가 존재하지 않습니다. " + path);
    }

    public String getHeader(String key) {
        return this.getHeaders().get(key);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("----request----").append(StringUtils.LINE_SEPERATOR);
        sb.append(method + " " + path).append(StringUtils.LINE_SEPERATOR);
        sb.append("*headers*").append(StringUtils.LINE_SEPERATOR);
        sb.append(headers.toString());

//        if (!body.isEmpty()) {
//            sb.append("*body*").append(StringUtils.LINE_SEPERATOR);
//            sb.append(body);
//        }

        return sb.toString();
    }
}
