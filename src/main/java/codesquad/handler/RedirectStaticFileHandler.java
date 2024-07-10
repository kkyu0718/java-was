package codesquad.handler;

import codesquad.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class RedirectStaticFileHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(RedirectStaticFileHandler.class);
    private StaticFileReaderSpec staticFileReader;
    private List<String> whitelist;

    public RedirectStaticFileHandler(StaticFileReaderSpec staticFileReader, List<String> whitelist) {
        this.staticFileReader = staticFileReader;
        this.whitelist = whitelist;
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        String path = request.getPath();
        String indexPath = path + "/index.html";
        try {
            if (!staticFileReader.exists(indexPath)) {
                return new HttpResponse.Builder(request, HttpStatus.NOT_FOUND).build();
            }
        } catch (IOException e) {
            throw new RuntimeException("error reading file " + indexPath);
        }

        return readFileAndCreateResponse(indexPath, request);
    }

    private HttpResponse readFileAndCreateResponse(String path, HttpRequest request) {
        try {
            if (!staticFileReader.exists(path)) {
                return new HttpResponse.Builder(request, HttpStatus.NOT_FOUND).build();
            }

            byte[] bytes = staticFileReader.readFile(path);

            MimeType contentType = MimeType.HTML;

            return new HttpResponse.Builder(request, HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MimeType.HTML.getMimeType())
                    .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(bytes.length))
                    .body(HttpBody.of(bytes, contentType))
                    .build();
        } catch (IOException ex) {
            logger.error("Error reading file: " + path);
            return new HttpResponse.Builder(request, HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        return whitelist.contains(request.getPath());
    }
}
