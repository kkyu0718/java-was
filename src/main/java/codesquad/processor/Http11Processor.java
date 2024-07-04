package codesquad.processor;

import codesquad.global.Url;
import codesquad.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;

import static codesquad.utils.StringUtils.LINE_SEPERATOR;

public class Http11Processor implements HttpProcessor {
    public static Logger logger = LoggerFactory.getLogger(Http11Processor.class);

    @Override
    public HttpRequest parseRequest(BufferedReader br) throws IOException {
        String[] startLineSplits = parseStartLine(br);
        HttpMethod method = HttpMethod.valueOf(startLineSplits[0]);
        Url url = Url.of(startLineSplits[1]);
        HttpVersion httpVersion = HttpVersion.fromRepresentation(startLineSplits[2]);

        HttpHeaders headers = parseHeaders(br);
        headers.put(HttpHeaders.PATH, url.getPath().toString());

        //TODO body 가 존재하는 경우 parse 로직 필요
        // content-length 에 따른 로직 구현 필요
        // trasfer-encoding 이 chunked 인 경우 구현 필요
        return new HttpRequest(method, url.getPath(), httpVersion, headers, new HttpBody(null), url.getParameters());
    }

    @Override
    public void writeResponse(OutputStream os, HttpResponse response) throws IOException {
        writeStatusLine(os, response);
        writeHeaders(os, response);
        os.write(LINE_SEPERATOR.getBytes());

        if (response.getBody() != null) {
            writeBody(os, response);
        }

        os.flush();
    }

    private void writeBody(OutputStream os, HttpResponse response) throws IOException {
        os.write(response.getBody().getBytes());
    }

    private void writeStatusLine(OutputStream os, HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        HttpStatus status = response.getStatus();
        HttpVersion httpVersion = response.getRequest().getHttpVersion();

        sb.append(httpVersion.getRepresentation()).append(" ")
                .append(status.getStatusCode()).append(" ")
                .append(status.getMessage()).append(LINE_SEPERATOR);

        os.write(sb.toString().getBytes());
    }

    private void writeHeaders(OutputStream os, HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();

        if (response.getBody() != null) {
            String contentType = response.getHeaders().get(HttpHeaders.CONTENT_TYPE);
            sb.append(HttpHeaders.CONTENT_TYPE).append(": ")
                    .append(contentType).append(LINE_SEPERATOR)
                    .append(HttpHeaders.CONTENT_LENGTH).append(": ")
                    .append(response.getBody().getBytes().length);
        }
        sb.append(LINE_SEPERATOR);

        os.write(sb.toString().getBytes());
    }

    private HttpHeaders parseHeaders(BufferedReader br) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        String line;
        String headerPattern = "^[\\w-]+:\\s*.*\\s*$"; // 정규 표현식 패턴

        //header-field   = field-name ":" OWS field-value OWS
        while (!(line = br.readLine()).isEmpty()) {
            if (!line.matches(headerPattern)) {
                throw new IllegalArgumentException("Invalid header field format: " + line);
            }
            String[] headerSplits = line.split(":", 2);
            headers.put(headerSplits[0], headerSplits[1].trim());
        }

        return headers;
    }

    private HttpBody parseBody(BufferedReader br) throws IOException {

        return null;
    }

    private String[] parseStartLine(BufferedReader br) throws IOException {
        String startLine = br.readLine();

        return startLine.split(" ");
    }
}
