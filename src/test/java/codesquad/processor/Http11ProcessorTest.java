package codesquad.processor;

import codesquad.http.HttpHeaders;
import codesquad.http.HttpMethod;
import codesquad.http.HttpRequest;
import codesquad.http.HttpVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Http11ProcessorTest {
    HttpProcessor processor;

    @Test
    public void start_line가_주어졌을때_적절히_파싱을_할수있다() throws IOException {
        String requestString = "GET /index.html HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n";
        BufferedReader br = new BufferedReader(new StringReader(requestString));

        HttpRequest request = processor.parseRequest(br);

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/index.html", request.getPath().toString());
        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());
        assertEquals("localhost:8080", request.getHeaders().get(HttpHeaders.HOST));
    }

    @Test
    public void start_line가_주어졌을때_header가_주어지면_적절히_파싱을_할수있다() throws IOException {
        String requestString = "GET /index.html HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "Accept: */*\r\n" +
                "Accept-Encoding: gzip, deflate, br, zstd\r\n" +
                "Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7\r\n" +
                "Connection: keep-alive\r\n" +
                "Content-Length: 144\r\n" +
                "Content-Type: text/plain;charset=UTF-8\r\n\r\n";

        BufferedReader br = new BufferedReader(new StringReader(requestString));

        HttpRequest request = processor.parseRequest(br);

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/index.html", request.getPath().toString());
        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());
        assertEquals("localhost:8080", request.getHeaders().get(HttpHeaders.HOST));
        assertEquals(8, request.getHeaders().size());
        assertEquals("*/*", request.getHeaders().get("Accept"));
        assertEquals("gzip, deflate, br, zstd", request.getHeaders().get("Accept-Encoding"));
        assertEquals("ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7", request.getHeaders().get("Accept-Language"));
        assertEquals("keep-alive", request.getHeaders().get("Connection"));
        assertEquals("144", request.getHeaders().get("Content-Length"));
        assertEquals("text/plain;charset=UTF-8", request.getHeaders().get("Content-Type"));
    }

    @Test
    public void start_line가_주어졌을때_쿼리파라미터가_들어오면_적절히_파싱을_할수있다() throws IOException {
        String param1 = "p1";
        String param2 = "p2";

        String requestString = String.format("GET /index.html?param1=%s&param2=%s HTTP/1.1\r\n" +
                "Host: localhost:8080\r\n" +
                "\r\n", param1, param2);

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

    @BeforeEach
    void setup() {
        processor = new Http11Processor();
    }
}