package codesquad.handler;

import codesquad.adapter.Adapter;
import codesquad.http.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DynamicHandlerTest {
    private DynamicHandler handler;

    @BeforeEach
    void setup() {
        handler = new DynamicHandler(List.of(new Adapter() {
            @Override
            public boolean supports(String path) {
                return path.equals("/exist");
            }

            @Override
            public HttpResponse handle(HttpRequest request) {
                return HttpResponse.createOkResponse(request, null, MimeType.NONE);
            }
        }));
    }

    @Test
    void DynamicHandler가_주어지고_존재하는_어댑터에_대한_요청이_주어졌을때_200상태코드가_주어진다() {
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "/exist", HttpVersion.HTTP11, httpHeaders, null, null);
        assertTrue(handler.canHandle(request));

        HttpResponse response = handler.handle(request);
        assertEquals(HttpStatus.OK, response.getStatus());
    }

    @Test
    void DynamicHandler가_주어지고_존재하지않는_어댑터에_대한_요청이_주어졌을때_예외가_발생한다() {
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpRequest request = new HttpRequest(HttpMethod.GET, "/non-existent", HttpVersion.HTTP11, httpHeaders, null, null);

        assertThrows(IllegalArgumentException.class, () -> {
            handler.handle(request);
        });
    }

    @Test
    void DynamicHandler_canHandle_메서드가_정확하게_작동하는지_검증한다() {
        // 지원하는 경로에 대한 요청
        HttpRequest supportedRequest = new HttpRequest(HttpMethod.GET, "/exist", HttpVersion.HTTP11, new HttpHeaders(), null, null);
        assertTrue(handler.canHandle(supportedRequest));

        // 지원하지 않는 경로에 대한 요청
        HttpRequest unsupportedRequest = new HttpRequest(HttpMethod.GET, "/non-existent", HttpVersion.HTTP11, new HttpHeaders(), null, null);
        assertFalse(handler.canHandle(unsupportedRequest));
    }
}