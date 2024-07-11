package codesquad.handler;

import codesquad.http.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class RedirectStaticFileHandlerTest {
    private RedirectStaticFileHandler handler;
    private String redirectNeededPath;
    private String nonExistentPath;

    @BeforeEach
    void setup() {
        redirectNeededPath = "/registration";
        handler = new RedirectStaticFileHandler(
                new StaticFileReader(),
                List.of(redirectNeededPath)
        );
        nonExistentPath = "/non-existent";
    }

    @Test
    void RedirectStaticFileHandler가_주어지고_존재하지않는_Path가_주어졌으나_디렉토리의_index_html가_존재할때_200상태코드가_주어진다() {
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, redirectNeededPath, HttpVersion.HTTP11).build();

        HttpResponse response = handler.handle(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getBody().getBytes());
    }

    @Test
    void RedirectStaticFileHandler가_주어지고_존재하지않는_Path와_디렉토리의_index_html이_없을때_404상태코드가_주어진다() {
        handler = new RedirectStaticFileHandler(
                new StaticFileReader(),
                List.of(nonExistentPath)
        );

        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, nonExistentPath, HttpVersion.HTTP11).build();

        HttpResponse response = handler.handle(request);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        Assertions.assertNull(response.getBody().getBytes());
    }

    @Test
    void RedirectStaticFileHandler가_주어지고_파일읽기_중_에러가_발생했을때_500상태코드가_주어진다() throws IOException {
        RedirectStaticFileHandler faultyHandler = new RedirectStaticFileHandler(
                new StaticFileReaderSpec() {
                    @Override
                    public byte[] readFile(String path) throws IOException {
                        throw new IOException("IO 에러 발생");
                    }

                    @Override
                    public boolean exists(String path) {
                        return true;
                    }
                },
                List.of(redirectNeededPath)
        );

        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, redirectNeededPath, HttpVersion.HTTP11).build();

        HttpResponse response = faultyHandler.handle(request);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
    }

    @Test
    void RedirectStaticFileHandler_canHandle_메서드가_정확하게_작동하는지_검증한다() {
        // whitelist에 존재하는 경로에 대한 요청
        HttpRequest validRequest = new HttpRequest.Builder(HttpMethod.GET, redirectNeededPath, HttpVersion.HTTP11).build();
        Assertions.assertTrue(handler.canHandle(validRequest));

        // whitelist에 존재하지 않는 경로에 대한 요청
        HttpRequest invalidRequest = new HttpRequest.Builder(HttpMethod.GET, nonExistentPath, HttpVersion.HTTP11).build();
        Assertions.assertFalse(handler.canHandle(invalidRequest));
    }
}
