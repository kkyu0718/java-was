package codesquad.http;

import codesquad.global.Path;
import codesquad.utils.StringUtils;

public class HttpRequest {
    private HttpMethod method;
    private Path path;
    private HttpVersion httpVersion;
    private HttpHeaders headers;
    private HttpBody body;
    private Parameters parameters;

    public HttpRequest(
            HttpMethod method,
            Path path,
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

    public Parameters getParameters() {
        return parameters;
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
