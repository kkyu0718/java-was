package codesquad;

import codesquad.adapter.UserAdapter;
import codesquad.exception.MethodNotAllowedException;
import codesquad.exception.NotFoundException;
import codesquad.filter.FilterChain;
import codesquad.filter.SessionFilter;
import codesquad.handler.DynamicHandler;
import codesquad.handler.HttpHandler;
import codesquad.handler.RedirectStaticFileHandler;
import codesquad.handler.StaticFileHandler;
import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;
import codesquad.http.HttpStatus;
import codesquad.processor.Http11Processor;
import codesquad.processor.HttpProcessor;
import codesquad.reader.StaticFileReader;
import codesquad.service.UserDbServiceJdbc;
import codesquad.service.UserDbServiceSpec;
import codesquad.service.UserSessionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WasServer {
    private static final Logger logger = LoggerFactory.getLogger(WasServer.class);
    private static final int DEFAULT_PORT = 8080;
    private static final int MAX_THREAD_POOL_SIZE = 100;

    private final ServerSocket serverSocket;
    private final List<HttpHandler> handlers;
    private final ExecutorService executorService;
    private final UserSessionService userSessionService;

    public WasServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.userSessionService = new UserSessionService();
        this.handlers = initializeHandlers();
        this.executorService = Executors.newFixedThreadPool(MAX_THREAD_POOL_SIZE);

        logger.debug("Listening for connection on port {} ....", port);
    }

    public void run() {
        executorService.execute(() -> {
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    logger.debug("Client connected");
                    OutputStream clientOutput = clientSocket.getOutputStream();
                    HttpProcessor processor = new Http11Processor();

                    // 요청 파싱
                    HttpRequest httpRequest = processor.parseRequest(br);
                    logger.debug(httpRequest.toString());

                    // 요청 처리
                    HttpResponse httpResponse = handleRequest(httpRequest);
                    processor.writeResponse(clientOutput, httpResponse);

                } catch (IOException ex) {
                    logger.error("Server accept failed", ex);
                }
            }
        });
    }

    private HttpResponse handleRequest(HttpRequest httpRequest) {
        for (HttpHandler handler : handlers) {
            if (handler.canHandle(httpRequest)) {
                logger.debug("Forwarding request to handler: {}", handler.getClass().getName());
                return doDispatch(handler, httpRequest);
            }
        }
        // 핸들러를 찾지 못한 경우 NOT FOUND 응답
        return new HttpResponse.Builder(httpRequest, HttpStatus.FOUND)
                .redirect("/exception/404.html")
                .build();
    }

    private HttpResponse doDispatch(HttpHandler handler, HttpRequest httpRequest) {
        FilterChain filterChain = getFilterChain();
        HttpResponse httpResponse = filterChain.doFilter(httpRequest);

        if (httpResponse != null) {
            return httpResponse;
        }

        try {
            return handler.handle(httpRequest);
        } catch (NotFoundException ex) {
            logger.error("Resource not found", ex);
            return new HttpResponse.Builder(httpRequest, HttpStatus.FOUND)
                    .redirect("/exception/404.html")
                    .build();
        } catch (MethodNotAllowedException ex) {
            logger.error("Method not allowed", ex);
            return new HttpResponse.Builder(httpRequest, HttpStatus.FOUND)
                    .redirect("/exception/405.html")
                    .build();
        } catch (Exception ex) {
            logger.error("Internal server error", ex);
            return new HttpResponse.Builder(httpRequest, HttpStatus.FOUND)
                    .redirect("/exception/500.html")
                    .build();
        }
    }

    private FilterChain getFilterChain() {
        FilterChain filterChain = new FilterChain();
        filterChain.addFilter(new SessionFilter(userSessionService));
        return filterChain;
    }

    private List<HttpHandler> initializeHandlers() {
        UserDbService userDbService = new UserDbService();
        List<String> whitelist = List.of(
                "/",
                "/registration",
                "/article",
                "/comment",
                "/main",
                "/login",
                "/user/list"
        );

        UserAdapter userAdapter = new UserAdapter(userDbService, userSessionService);

        return List.of(
                new StaticFileHandler(new StaticFileReader(), userSessionService, userDbService),
                new RedirectStaticFileHandler(whitelist),
                new DynamicHandler(List.of(userAdapter))
        );
    }
}
