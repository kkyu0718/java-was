package codesquad.handler;

import codesquad.adapter.Adapter;
import codesquad.annotation.RequestMapping;
import codesquad.annotation.Session;
import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;
import codesquad.http.HttpStatus;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

public class DynamicHandler implements HttpHandler {
    private List<Adapter> adapters;

    public DynamicHandler(List<Adapter> adapters) {
        this.adapters = adapters;
    }

    public boolean canHandle(HttpRequest request) {
        return adapters.stream().anyMatch(adapter -> adapter.supports(request.getPath()));
    }

    public HttpResponse handle(HttpRequest request) {
        for (Adapter adapter : adapters) {
            if (adapter.supports(request.getPath())) {
                Method[] methods = adapter.getClass().getMethods();
                for (Method method : methods) {
                    RequestMapping mapping = method.getAnnotation(RequestMapping.class);
                    if (mapping != null &&
                            mapping.path().equals(request.getPath()) &&
                            mapping.method().equalsIgnoreCase(request.getMethod().toString())) {

                        Object[] args = buildMethodArguments(method, request);
                        try {
                            return (HttpResponse) method.invoke(adapter, args);
                        } catch (Exception e) {
                            // 예외 처리
                            return new HttpResponse.Builder(request, HttpStatus.INTERNAL_SERVER_ERROR).build();
                        }
                    }
                }
            }
        }
        // 처리할 수 없는 요청에 대한 응답
        return new HttpResponse.Builder(request, HttpStatus.NOT_FOUND).build();
    }

    private Object[] buildMethodArguments(Method method, HttpRequest request) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (param.getType() == HttpRequest.class) {
                args[i] = request;
            } else if (param.isAnnotationPresent(Session.class)) {
                args[i] = request.getHeader("userId");
            } else {
                // 다른 파라미터 처리 로직
            }
        }

        return args;
    }
}
