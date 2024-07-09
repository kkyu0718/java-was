package codesquad.processor;

import codesquad.http.HttpHeaders;
import codesquad.http.HttpMethod;
import codesquad.http.HttpRequest;
import codesquad.http.HttpVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static codesquad.utils.StringUtils.LINE_SEPERATOR;
import static org.junit.jupiter.api.Assertions.*;

class Http11ProcessorTest {
    HttpProcessor processor;

    @Test
    public void request가_주어지면_start_line를_적절히_파싱을_할수있다() throws IOException {
        String requestString = "GET /index.html HTTP/1.1" + "Host : localhost" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + LINE_SEPERATOR + LINE_SEPERATOR;
        BufferedReader br = new BufferedReader(new StringReader(requestString));

        HttpRequest request = processor.parseRequest(br);

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/index.html", request.getPath().toString());
        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());
    }

    @Test
    public void request가_주어지면_start_line과_적절한_규약을_지키는_헤더를_적절히_파싱을_할수있다() throws IOException {
        String requestString = "GET /index.html HTTP/1.1" + LINE_SEPERATOR + "Host: localhost:8080" + LINE_SEPERATOR + "Accept: */*" + LINE_SEPERATOR + "Accept-Encoding: gzip, deflate, br, zstd" + LINE_SEPERATOR + "Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + "Content-Length: 144" + LINE_SEPERATOR + "Content-Type: text/plain;charset=UTF-8" + LINE_SEPERATOR + LINE_SEPERATOR;

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
        assertEquals("144", request.getHeaders().get("Content-Length"));
        assertEquals("text/plain;charset=UTF-8", request.getHeaders().get("Content-Type"));
    }

    @Test
    public void request가_주어지면_start_line과_쿼리파라미터를_적절히_파싱을_할수있다() throws IOException {
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
    public void request가_주어지면_헤더에_세미콜론이_생략된경우_400에러가_발생한다() {
        String headerString = "Host localhost" + LINE_SEPERATOR // ':' 누락
                + "Content-Type: text/html" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + LINE_SEPERATOR;
        BufferedReader br = new BufferedReader(new StringReader("GET /index.html HTTP/1.1\r\n" + headerString));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(br);
        });
    }

    @Test
    public void request가_주어지면_헤더의_key와_세미콜론_사이에_공백이_존재하는경우_400에러가_발생한다() {
        String headerString = "Host : localhost" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + LINE_SEPERATOR;
        BufferedReader br = new BufferedReader(new StringReader("GET /index.html HTTP/1.1\r\n" + headerString));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(br);
        });
    }

    @ParameterizedTest
    @CsvSource({"Host:localhost", "Host:localhost", "Host: localhost", "Host: localhost "})
    public void request가_주어지면_헤더의_value양옆으로_옵션공백이_존재하는경우_적절히_파싱된다(String header) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader("GET /index.html HTTP/1.1" + LINE_SEPERATOR + header + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + LINE_SEPERATOR));

        HttpRequest httpRequest = processor.parseRequest(br);
        HttpHeaders headers = httpRequest.getHeaders();
        assertEquals("localhost", headers.get("Host"));
    }

//    @Test
//    void 유효한_URL_검증() throws IOException {
//        String validRequest = "GET http://example.com/path/to/resource HTTP/1.1\r\nHost: example.com\r\n\r\n";
//        BufferedReader reader = new BufferedReader(new StringReader(validRequest));
//
//        HttpRequest request = processor.parseRequest(reader);
//
//        assertNotNull(request);
//        assertEquals(HttpMethod.GET, request.getMethod());
//        assertEquals("/path/to/resource", request.getPath());
//        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());
//    }

    @Test
    void 중복되는_header가_오는_경우_뒤에_오는_것으로_덮어씌워진다() throws IOException {
        String requestString = "GET /index.html HTTP/1.1" + LINE_SEPERATOR + "Host: localhost:8080" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + "header1: a" + LINE_SEPERATOR + "header1: b" + LINE_SEPERATOR + LINE_SEPERATOR;
        ;
        BufferedReader reader = new BufferedReader(new StringReader(requestString));

        HttpRequest request = processor.parseRequest(reader);

        assertNotNull(request);
        assertEquals(HttpMethod.GET, request.getMethod());
//        Assertions.assertEquals("/path/to/resource", request.getPath());
        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());
        assertEquals("b", request.getHeaders().get("header1"));
    }

    @Test
    void 중복되는_parameter가_오는_경우_뒤에_오는_것으로_덮어씌워진다() throws IOException {
        String requestString = "GET /index.html?param1=a&param1=b HTTP/1.1" + LINE_SEPERATOR + "Host: localhost:8080" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + LINE_SEPERATOR;
        BufferedReader reader = new BufferedReader(new StringReader(requestString));

        HttpRequest request = processor.parseRequest(reader);

        assertNotNull(request);
        assertEquals(HttpMethod.GET, request.getMethod());
//        Assertions.assertEquals("/path/to/resource", request.getPath());
        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());
        assertEquals("b", request.getParameters().getParameter("param1"));
    }

    @Test
    void 시작_라인에_HTTP_버전이_없는_경우_검증() {
        String invalidStartLine = "GET http://example.com/path/to/resource" + "Host: localhost:8080" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR; // HTTP 버전 없음
        BufferedReader reader = new BufferedReader(new StringReader(invalidStartLine));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(reader);
        });
    }

    @Test
    void 시작_라인에_URI가_없는_경우_검증() {
        String invalidStartLine = "GET /path/to/resource HTTP/1.1" + LINE_SEPERATOR
                + "Host: localhost:8080" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR; // URI 없음
        BufferedReader reader = new BufferedReader(new StringReader(invalidStartLine));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(reader);
        });
    }

    @Test
    void 시작_라인에_HTTP_메서드가_없는_경우_검증() {
        String invalidStartLine = "/path/to/resource HTTP/1.1" + LINE_SEPERATOR
                + "Host: localhost:8080" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR; // 메서드 없음
        BufferedReader reader = new BufferedReader(new StringReader(invalidStartLine));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(reader);
        });
    }

    @Test
    void Host헤더와_Connection혜더는_항상_존재해야한다() {
        String validStartLine = "GET /path/to/resource HTTP/1.1" + "Host: localhost:8080" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR;
        BufferedReader reader = new BufferedReader(new StringReader(validStartLine));

        assertDoesNotThrow(() -> {
            processor.parseRequest(reader);
        });
    }

    @Test
    void Host헤더가_존재하지않으면_에러를_던진다() {
        String validStartLine = "/path/to/resource HTTP/1.1" + "Connection: keep-alive" + LINE_SEPERATOR;
        BufferedReader reader = new BufferedReader(new StringReader(validStartLine));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(reader);
        });
    }

    @Test
    void Connection헤더가_존재하지않으면_에러를_던진다() {
        String validStartLine = "/path/to/resource HTTP/1.1" + "Host: localhost:8080" + LINE_SEPERATOR;
        BufferedReader reader = new BufferedReader(new StringReader(validStartLine));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(reader);
        });
    }

    @Test
    void 시작라인O_헤더O_바디X() throws IOException {
        String requestString = "GET /path/to/resource HTTP/1.1" + LINE_SEPERATOR + "Host: localhost:8080" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + LINE_SEPERATOR + LINE_SEPERATOR;
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

        assertNull(request.getBody());
    }

    @Test
    void 시작라인O_헤더O_바디O() throws IOException {
        String requestString = "GET /path/to/resource HTTP/1.1" + LINE_SEPERATOR + "Host: localhost:8080" + LINE_SEPERATOR + "Connection: keep-alive" + LINE_SEPERATOR + LINE_SEPERATOR + "body body body" + LINE_SEPERATOR;
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

        assertNotNull(request.getBody());
    }

    @BeforeEach
    void setup() {
        processor = new Http11Processor();
    }
}