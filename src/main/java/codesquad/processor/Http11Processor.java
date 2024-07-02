package codesquad.processor;

import codesquad.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static codesquad.utils.StringUtils.LINE_SEPERATOR;

public class Http11Processor implements HttpProcessor {
    public static Logger logger = LoggerFactory.getLogger(Http11Processor.class);

    @Override
    public HttpRequest parseRequest(InputStream is) throws IOException {
        String[] startLineSplits = parseStartLine(is);
        HttpMethod method = HttpMethod.valueOf(startLineSplits[0]);
        String path = startLineSplits[1];
        HttpVersion httpVersion = HttpVersion.valueOf(startLineSplits[2]);

        String host = parseRequestLine(is);
        HttpHeaders headers = parseHeaders(is);

        //TODO parseBody 구현 필요
        return new HttpRequest(method, path, httpVersion, headers, new HttpBody(null));
    }

    @Override
    public void writeResponse(OutputStream os, HttpResponse response) throws IOException {
        writeStatusLine(os, response);
        writeHeaders(os, response);
        os.write(LINE_SEPERATOR.getBytes());
        writeBody(os, response);

        os.flush();
    }

    private void writeBody(OutputStream os, HttpResponse response) throws IOException {
        os.write(response.getBody().getBytes());
    }

    private void writeStatusLine(OutputStream os, HttpResponse response) {
        StringBuilder sb = new StringBuilder();
        HttpStatus status = response.getStatus();
        HttpVersion httpVersion = response.getRequest().getHttpVersion();

        sb.append(httpVersion.getRepresentation()).append(" ")
                .append(status.getStatusCode()).append(" ")
                .append(status.getMessage()).append(LINE_SEPERATOR);
    }

    private void writeHeaders(OutputStream os, HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();

        String contentType = response.getHeaders().get(HttpHeaders.CONTENT_TYPE);
        sb.append(HttpHeaders.CONTENT_TYPE).append(": ")
                .append(contentType).append(LINE_SEPERATOR);
        sb.append(LINE_SEPERATOR);

        os.write(sb.toString().getBytes());
    }

    private HttpHeaders parseHeaders(InputStream clientInput) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput));
        String line;

        while (!(line = reader.readLine()).isEmpty()) {
            String[] headerSplits = line.split(":", 2);
            headers.put(headerSplits[0], headerSplits[1]);
        }

        return headers;
    }

    private HttpBody parseBody(InputStream clientInput) throws IOException {
        //TODO
        return null;
    }

    private String[] parseStartLine(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String startLine = reader.readLine();

        return startLine.split(" ");
    }

    private String parseRequestLine(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String requestLine = reader.readLine();
        return requestLine.split(":", 2)[1];
    }
}
