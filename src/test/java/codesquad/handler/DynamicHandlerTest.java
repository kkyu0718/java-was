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
        }));
    }

    @Test
    void DynamicHandler가_주어지고_존재하는_어댑터에_대한_요청이_주어졌을때_응답이_주어진다() {
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/exist", HttpVersion.HTTP11).build();
        assertTrue(handler.canHandle(request));

        HttpResponse response = handler.handle(request);
        assertNotNull(response);
    }

    @Test
    void DynamicHandler가_주어지고_존재하지않는_어댑터에_대한_요청이_주어졌을때_404응답을_준다() {
        HttpRequest request = new HttpRequest.Builder(HttpMethod.GET, "/non-existent", HttpVersion.HTTP11).build();

        HttpResponse response = handler.handle(request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatus());
    }

    @Test
    void DynamicHandler_canHandle_메서드가_정확하게_작동하는지_검증한다() {
        // 지원하는 경로에 대한 요청
        HttpRequest supportedRequest = new HttpRequest.Builder(HttpMethod.GET, "/exist", HttpVersion.HTTP11).build();
        assertTrue(handler.canHandle(supportedRequest));

        // 지원하지 않는 경로에 대한 요청
        HttpRequest unsupportedRequest = new HttpRequest.Builder(HttpMethod.GET, "/non-existent", HttpVersion.HTTP11).build();
        assertFalse(handler.canHandle(unsupportedRequest));
    }
}
