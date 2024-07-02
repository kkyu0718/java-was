package codesquad.handler;

import codesquad.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class StaticFileHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(StaticFileHandler.class);
    private String resourceRootPath;

    public StaticFileHandler(String resourceRootPath) {
        this.resourceRootPath = resourceRootPath;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        String path = request.getPath();
        Path filePath = Paths.get(resourceRootPath, path);

        if (!Files.exists(filePath)) {
            logger.warn("File not found: " + filePath);
            return createNotFoundResponse(request);
        }

        try {
            return readStaticFile(request, filePath);
        } catch (IOException ex) {
            logger.error("Error reading file: " + filePath, ex);
            return createErrorResponse(request);
        }
    }

    protected HttpResponse readStaticFile(HttpRequest request, Path filePath) throws IOException {
        byte[] bytes = Files.readAllBytes(filePath);
        String contentType = Files.probeContentType(filePath);

        HttpHeaders resHeaders = new HttpHeaders();
        resHeaders.put(HttpHeaders.HTTP_VERSION, request.getHttpVersion().getRepresentation());
        resHeaders.put(HttpHeaders.CONTENT_TYPE, contentType);

        return new HttpResponse(request, HttpStatus.OK, resHeaders, new HttpBody(bytes));
    }

    private HttpResponse createNotFoundResponse(HttpRequest request) {
        HttpHeaders resHeaders = new HttpHeaders();
        resHeaders.put(HttpHeaders.HTTP_VERSION, request.getHttpVersion().getRepresentation());

        return new HttpResponse(request, HttpStatus.NOT_FOUND, resHeaders, new HttpBody(null));
    }

    private HttpResponse createErrorResponse(HttpRequest request) {
        HttpHeaders resHeaders = new HttpHeaders();
        resHeaders.put(HttpHeaders.HTTP_VERSION, request.getHttpVersion().getRepresentation());

        return new HttpResponse(request, HttpStatus.INTERNAL_SERVER_ERROR, resHeaders, new HttpBody(null));
    }
}