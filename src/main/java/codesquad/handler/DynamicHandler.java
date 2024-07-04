package codesquad.handler;

import codesquad.adapter.Adapter;
import codesquad.adapter.UserAdapter;
import codesquad.global.Path;
import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;

import java.util.List;

public class DynamicHandler implements HttpHandler {
    private List<Adapter> adapters;

    public DynamicHandler(List<Adapter> adapters) {
        this.adapters = List.of(new UserAdapter());
    }

    @Override
    public HttpResponse handle(HttpRequest request) {
        Path path = request.getPath();
        Adapter adapter = getAdapter(path);
        return adapter.handle(request);
    }

    private Adapter getAdapter(Path path) {
        for (Adapter adapter : adapters) {
            if (adapter.supports(path)) {
                return adapter;
            }
        }
        throw new IllegalArgumentException("요청을 처리할 수 있는 어댑터가 존재하지 않습니다. " + path);
    }

    @Override
    public boolean canHandle(HttpRequest request) {
        return true;
    }
}
