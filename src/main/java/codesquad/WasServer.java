package codesquad;

import codesquad.adapter.UserAdapter;
import codesquad.filter.FilterChain;
import codesquad.filter.SessionFilter;
import codesquad.handler.DynamicHandler;
import codesquad.handler.RedirectStaticFileHandler;
import codesquad.handler.StaticFileHandler;
import codesquad.http.HttpMethod;
import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;
import codesquad.processor.Http11Processor;
import codesquad.processor.HttpProcessor;
import codesquad.reader.StaticFileReader;
import codesquad.service.UserDbService;
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
    private static Logger logger = LoggerFactory.getLogger(WasServer.class);
    private static int MAX_THREAD_POOL_SIZE = 100;

    private ServerSocket serverSocket;
    private DynamicHandler dynamicHandler;
    private StaticFileHandler staticFileHandler;
    private RedirectStaticFileHandler redirectStaticFileHandler;
    private ExecutorService executorService;
    private UserSessionService userSessionService;

    public WasServer(int port) throws IOException {
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

        userSessionService = new UserSessionService();
        UserAdapter userAdapter = new UserAdapter(userDbService, userSessionService);
        serverSocket = new ServerSocket(port);
        dynamicHandler = new DynamicHandler(List.of(userAdapter));
        staticFileHandler = new StaticFileHandler(new StaticFileReader(), userSessionService, userDbService);
        redirectStaticFileHandler = new RedirectStaticFileHandler(whitelist);
        executorService = Executors.newFixedThreadPool(MAX_THREAD_POOL_SIZE);

        logger.debug("Listening for connection on port 8080 ....");
    }

    public void run() {
        executorService.execute(() -> {
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                    handleClientSocket(clientSocket, br);
                } catch (Exception ex) {
                    logger.error("Server accept failed ");
                    ex.printStackTrace();
                }
            }
        });
    }

    public void handleClientSocket(Socket clientSocket, BufferedReader br) {
        try {
            logger.debug("Client connected");

            // 요청 파싱
            HttpProcessor processor = new Http11Processor();
            HttpRequest httpRequest = processor.parseRequest(br);
            logger.debug(httpRequest.toString());

            // 필터 체인 생성
            FilterChain filterChain = getFilterChain();
            HttpResponse httpResponse = filterChain.doFilter(httpRequest);

            // 모든 필터를 통과했으므로 핸들러로 처리
            if (httpResponse == null) {
                httpResponse = getHttpResponse(httpRequest);
                logger.debug(httpResponse.getStatus().toString());
                logger.debug(httpResponse.getHeaders().toString());
                logger.debug(httpResponse.toString());
            }

            // 응답 쓰기
            OutputStream clientOutput = clientSocket.getOutputStream();
            processor.writeResponse(clientOutput, httpResponse);
        } catch (Exception ex) {
            logger.error("Error handling client socket", ex);
        }
    }

    private HttpResponse getHttpResponse(HttpRequest httpRequest) {
        if (httpRequest.getMethod() == HttpMethod.GET && staticFileHandler.canHandle(httpRequest)) {
            logger.info("forward to staticFileHandler" + httpRequest.getPath());
            return staticFileHandler.handle(httpRequest);
        } else if (httpRequest.getMethod() == HttpMethod.GET && redirectStaticFileHandler.canHandle(httpRequest)) {
            logger.info("forward to redirect" + httpRequest.getPath());
            return redirectStaticFileHandler.handle(httpRequest);
        } else if (dynamicHandler.canHandle(httpRequest)) {
            logger.info("forward to dynamicHandler" + httpRequest.getPath());

            return dynamicHandler.handle(httpRequest);
        }
        throw new RuntimeException("no handler found for " + httpRequest.getPath());
    }

    private FilterChain getFilterChain() {
        FilterChain filterChain = new FilterChain();
        filterChain.addFilter(new SessionFilter(userSessionService));
        return filterChain;
    }
}
