package codesquad.http;

import codesquad.utils.StringUtils;

public class HttpRequest {
    private HttpMethod method;
    private String path;
    private HttpVersion httpVersion;
    private HttpHeaders headers;
    private HttpBody body;
    private Parameters parameters;

    public HttpRequest(
            HttpMethod method,
            String path,
            HttpVersion httpVersion,
            HttpHeaders headers,
            HttpBody body,
            Parameters parameters
    ) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.body = body;
        this.parameters = parameters;
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

    public boolean isFilePath() {
        return path.split("\\.").length == 2;
    }

    public String getExt() {
        if (isFilePath()) {
            return path.split("\\.")[1];
        }

        throw new IllegalArgumentException("확장자가 존재하지 않습니다. " + path);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("----request----").append(StringUtils.LINE_SEPERATOR);
        sb.append("*headers*").append(StringUtils.LINE_SEPERATOR);
        sb.append(headers.toString());

        if (body != null) {
            sb.append("*body*").append(StringUtils.LINE_SEPERATOR);
            sb.append(body); //TODO body 구현
        }

        return sb.toString();
    }
}
