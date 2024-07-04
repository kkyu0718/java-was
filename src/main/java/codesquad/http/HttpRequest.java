package codesquad.http;

import codesquad.global.Path;
import codesquad.utils.StringUtils;

public class HttpRequest {
    private HttpMethod method;
    private Path path;
    private HttpVersion httpVersion;
    private HttpHeaders headers;
    private HttpBody body;

    public HttpRequest(
            HttpMethod method,
            Path path,
            HttpVersion httpVersion,
            HttpHeaders headers,
            HttpBody body
    ) {
        this.method = method;
        this.path = path;
        this.httpVersion = httpVersion;
        this.headers = headers;
        this.body = body;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public Path getPath() {
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("----headers----").append(StringUtils.LINE_SEPERATOR);
        sb.append(headers.toString());
        sb.append("----body----").append(StringUtils.LINE_SEPERATOR);
//        sb.append(body.toString()); //TODO body 구현
        return sb.toString();
    }
}
