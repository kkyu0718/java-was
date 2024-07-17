package codesquad.processor;

import codesquad.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static codesquad.utils.StringUtils.LINE_SEPERATOR;
import static org.junit.jupiter.api.Assertions.*;

class Http11ProcessorTest {
    HttpProcessor processor;

    @Test
    public void 주어진_유효한_요청에서_스타트라인을_파싱하면_올바르게_파싱된다() throws IOException {
        String requestString = "GET /index.html HTTP/1.1" + LINE_SEPERATOR
                + "Host: localhost" + LINE_SEPERATOR
                + "Connection: keep-alive" + LINE_SEPERATOR
                + LINE_SEPERATOR
                + LINE_SEPERATOR;
        BufferedReader br = new BufferedReader(new StringReader(requestString));

        HttpRequest request = processor.parseRequest(br);

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/index.html", request.getPath().toString());
        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());
    }

    @Test
    public void 주어진_유효한_요청에서_헤더를_파싱하면_올바르게_파싱된다() throws IOException {
        String requestString = "GET /index.html HTTP/1.1" + LINE_SEPERATOR
                + "Host: localhost:8080" + LINE_SEPERATOR
                + "Accept: */*" + LINE_SEPERATOR
                + "Accept-Encoding: gzip, deflate, br, zstd" + LINE_SEPERATOR
                + "Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7" + LINE_SEPERATOR
                + "Connection: keep-alive" + LINE_SEPERATOR
                + "Content-Length: 0" + LINE_SEPERATOR
                + "Content-Type: text/plain;charset=UTF-8" + LINE_SEPERATOR
                + LINE_SEPERATOR;

        BufferedReader br = new BufferedReader(new StringReader(requestString));

        HttpRequest request = processor.parseRequest(br);

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/index.html", request.getPath().toString());
        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());
        assertEquals("localhost:8080", request.getHeaders().get(HttpHeaders.HOST));
        assertEquals(7, request.getHeaders().size());
        assertEquals("*/*", request.getHeaders().get("Accept"));
        assertEquals("gzip, deflate, br, zstd", request.getHeaders().get("Accept-Encoding"));
        assertEquals("ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7", request.getHeaders().get("Accept-Language"));
        assertEquals("keep-alive", request.getHeaders().get("Connection"));
        assertEquals("0", request.getHeaders().get("Content-Length"));
        assertEquals("text/plain;charset=UTF-8", request.getHeaders().get("Content-Type"));
    }

    @Test
    public void 주어진_유효한_요청에서_쿼리파라미터를_파싱하면_올바르게_파싱된다() throws IOException {
        String param1 = "p1";
        String param2 = "p2";

        String requestString = String.format("GET /index.html?param1=%s&param2=%s HTTP/1.1" + LINE_SEPERATOR + "Host: localhost:8080" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + LINE_SEPERATOR, param1, param2);

        BufferedReader br = new BufferedReader(new StringReader(requestString));

        HttpRequest request = processor.parseRequest(br);

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/index.html", request.getPath().toString());
        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());
        assertEquals("localhost:8080", request.getHeaders().get(HttpHeaders.HOST));
        assertEquals(2, request.getParameters().size());
        assertEquals(param1, request.getParameters().getParameter("param1"));
        assertEquals(param2, request.getParameters().getParameter("param2"));
    }

    @Test
    public void 헤더에_세미콜론이_누락된_경우_400_에러가_발생한다() {
        String headerString = "Host localhost" + LINE_SEPERATOR // ':' 누락
                + "Content-Type: text/html" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + LINE_SEPERATOR;
        BufferedReader br = new BufferedReader(new StringReader("GET /index.html HTTP/1.1\r\n" + headerString));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(br);
        });
    }

    @Test
    public void 헤더의_키와_세미콜론_사이에_공백이_존재하는_경우_400_에러가_발생한다() {
        String headerString = "Host : localhost" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + LINE_SEPERATOR;
        BufferedReader br = new BufferedReader(new StringReader("GET /index.html HTTP/1.1\r\n" + headerString));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(br);
        });
    }

    @ParameterizedTest
    @CsvSource({"Host:localhost", "Host:localhost", "Host: localhost", "Host: localhost "})
    public void 헤더의_값_양옆에_공백이_존재하는_경우_적절히_파싱된다(String header) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader("GET /index.html HTTP/1.1" + LINE_SEPERATOR + header + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + LINE_SEPERATOR));

        HttpRequest httpRequest = processor.parseRequest(br);
        HttpHeaders headers = httpRequest.getHeaders();
        assertEquals("localhost", headers.get("Host"));
    }


    @Test
    void 중복되는_헤더가_존재하는_경우_뒤에_오는_값으로_덮어쓴다() throws IOException {
        String requestString = "GET /index.html HTTP/1.1" + LINE_SEPERATOR + "Host: localhost:8080" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + "header1: a" + LINE_SEPERATOR + "header1: b" + LINE_SEPERATOR + LINE_SEPERATOR;
        ;
        BufferedReader reader = new BufferedReader(new StringReader(requestString));

        HttpRequest request = processor.parseRequest(reader);

        assertNotNull(request);
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());
        assertEquals("b", request.getHeaders().get("header1"));
    }

    @Test
    void 중복되는_파라미터가_존재하는_경우_뒤에_오는_값으로_덮어쓴다() throws IOException {
        String requestString = "GET /index.html?param1=a&param1=b HTTP/1.1" + LINE_SEPERATOR + "Host: localhost:8080" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + LINE_SEPERATOR;
        BufferedReader reader = new BufferedReader(new StringReader(requestString));

        HttpRequest request = processor.parseRequest(reader);

        assertNotNull(request);
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());
        assertEquals("b", request.getParameters().getParameter("param1"));
    }

    @Test
    void 시작_라인에_HTTP_버전이_없는_경우_예외가_발생한다() {
        String invalidStartLine = "GET http://example.com/path/to/resource" + "Host: localhost:8080" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR; // HTTP 버전 없음
        BufferedReader reader = new BufferedReader(new StringReader(invalidStartLine));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(reader);
        });
    }

    @Test
    void 시작_라인에_HTTP_메서드가_없는_경우_예외가_발생한다() {
        String invalidStartLine = "/path/to/resource HTTP/1.1" + LINE_SEPERATOR
                + "Host: localhost:8080" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR; // 메서드 없음
        BufferedReader reader = new BufferedReader(new StringReader(invalidStartLine));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(reader);
        });
    }

    @Test
    void Host헤더와_Connection혜더는_항상_존재해야한다() {
        String validStartLine = "GET /path/to/resource HTTP/1.1" + LINE_SEPERATOR
                + "Host: localhost:8080" + LINE_SEPERATOR
                + "Connection: keep-alive" + LINE_SEPERATOR
                + LINE_SEPERATOR;
        BufferedReader reader = new BufferedReader(new StringReader(validStartLine));

        assertDoesNotThrow(() -> {
            processor.parseRequest(reader);
        });
    }

    @Test
    void Host헤더가_존재하지않으면_400에러를_던진다() {
        String validStartLine = "/path/to/resource HTTP/1.1" + LINE_SEPERATOR
                + "Connection: keep-alive" + LINE_SEPERATOR;
        BufferedReader reader = new BufferedReader(new StringReader(validStartLine));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(reader);
        });
    }

    @Test
    void Connection헤더가_존재하지않으면_400에러를_던진다() {
        String validStartLine = "/path/to/resource HTTP/1.1" + "Host: localhost:8080" + LINE_SEPERATOR;
        BufferedReader reader = new BufferedReader(new StringReader(validStartLine));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(reader);
        });
    }

    @Test
    void 시작라인O_헤더O_바디X로_존재하는_경우_파싱하면_올바르게_파싱된다() throws IOException {
        String requestString = "GET /path/to/resource HTTP/1.1" + LINE_SEPERATOR
                + "Host: localhost:8080" + LINE_SEPERATOR
                + "Connection: keep-alive" + LINE_SEPERATOR
                + LINE_SEPERATOR
                + LINE_SEPERATOR;
        ;
        BufferedReader reader = new BufferedReader(new StringReader(requestString));

        HttpRequest request = processor.parseRequest(reader);
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/path/to/resource", request.getPath());
        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());

        assertNotNull(request);
        assertEquals(2, request.getHeaders().size());
        assertEquals("keep-alive", request.getHeaders().get("Connection"));
        assertEquals("localhost:8080", request.getHeaders().get("Host"));

        assertTrue(request.getBody().isEmpty());
    }

    @Test
    void 시작라인O_헤더O_바디O로_존재하는_경우_파싱하면_올바르게_파싱된다() throws IOException {
        String body = "body body body";
        String requestString = "GET /path/to/resource HTTP/1.1" + LINE_SEPERATOR
                + "Host: localhost:8080" + LINE_SEPERATOR
                + "Connection: keep-alive" + LINE_SEPERATOR
                + "Content-Length: " + body.getBytes().length + LINE_SEPERATOR
                + "Content-Type: " + "text/html" + LINE_SEPERATOR
                + LINE_SEPERATOR
                + body + LINE_SEPERATOR;

        BufferedReader reader = new BufferedReader(new StringReader(requestString));

        HttpRequest request = processor.parseRequest(reader);
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/path/to/resource", request.getPath());
        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());

        assertNotNull(request);
        assertEquals(4, request.getHeaders().size());
        assertEquals("keep-alive", request.getHeaders().get("Connection"));
        assertEquals("localhost:8080", request.getHeaders().get("Host"));
        assertEquals("body body body".getBytes().length, Integer.parseInt(request.getHeaders().get(HttpHeaders.CONTENT_LENGTH)));
        assertEquals(MimeType.HTML.getMimeType(), request.getHeaders().get(HttpHeaders.CONTENT_TYPE));

        assertFalse(request.getBody().isEmpty());
    }

    @Test
    void 시작라인O_헤더O_바디가_json으로_존재하는_경우_파싱하면_올바르게_파싱된다() throws IOException {
        String body = "{\"content\":\"규원안녕\"}";
        String requestString = "GET /path/to/resource HTTP/1.1" + LINE_SEPERATOR
                + "Host: localhost:8080" + LINE_SEPERATOR
                + "Connection: keep-alive" + LINE_SEPERATOR
                + "Content-Length: " + body.getBytes().length + LINE_SEPERATOR
                + "Content-Type: " + "application/json" + LINE_SEPERATOR
                + LINE_SEPERATOR
                + body + LINE_SEPERATOR;

        BufferedReader reader = new BufferedReader(new StringReader(requestString));

        HttpRequest request = processor.parseRequest(reader);
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/path/to/resource", request.getPath());
        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());

        assertNotNull(request);
        assertEquals(4, request.getHeaders().size());
        assertEquals("keep-alive", request.getHeaders().get("Connection"));
        assertEquals("localhost:8080", request.getHeaders().get("Host"));
        assertEquals(body.getBytes().length, Integer.parseInt(request.getHeaders().get(HttpHeaders.CONTENT_LENGTH)));
        assertEquals(MimeType.APPLICATION_JSON.getMimeType(), request.getHeaders().get(HttpHeaders.CONTENT_TYPE));

        assertFalse(request.getBody().isEmpty());
    }

    @Test
    public void 응답을_작성하면_올바르게_작성된다() throws IOException {
        // given
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/index.html", HttpVersion.HTTP11).build();
        HttpResponse response = new HttpResponse.Builder(request, HttpStatus.OK)
                .header("Content-Type", "text/html")
                .header("Content-Length", "20")
                .body(HttpBody.of("Hello, world!".getBytes(), MimeType.HTML))
                .build();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // when
        processor.writeResponse(outputStream, response);

        // then
        String expectedResponse = "HTTP/1.1 200 OK" + LINE_SEPERATOR +
                "Content-Length: 20" + LINE_SEPERATOR +
                "Content-Type: text/html" + LINE_SEPERATOR +
                LINE_SEPERATOR +
                "Hello, world!";
        assertEquals(expectedResponse, outputStream.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void 응답에_쿠키를_포함하면_올바르게_작성된다() throws IOException {
        // given
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/index.html", HttpVersion.HTTP11).build();
        HttpResponse response = new HttpResponse.Builder(request, HttpStatus.OK)
                .header("Content-Type", "text/html")
                .header("Content-Length", "20")
                .cookie(new HttpCookie.Builder("sessionId", "1234567890").path("/").build())
                .body(HttpBody.of("Hello, world!".getBytes(StandardCharsets.UTF_8), MimeType.HTML))
                .build();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // when
        processor.writeResponse(outputStream, response);

        // then
        String expectedResponse = "HTTP/1.1 200 OK" + LINE_SEPERATOR +
                "Content-Length: 20" + LINE_SEPERATOR +
                "Content-Type: text/html" + LINE_SEPERATOR +
                "Set-Cookie: sessionId=1234567890; Path=/" + LINE_SEPERATOR +
                LINE_SEPERATOR +
                "Hello, world!";
        assertEquals(expectedResponse, outputStream.toString(StandardCharsets.UTF_8));
    }

    @BeforeEach
    void setup() {
        processor = new Http11Processor();
    }
}