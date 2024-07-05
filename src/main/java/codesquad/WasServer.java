package codesquad;

import codesquad.adapter.Adapter;
import codesquad.adapter.UserAdapter;
import codesquad.handler.DynamicHandler;
import codesquad.handler.HttpHandler;
import codesquad.handler.StaticFileHandler;
import codesquad.handler.StaticFileReader;
import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;
import codesquad.processor.Http11Processor;
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
    private static String resourceRootPath = "src/main/resources/static";

    private ServerSocket serverSocket;
    private List<HttpHandler> handlers;
    private ExecutorService executorService;

    public WasServer(int port) throws IOException {
        StaticFileHandler staticFileHandler = new StaticFileHandler(new StaticFileReader(resourceRootPath));

        Adapter userAdapter = new UserAdapter();
        DynamicHandler dynamicHandler = new DynamicHandler(List.of(userAdapter));

        serverSocket = new ServerSocket(port);
        handlers = List.of(staticFileHandler, dynamicHandler);
        executorService = Executors.newFixedThreadPool(MAX_THREAD_POOL_SIZE);

        logger.debug("Listening for connection on port 8080 ....");
    }

    public void run() {
        executorService.execute(() -> {
            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {
                    logger.debug("Client connected");

                    //TODO http 버젼 별 분기처리 필요
                    Http11Processor processor = new Http11Processor();
                    HttpRequest httpRequest = processor.parseRequest(br);
                    logger.debug(httpRequest.toString());

                    //TODO URI 패턴 제한
                    //TODO Path Util 클래스 뺴기

                    //TODO handler 별 분기처리 필요
                    // do service
                    HttpHandler handler = getHandler(httpRequest);
                    HttpResponse response = handler.handle(httpRequest);

                    OutputStream clientOutput = clientSocket.getOutputStream();
                    processor.writeResponse(clientOutput, response);

                } catch (Exception ex) {
                    logger.error("Server accept failed ");
                    ex.printStackTrace();
                }
            }
        });
    }

    private HttpHandler getHandler(HttpRequest httpRequest) {
        for (HttpHandler handler : handlers) {
            if (handler.canHandle(httpRequest)) {
                return handler;
            }
        }

        throw new IllegalArgumentException("처리할 수 있는 handler 가 존재하지 않습니다. " + httpRequest.getPath());
    }
}
