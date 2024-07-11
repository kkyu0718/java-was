package codesquad.handler;

import codesquad.http.*;
import codesquad.reader.StaticFileReader;
import codesquad.reader.StaticFileReaderSpec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class StaticFileHandlerTest {
    private StaticFileHandler handler;

    @BeforeEach
    void setup() {
        handler = new StaticFileHandler(new StaticFileReader());
    }

    @Test
    void StaticFileHandler가_주어지고_존재하지않는_Path가_주어졌을때_404상태코드가_주어진다() {
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/not-index.html", HttpVersion.HTTP11).build();

        HttpResponse response = handler.handle(request);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        Assertions.assertNull(response.getBody().getBytes());
    }

    @Test
    void StaticFileHandler가_주어지고_존재하는_Path가_주어졌을때_200상태코드가_주어진다() {
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/index.html", HttpVersion.HTTP11).build();

        HttpResponse response = handler.handle(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertEquals(MimeType.HTML.getMimeType(), response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void StaticFileHandler가_주어지고_CSS파일_요청시_200상태코드와_올바른_컨텐츠타입이_주어진다() {
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/main.css", HttpVersion.HTTP11).build();

        HttpResponse response = handler.handle(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertEquals(MimeType.CSS.getMimeType(), response.getHeaders().get(HttpHeaders.CONTENT_TYPE));
        Assertions.assertNotNull(response.getBody());
    }

    @Test
    void StaticFileHandler가_주어지고_파일읽기_중_에러가_발생했을때_500상태코드가_주어진다() throws IOException {
        StaticFileHandler faultyHandler = new StaticFileHandler(new StaticFileReaderSpec() {
            @Override
            public byte[] readFile(String path) throws IOException {
                throw new IOException("IO 에러 발생");
            }

            @Override
            public boolean exists(String path) {
                return true;
            }

            @Override
            public String readFileLines(String path) throws IOException {
                return null;
            }
        });

        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/index.html", HttpVersion.HTTP11).build();

        HttpResponse response = faultyHandler.handle(request);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    void StaticFileHandler_canHandle_메서드가_정확하게_작동하는지_검증한다() {
        // 파일 경로에 대한 요청
        HttpRequest fileRequest = new HttpRequest.Builder(HttpMethod.GET, "/index.html", HttpVersion.HTTP11).build();
        Assertions.assertTrue(handler.canHandle(fileRequest));

        // 디렉토리 경로에 대한 요청
        HttpRequest directoryRequest = new HttpRequest.Builder(HttpMethod.GET, "/directory/", HttpVersion.HTTP11).build();
        Assertions.assertFalse(handler.canHandle(directoryRequest));

        // 비어 있는 경로에 대한 요청
        HttpRequest emptyRequest = new HttpRequest.Builder(HttpMethod.GET, "", HttpVersion.HTTP11).build();
        Assertions.assertFalse(handler.canHandle(emptyRequest));
    }

}
