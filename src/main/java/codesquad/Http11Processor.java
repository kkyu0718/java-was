package codesquad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static codesquad.utils.StringUtils.LINE_SEPERATOR;

public class Http11Processor implements HttpProcessor {
    public static Logger logger = LoggerFactory.getLogger(Http11Processor.class);

    @Override
    public HttpRequest parseRequest(InputStream is) throws IOException {
        HttpHeaders headers = parseHeaders(is);

        //TODO parseBody 구현 필요
        return new HttpRequest(headers, new HttpBody(null));
    }

    @Override
    public void writeResponse(OutputStream os, HttpResponse response) throws IOException {
        writeHeaders(os, response);
        os.write(LINE_SEPERATOR.getBytes());
        writeBody(os, response);

        os.flush();
    }

    private void writeBody(OutputStream os, HttpResponse response) throws IOException {
        os.write(response.getBody().getBytes());
    }

    private void writeHeaders(OutputStream os, HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        HttpStatus status = response.getStatus();
        String httpVersion = response.getRequest().getHeaders().get(HttpHeaders.HTTP_VERSION);

        sb.append(httpVersion).append(" ").append(status.getStatusCode()).append(" ").append(status.getMessage()).append(LINE_SEPERATOR);

        String contentType = response.getHeaders().get(HttpHeaders.CONTENT_TYPE);
        sb.append(HttpHeaders.CONTENT_TYPE).append(": ").append(contentType).append(LINE_SEPERATOR);
        sb.append(LINE_SEPERATOR);

        os.write(sb.toString().getBytes());
    }

    private HttpHeaders parseHeaders(InputStream clientInput) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput));
        String line;

        parseStartLine(reader.readLine(), headers);
        parseRequestLine(reader.readLine(), headers);

        while (!(line = reader.readLine()).isEmpty()) {
            String[] headerSplits = line.split(":", 2);
            headers.put(headerSplits[0], headerSplits[1]);
        }

        return headers;
    }

    private HttpBody parseBody(InputStream clientInput) throws IOException {
        //TODO
    }

    private void parseStartLine(String startLine, HttpHeaders headers) throws IOException {
        String[] startLineSplits = startLine.split(" ");
        String requestMethod = startLineSplits[0];
        String requestTarget = startLineSplits[1];
        String httpVersion = startLineSplits[2];

        headers.put(HttpHeaders.HTTP_METHOD, requestMethod);
        headers.put(HttpHeaders.PATH, requestTarget);
        headers.put(HttpHeaders.HTTP_VERSION, httpVersion);
    }

    private void parseRequestLine(String requestLine, HttpHeaders headers) {
        String host = requestLine.split(":", 2)[1];
        headers.put("Host", host);
    }
}
