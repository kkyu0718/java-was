package codesquad;

import codesquad.adapter.PostAdapter;
import codesquad.adapter.UserAdapter;
import codesquad.db.DbConfig;
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
import codesquad.reader.SystemFileReader;
import codesquad.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
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
    private final DbConfig dbConfig;

    public WasServer(int port, DbConfig dbConfig) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.userSessionService = new UserSessionService();
        this.executorService = Executors.newFixedThreadPool(MAX_THREAD_POOL_SIZE);
        this.dbConfig = dbConfig;
        this.handlers = initializeHandlers();

        logger.debug("Listening for connection on port {} ....", port);
    }

    public void run() {
        executorService.execute(() -> {
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     InputStream is = clientSocket.getInputStream()) {

                    logger.debug("Client connected");
                    OutputStream clientOutput = clientSocket.getOutputStream();
                    HttpProcessor processor = new Http11Processor();

                    // 요청 파싱
                    HttpRequest httpRequest = processor.parseRequest(is);
                    logger.debug(httpRequest.toString());

                    // 요청 처리
                    HttpResponse httpResponse = handleRequest(httpRequest);
                    processor.writeResponse(clientOutput, httpResponse);

                } catch (Exception ex) {
                    logger.error("Server accept failed");
                    ex.printStackTrace();
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
        logger.debug("No handler found for request: {}", httpRequest.getPath());
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
        UserDbServiceSpec userDbService = new UserDbServiceCsv();
//        UserDbServiceSpec userDbService = new UserDbServiceJdbc(dbConfig);
        List<String> whitelist = List.of(
                "/",
                "/registration",
                "/article",
                "/comment",
                "/main",
                "/login",
                "/user/list"
        );

        PostServiceSpec postService = new PostServiceCsv();
//        PostServiceSpec postService = new PostServiceJdbc(dbConfig);

        UserAdapter userAdapter = new UserAdapter(userDbService, userSessionService);
        PostAdapter postAdapter = new PostAdapter(postService);

        return List.of(
                new StaticFileHandler(userSessionService, userDbService, postService, new StaticFileReader(), new SystemFileReader()),
                new RedirectStaticFileHandler(whitelist),
                new DynamicHandler(List.of(userAdapter, postAdapter))
        );
    }
}
