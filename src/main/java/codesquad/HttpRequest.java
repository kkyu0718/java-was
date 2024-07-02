package codesquad;

import codesquad.utils.StringUtils;

public class HttpRequest {
    private HttpHeaders headers;
    private HttpBody body;

    public HttpRequest(HttpHeaders headers, HttpBody body) {
        this.headers = headers;
        this.body = body;
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
//        sb.append(body.toString());
        return sb.toString();
    }
}
