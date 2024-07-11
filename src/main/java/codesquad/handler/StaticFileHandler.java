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
        try {
            if (!isValid(path)) {
                return new HttpResponse.Builder(request, HttpStatus.NOT_FOUND).build();
            }
        } catch (IOException e) {
            throw new RuntimeException("error reading file " + path);
        }

        return readFileAndCreateResponse(request);
    }

    private boolean isValid(String path) throws IOException {
        return staticFileReader.exists(path);
    }

    private HttpResponse readFileAndCreateResponse(HttpRequest request) {
        try {
            byte[] bytes = staticFileReader.readFile(request.getPath());
            MimeType contentType = MimeType.fromExt(request.getExt());

            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.put(HttpHeaders.CONTENT_TYPE, contentType.getMimeType());
            httpHeaders.put(HttpHeaders.CONTENT_LENGTH, String.valueOf(bytes != null ? bytes.length : 0));

            return new HttpResponse.Builder(request, HttpStatus.OK)
                    .headers(httpHeaders)
                    .body(HttpBody.of(bytes, contentType))
                    .build();
        } catch (IOException ex) {
            logger.error("Error reading file: " + request.getPath());
            return new HttpResponse.Builder(request, HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        return request.isFilePath();
    }

}