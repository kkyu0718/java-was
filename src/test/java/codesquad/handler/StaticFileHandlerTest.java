package codesquad.handler;

import codesquad.global.Path;
import codesquad.http.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class StaticFileHandlerTest {
    private StaticFileHandler handler;

    @Test
    void StaticFileHandler가_주어지고_존재하지않는_Path가_주어졌을때_404상태코드가_주어진다() {
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, Path.of("/not-index.html"), HttpVersion.HTTP11, httpHeaders, null, null);

        HttpResponse response = handler.handle(request);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        Assertions.assertNull(response.getBody().getBytes());
    }


    @Test
    void StaticFileHandler가_주어지고_존재하는_Path가_주어졌을때_200상태코드가_주어진다() {
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, Path.of("/index.html"), HttpVersion.HTTP11, httpHeaders, null, null);

        HttpResponse response = handler.handle(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertEquals(MimeType.HTML.getMimeType(), response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void StaticFileHandler가_주어지고_CSS파일_요청시_200상태코드와_올바른_컨텐츠타입이_주어진다() {
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, Path.of("/main.css"), HttpVersion.HTTP11, httpHeaders, null, null);

        HttpResponse response = handler.handle(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertEquals(MimeType.CSS.getMimeType(), response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void StaticFileHandler가_주어지고_파일읽기_중_에러가_발생했을때_500상태코드가_주어진다() throws IOException {
        StaticFileHandler faultyHandler = new StaticFileHandler(new StaticFileReaderSpec() {

            @Override
            public byte[] readFile(Path path) throws IOException {
                throw new IOException("IO 에러 발생");
            }

            @Override
            public boolean exists(Path path) {
                return true;
            }
        });

        HttpHeaders httpHeaders = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, Path.of("/index.html"), HttpVersion.HTTP11, httpHeaders, null, null);

        HttpResponse response = faultyHandler.handle(request);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    void StaticFileHandler_canHandle_메서드가_정확하게_작동하는지_검증한다() {
        // 파일 경로에 대한 요청
        HttpRequest fileRequest = new HttpRequest(HttpMethod.GET, Path.of("/index.html"), HttpVersion.HTTP11, new HttpHeaders(), null, null);
        Assertions.assertTrue(handler.canHandle(fileRequest));

        // 디렉토리 경로에 대한 요청
        HttpRequest directoryRequest = new HttpRequest(HttpMethod.GET, Path.of("/directory/"), HttpVersion.HTTP11, new HttpHeaders(), null, null);
        Assertions.assertFalse(handler.canHandle(directoryRequest));

        // 비어 있는 경로에 대한 요청
        HttpRequest emptyRequest = new HttpRequest(HttpMethod.GET, Path.of(""), HttpVersion.HTTP11, new HttpHeaders(), null, null);
        Assertions.assertFalse(handler.canHandle(emptyRequest));
    }

    @BeforeEach
    void setup() {
        handler = new StaticFileHandler(new StaticFileReader());
    }
}