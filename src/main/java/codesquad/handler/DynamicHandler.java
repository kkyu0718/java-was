package codesquad.handler;

import codesquad.adapter.Adapter;
import codesquad.annotation.RequestMapping;
import codesquad.annotation.Session;
import codesquad.exception.InternalServerError;
import codesquad.exception.MethodNotAllowedException;
import codesquad.exception.NotFoundException;
import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;

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
                    if (mapping != null && mapping.path().equals(request.getPath())) {
                        if (!mapping.method().equals(request.getMethod())) {
                            // HTTP 메서드가 일치하지 않으면 405 응답 반환
                            throw new MethodNotAllowedException("Method Not Allowed");
                        }

                        Object[] args = buildMethodArguments(method, request);
                        try {
                            return (HttpResponse) method.invoke(adapter, args);
                        } catch (Exception e) {
                            // 예외 처리
                            throw new InternalServerError("Internal Server Error");
                        }
                    }
                }
            }
        }
        // 처리할 수 없는 요청에 대한 응답
        throw new NotFoundException("Not Found");
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
