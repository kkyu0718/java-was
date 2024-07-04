package codesquad.adapter;

import codesquad.global.Path;
import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;

public interface Adapter {
    boolean supports(Path path);

    HttpResponse handle(HttpRequest request);
}
