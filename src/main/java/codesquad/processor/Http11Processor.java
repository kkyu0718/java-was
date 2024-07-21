package codesquad.processor;

import codesquad.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import static codesquad.utils.StringUtils.LINE_SEPERATOR;

public class Http11Processor implements HttpProcessor {
    public static Logger logger = LoggerFactory.getLogger(Http11Processor.class);

    /*
         HTTP-message   = start-line CRLF
                      *( header-field CRLF )
                      CRLF
                      [ message-body ]

     */
    @Override
    public HttpRequest parseRequest(InputStream is) throws IOException {
        String[] startLineSplits = parseStartLine(is);

        if (startLineSplits.length != 3) {
            throw new IllegalArgumentException("Invalid start line format");
        }

        HttpMethod method = HttpMethod.valueOf(startLineSplits[0]);
        String uri = startLineSplits[1];

        URI url = URI.create(uri);
        HttpVersion httpVersion = HttpVersion.fromRepresentation(startLineSplits[2]);

        HttpHeaders headers = parseHeaders(is);
        verifyMendatoryHeaders(headers);

        String query = url.getQuery();
        Parameters parameters = Parameters.of(query);

        HttpBody httpBody = parseBody(is, headers);

        HttpCookies cookies = new HttpCookies();
        // cookie 처리
        if (headers.contains("Cookie")) {
            String s = headers.get("Cookie");

            String[] splits = s.split(";");
            for (String split : splits) {
                String[] keyValue = split.split("=");
                cookies.setCookie(new HttpCookie.Builder(keyValue[0].trim(), keyValue[1].trim()).build());
            }
        }

        return new HttpRequest.Builder(method, url.getPath(), httpVersion)
                .headers(headers)
                .body(httpBody)
                .parameters(parameters)
                .cookies(cookies)
                .build();
    }

    private void verifyMendatoryHeaders(HttpHeaders headers) {
        if (!headers.contains("Connection") || !headers.contains("Host")) {
            throw new IllegalArgumentException("필수 헤더 Connection 과 Host 가 존재하지 않습니다.");
        }
    }

    @Override
    public void writeResponse(OutputStream os, HttpResponse response) throws IOException {
        try (BufferedOutputStream bos = new BufferedOutputStream(os)) {
            writeStatusLine(os, response);
            writeHeaders(os, response);
            os.write(LINE_SEPERATOR.getBytes());

            writeBody(os, response);

            os.flush();
        }
    }

    private void writeBody(OutputStream os, HttpResponse response) throws IOException {
        os.write(!response.getBody().isEmpty() ? response.getBody().getBytes() : "0".getBytes());
    }

    private void writeStatusLine(OutputStream os, HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();
        HttpStatus status = response.getStatus();
        HttpVersion httpVersion = response.getRequest().getHttpVersion();

        sb.append(httpVersion.getRepresentation()).append(" ").append(status.getStatusCode()).append(" ").append(status.getMessage()).append(LINE_SEPERATOR);

        os.write(sb.toString().getBytes());
    }

    private void writeHeaders(OutputStream os, HttpResponse response) throws IOException {
        StringBuilder sb = new StringBuilder();

        for (String key : response.getHeaders().keySet()) {
            sb.append(key).append(": ").append(response.getHeaders().get(key)).append(LINE_SEPERATOR);
        }

        // write cookie
        HttpCookies httpCookies = response.getHttpCookies();
        for (HttpCookie cookie : httpCookies.getCookies()) {
            sb.append("Set-Cookie").append(": ").append(cookie.getName()).append("=").append(cookie.getValue()).append("; ")
                    .append("Path").append("=").append(cookie.getPath()).append(LINE_SEPERATOR);
        }

        os.write(sb.toString().getBytes());
    }

    private HttpHeaders parseHeaders(InputStream is) throws IOException {
        String headerPattern = "^[\\w-]+:\\s*.*\\s*$"; // 정규 표현식 패턴


        HttpHeaders headers = new HttpHeaders();
        StringBuilder buffer = new StringBuilder();
        int ch;
        boolean isEndOfHeaders = false;

        while (!isEndOfHeaders && (ch = is.read()) != -1) {
            if (ch == '\r') {
                ch = is.read();
                if (ch == '\n') {
                    if (buffer.length() == 0) {
                        isEndOfHeaders = true;
                    } else {
                        String headerLine = buffer.toString().trim();
                        //header-field   = field-name ":" OWS field-value OWS
                        if (!headerLine.matches(headerPattern)) {
                            throw new IllegalArgumentException("Invalid header field format: " + headerLine);
                        }
                        int colonIndex = headerLine.indexOf(':');
                        if (colonIndex > 0) {
                            String key = headerLine.substring(0, colonIndex).trim();
                            String value = headerLine.substring(colonIndex + 1).trim();
                            headers.put(key, value);
                        } else {
                            throw new IllegalArgumentException("Invalid header field format: " + headerLine);
                        }
                        buffer.setLength(0);
                    }
                } else {
                    buffer.append('\r');
                    if (ch != -1) {
                        buffer.append((char) ch);
                    }
                }
            } else {
                buffer.append((char) ch);
            }
        }

        return headers;
    }
    
    private HttpBody parseBody(InputStream is, HttpHeaders headers) throws IOException {
        if (!headers.contains(HttpHeaders.CONTENT_LENGTH)) {
            return HttpBody.empty();
        }

        int contentLength = Integer.parseInt(headers.get(HttpHeaders.CONTENT_LENGTH));
        String contentType = headers.get(HttpHeaders.CONTENT_TYPE);

        if (contentLength == 0 || contentType == null) {
            return HttpBody.empty();
        }

        MimeType mimeType = MimeType.fromMimeType(contentType);

        byte[] bodyBytes = new byte[contentLength];
        int totalRead = 0;
        while (totalRead < contentLength) {
            int read = is.read(bodyBytes, totalRead, contentLength - totalRead);
            if (read == -1) {
                break;
            }
            totalRead += read;
        }

        if (totalRead != contentLength) {
            throw new IOException("Expected " + contentLength + " bytes but read " + totalRead + " bytes.");
        }

        return HttpBody.of(bodyBytes, mimeType);
    }

    private String[] parseStartLine(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int previous = 0, current;
        while ((current = is.read()) != -1) {
            if (previous == '\r' && current == '\n') {
                break;
            }
            buffer.write(current);
            previous = current;
        }
        String startLine = new String(buffer.toByteArray(), StandardCharsets.UTF_8).trim();
        if (startLine.isEmpty()) {
            throw new IllegalArgumentException("Invalid start line: " + startLine);
        }
        return startLine.split(" ");
    }
}
