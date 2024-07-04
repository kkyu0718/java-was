package codesquad.processor;

import codesquad.http.HttpHeaders;
import codesquad.http.HttpMethod;
import codesquad.http.HttpRequest;
import codesquad.http.HttpVersion;
import codesquad.utils.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Http11ProcessorTest {
    HttpProcessor processor;

    @Test
    public void request가_주어지면_start_line를_적절히_파싱을_할수있다() throws IOException {
        String requestString = "GET /index.html HTTP/1.1" + StringUtils.LINE_SEPERATOR
                + StringUtils.LINE_SEPERATOR;
        BufferedReader br = new BufferedReader(new StringReader(requestString));

        HttpRequest request = processor.parseRequest(br);

        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/index.html", request.getPath().toString());
        assertEquals(HttpVersion.HTTP11, request.getHttpVersion());
    }

    @Test
    public void request가_주어지면_start_line과_적절한_규약을_지키는_헤더를_적절히_파싱을_할수있다() throws IOException {
        String requestString = "GET /index.html HTTP/1.1" + StringUtils.LINE_SEPERATOR +
                "Host: localhost:8080" + StringUtils.LINE_SEPERATOR +
                "Accept: */*" + StringUtils.LINE_SEPERATOR +
                "Accept-Encoding: gzip, deflate, br, zstd" + StringUtils.LINE_SEPERATOR +
                "Accept-Language: ko-KR,ko;q=0.9,en-US;q=0.8,en;q=0.7" + StringUtils.LINE_SEPERATOR +
                "Connection: keep-alive" + StringUtils.LINE_SEPERATOR +
                "Content-Length: 144" + StringUtils.LINE_SEPERATOR +
                "Content-Type: text/plain;charset=UTF-8" + StringUtils.LINE_SEPERATOR
                + StringUtils.LINE_SEPERATOR;

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
    public void request가_주어지면_start_line과_쿼리파라미터를_적절히_파싱을_할수있다() throws IOException {
        String param1 = "p1";
        String param2 = "p2";

        String requestString = String.format("GET /index.html?param1=%s&param2=%s HTTP/1.1" + StringUtils.LINE_SEPERATOR
                + "Host: localhost:8080" + StringUtils.LINE_SEPERATOR
                + StringUtils.LINE_SEPERATOR, param1, param2);

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
        String headerString = "Host localhost" + StringUtils.LINE_SEPERATOR // ':' 누락
                + "Content-Type: text/html" + StringUtils.LINE_SEPERATOR
                + StringUtils.LINE_SEPERATOR;
        BufferedReader br = new BufferedReader(new StringReader("GET /index.html HTTP/1.1\r\n" + headerString));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(br);
        });
    }

    @Test
    public void request가_주어지면_헤더의_key와_세미콜론_사이에_공백이_존재하는경우_400에러가_발생한다() {
        String headerString = "Host : localhost" + StringUtils.LINE_SEPERATOR
                + StringUtils.LINE_SEPERATOR;
        BufferedReader br = new BufferedReader(new StringReader("GET /index.html HTTP/1.1\r\n" + headerString));

        assertThrows(IllegalArgumentException.class, () -> {
            processor.parseRequest(br);
        });
    }

    @ParameterizedTest
    @CsvSource({"Host:localhost", "Host:localhost", "Host: localhost", "Host: localhost "})
    public void request가_주어지면_헤더의_value양옆으로_옵션공백이_존재하는경우_적절히_파싱된다(String header) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader("GET /index.html HTTP/1.1" + StringUtils.LINE_SEPERATOR
                + header + StringUtils.LINE_SEPERATOR
                + StringUtils.LINE_SEPERATOR
        ));

        HttpRequest httpRequest = processor.parseRequest(br);
        HttpHeaders headers = httpRequest.getHeaders();
        Assertions.assertEquals("localhost", headers.get("Host"));
    }

    @BeforeEach
    void setup() {
        processor = new Http11Processor();
    }
}