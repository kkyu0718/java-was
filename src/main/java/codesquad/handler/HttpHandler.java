package codesquad.handler;

import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;

public interface HttpHandler {
    HttpResponse handle(HttpRequest request);

    boolean canHandle(HttpRequest request);
}
