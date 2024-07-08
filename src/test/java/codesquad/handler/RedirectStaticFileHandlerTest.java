package codesquad.handler;

import codesquad.global.Path;
import codesquad.http.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

class RedirectStaticFileHandlerTest {
    private RedirectStaticFileHandler handler;
    private Path redirectNeededPath;

    @BeforeEach
    void setup() {
        redirectNeededPath = Path.of("/registration");
        handler = new RedirectStaticFileHandler(
                new StaticFileReader(),
                List.of(redirectNeededPath)
        );
    }

    @Test
    void RedirectStaticFileHandler가_주어지고_존재하지않는_Path가_주어졌으나_디렉토리의_index_html가_존재할때_200상태코드가_주어진다() {
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, redirectNeededPath, HttpVersion.HTTP11, httpHeaders, null, null);

        HttpResponse response = handler.handle(request);

        Assertions.assertEquals(HttpStatus.OK, response.getStatus());
        Assertions.assertNotNull(response.getBody().getBytes());
    }

    @Test
    void RedirectStaticFileHandler가_주어지고_존재하지않는_Path와_디렉토리의_index_html이_없을때_404상태코드가_주어진다() {
        Path nonExistentPath = Path.of("/non-existent");
        handler = new RedirectStaticFileHandler(
                new StaticFileReader(),
                List.of(nonExistentPath)
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, nonExistentPath, HttpVersion.HTTP11, httpHeaders, null, null);

        HttpResponse response = handler.handle(request);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
        Assertions.assertNull(response.getBody().getBytes());
    }

    @Test
    void RedirectStaticFileHandler가_주어지고_파일읽기_중_에러가_발생했을때_500상태코드가_주어진다() throws IOException {
        RedirectStaticFileHandler faultyHandler = new RedirectStaticFileHandler(
                new StaticFileReaderSpec() {
                    @Override
                    public byte[] readFile(Path path) throws IOException {
                        throw new IOException("IO 에러 발생");
                    }

                    @Override
                    public boolean exists(Path path) {
                        return true;
                    }
                },
                List.of(redirectNeededPath)
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, redirectNeededPath, HttpVersion.HTTP11, httpHeaders, null, null);

        HttpResponse response = faultyHandler.handle(request);

        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatus());
    }
}
