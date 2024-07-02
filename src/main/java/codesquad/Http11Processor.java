package codesquad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class Http11Processor implements HttpProcessor {
    public static Logger logger = LoggerFactory.getLogger(Http11Processor.class);

    @Override
    public HttpRequest parseRequest(InputStream is) throws IOException {
        HttpHeaders headers = parseHeaders(is);

        //TODO parseBody 구현 필요
        return new HttpRequest(headers, new HttpBody());
    }

    @Override
    public void createResponse(OutputStream os, HttpResponse response) throws IOException {
        os.write("HTTP/1.1 200 OK\r\n".getBytes());
        os.write("Content-Type: text/html\r\n".getBytes());
        os.write("\r\n".getBytes());
        os.write("<h1>Hello</h1>\r\n".getBytes()); // 응답 본문으로 "Hello"를 보냅니다.
        os.flush();
    }

    private HttpHeaders parseHeaders(InputStream clientInput) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput));
        String line;

        parseStartLine(reader.readLine(), headers);
        parseRequestLine(reader.readLine(), headers);

        while (!(line = reader.readLine()).isEmpty()) { // 빈 줄이 나올 때까지 읽습니다.
            String[] headerSplits = line.split(":", 2);
            headers.put(headerSplits[0], headerSplits[1]);
        }

        return headers;
    }

    private void parseStartLine(String startLine, HttpHeaders headers) throws IOException {
        String[] startLineSplits = startLine.split(" ");
        String requestMethod = startLineSplits[0];
        String requestTarget = startLineSplits[1];
        String httpVersion = startLineSplits[2];

        headers.put("HttpMethod", requestMethod);
        headers.put("Path", requestTarget);
        headers.put("HttpVersion", httpVersion);
    }

    private void parseRequestLine(String requestLine, HttpHeaders headers) {
        String host = requestLine.split(":", 2)[1];
        headers.put("Host", host);
    }
}
