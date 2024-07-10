package codesquad.processor;

import codesquad.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
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
    public HttpRequest parseRequest(BufferedReader br) throws IOException {
        String[] startLineSplits = parseStartLine(br);

        if (startLineSplits.length != 3) {
            throw new IllegalArgumentException("Invalid start line format");
        }

        HttpMethod method = HttpMethod.valueOf(startLineSplits[0]);
        String uri = startLineSplits[1];

        URI url = URI.create(uri);
        HttpVersion httpVersion = HttpVersion.fromRepresentation(startLineSplits[2]);

        HttpHeaders headers = parseHeaders(br);
        verifyMendatoryHeaders(headers);

        String query = url.getQuery();
        Parameters parameters = query != null ? Parameters.of(query) : null;

        HttpBody httpBody = parseBody(br, headers);

        HttpCookies cookies = new HttpCookies();
        // cookie 처리
        if (headers.contains("Cookie")) {
            String s = headers.get("Cookie");

            String[] splits = s.split(";");
            for (String split : splits) {
                String[] keyValue = split.split("=");
                cookies.setCookie(new HttpCookie(keyValue[0].trim(), keyValue[1].trim()));
            }
        }
        return new HttpRequest(method, url.getPath(), httpVersion, headers, httpBody, parameters, cookies);
    }

    private void verifyMendatoryHeaders(HttpHeaders headers) {
        if (!headers.contains("Connection") || !headers.contains("Host")) {
            throw new IllegalArgumentException("필수 헤더 Connection 과 Host 가 존재하지 않습니다.");
        }
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
        os.write(response.getBody() != null ? response.getBody().getBytes() : "0".getBytes());
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

    /*

   A recipient MUST parse an HTTP message as a sequence of octets in an
   encoding that is a superset of US-ASCII [USASCII].  Parsing an HTTP
   message as a stream of Unicode characters, without regard for the
   specific encoding, creates security vulnerabilities due to the
   varying ways that string processing libraries handle invalid
   multibyte character sequences that contain the octet LF (%x0A).
   String-based parsers can only be safely used within protocol elements
   after the element has been extracted from the message, such as within
   a header field-value after message parsing has delineated the
   individual fields.

   An HTTP message can be parsed as a stream for incremental processing
   or forwarding downstream.  However, recipients cannot rely on
   incremental delivery of partial messages, since some implementations
   will buffer or delay message forwarding for the sake of network
   efficiency, security checks, or payload transformations.
     */
    private HttpBody parseBody(BufferedReader br, HttpHeaders headers) throws IOException {
        if (!headers.contains(HttpHeaders.CONTENT_LENGTH)) {
            return null;
        }

        int contentLength = Integer.parseInt(headers.get(HttpHeaders.CONTENT_LENGTH));
        String contentType = headers.get(HttpHeaders.CONTENT_TYPE);

        if (contentLength == 0 || contentType == null) {
            return null;
        }

        MimeType mimeType = MimeType.fromMimeType(contentType);

        char[] bodyChars = new char[contentLength];
        int read = br.read(bodyChars, 0, contentLength);

        if (read != contentLength) {
            throw new IOException("Expected " + contentLength + " bytes but read " + read + " bytes.");
        }

        return new HttpBody(new String(bodyChars).getBytes(StandardCharsets.US_ASCII), mimeType);
    }

    private String[] parseStartLine(BufferedReader br) throws IOException {
        String startLine = br.readLine();
        if (startLine == null || startLine.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid start line: " + startLine);
        }
        return startLine.split(" ");
    }
}
