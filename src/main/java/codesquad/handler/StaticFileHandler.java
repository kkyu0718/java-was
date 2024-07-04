package codesquad.handler;

import codesquad.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class StaticFileHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(StaticFileHandler.class);
    private StaticFileReaderSpec staticFileReader;

    public StaticFileHandler(StaticFileReaderSpec staticFileReader) {
        this.staticFileReader = staticFileReader;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        String path = request.getPath();
        if (!staticFileReader.exists(path)) {
            logger.warn("File not found: " + path);
            return createNotFoundResponse(request);
        }

        try {
            byte[] bytes = staticFileReader.readFile(path);
            MimeType contentType = MimeType.fromExt(path);

            return createOkResponse(request, bytes, contentType);
        } catch (IOException ex) {
            logger.error("Error reading file: " + path, ex);
            return createErrorResponse(request);
        }
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        String path = request.getPath();
        String[] split = path.split("\\.");

        if (split.length == 2) {
            return true;
        } else return staticFileReader.exists(path + "/index.html");
    }

    private static HttpResponse createOkResponse(HttpRequest request, byte[] bytes, MimeType contentType) {
        HttpHeaders resHeaders = new HttpHeaders();
        resHeaders.put(HttpHeaders.HTTP_VERSION, request.getHttpVersion().getRepresentation());
        resHeaders.put(HttpHeaders.CONTENT_TYPE, contentType.getMimeType());

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