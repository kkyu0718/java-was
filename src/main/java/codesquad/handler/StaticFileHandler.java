package codesquad.handler;

import codesquad.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFileHandler implements HttpHandler {
    private static final String RESOURCE_ROOT_PATH = "src/main/resources/static";
    private static final Logger logger = LoggerFactory.getLogger(StaticFileHandler.class);

    @Override
    public HttpResponse handle(HttpRequest request) {
        HttpHeaders reqHeaders = request.getHeaders();
        String path = reqHeaders.get(HttpHeaders.PATH);
        String httpVersion = reqHeaders.get(HttpHeaders.HTTP_VERSION);

        HttpHeaders resHeaders = new HttpHeaders();
        Path filePath = Paths.get(RESOURCE_ROOT_PATH, path);

        if (Files.exists(filePath)) {
            try {
                byte[] bytes = Files.readAllBytes(filePath);
                String contentType = Files.probeContentType(filePath);

                resHeaders.put(HttpHeaders.CONTENT_TYPE, contentType);

                return new HttpResponse(request, HttpStatus.OK, resHeaders, new HttpBody(bytes));
            } catch (IOException ex) {
                logger.error("static file io exception 발생");
            }
        }

        resHeaders.put(HttpHeaders.HTTP_VERSION, httpVersion);
        return new HttpResponse(request, HttpStatus.NOT_FOUND, resHeaders, new HttpBody(null));
    }
}
