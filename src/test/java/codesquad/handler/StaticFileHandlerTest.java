package codesquad.handler;

import codesquad.http.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

class StaticFileHandlerTest {
    private StaticFileHandler handler;
    private String resourcePath = "src/test/resources/static";

    @Test
    void StaticFileHandler가_주어지고_존재하지않는_Path가_주어졌을때_404상태코드가_주어진다() {
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "/not-index.html", HttpVersion.HTTP11, httpHeaders, null);

        HttpResponse response = handler.handle(request);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        Assertions.assertNull(response.getBody().getBytes());
    }

    @Test
    void StaticFileHandler가_주어지고_존재하는_Path가_주어졌을때_200상태코드가_주어진다() {
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "/index.html", HttpVersion.HTTP11, httpHeaders, null);

        HttpResponse response = handler.handle(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertEquals("text/html", response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void StaticFileHandler가_주어지고_CSS파일_요청시_200상태코드와_올바른_컨텐츠타입이_주어진다() {
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "/main.css", HttpVersion.HTTP11, httpHeaders, null);

        HttpResponse response = handler.handle(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertEquals("text/css", response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void StaticFileHandler가_주어지고_파일읽기_중_에러가_발생했을때_500상태코드가_주어진다() throws IOException {
        StaticFileHandler faultyHandler = new StaticFileHandler(resourcePath) {
            @Override
            protected HttpResponse readStaticFile(HttpRequest request, Path filePath) throws IOException {
                throw new IOException("Simulated IO Exception");
            }
        };

        HttpHeaders httpHeaders = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "/index.html", HttpVersion.HTTP11, httpHeaders, null);

        HttpResponse response = faultyHandler.handle(request);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @BeforeEach
    void setup() {
        handler = new StaticFileHandler(resourcePath);
    }
}