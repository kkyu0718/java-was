package codesquad;

import codesquad.adapter.Adapter;
import codesquad.adapter.UserAdapter;
import codesquad.handler.DynamicHandler;
import codesquad.handler.RedirectStaticFileHandler;
import codesquad.handler.StaticFileHandler;
import codesquad.handler.StaticFileReader;
import codesquad.http.HttpMethod;
import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;
import codesquad.http.HttpStatus;
import codesquad.processor.Http11Processor;
import codesquad.processor.HttpProcessor;
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

    public WasServer(int port) throws IOException {
        Adapter userAdapter = new UserAdapter();
        List<String> whitelist = List.of(
                "/",
                "/registration",
                "/article",
                "/comment",
                "/main",
                "/login"
        );

        serverSocket = new ServerSocket(port);
        dynamicHandler = new DynamicHandler(List.of(userAdapter));
        staticFileHandler = new StaticFileHandler(new StaticFileReader());
        redirectStaticFileHandler = new RedirectStaticFileHandler(new StaticFileReader(), whitelist);
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

            //TODO http 버젼 별 분기처리 필요
            HttpProcessor processor = new Http11Processor();
            HttpRequest httpRequest = processor.parseRequest(br);
            logger.debug(httpRequest.toString());

            HttpResponse httpResponse = null;

            // file 로 요청이 오거나 정해진 view 로 요청이 오는 경우
            if (httpRequest.getMethod() == HttpMethod.GET && staticFileHandler.canHandle(httpRequest)) {
                httpResponse = staticFileHandler.handle(httpRequest);
            } else if (httpRequest.getMethod() == HttpMethod.GET && redirectStaticFileHandler.canHandle(httpRequest)) {
                httpResponse = redirectStaticFileHandler.handle(httpRequest);
            } else if (dynamicHandler.canHandle(httpRequest)) {
                httpResponse = dynamicHandler.handle(httpRequest);
            } else {
                httpResponse = new HttpResponse.Builder(httpRequest, HttpStatus.NOT_FOUND).build();
            }

            logger.debug(httpResponse.toString());
            OutputStream clientOutput = clientSocket.getOutputStream();
            processor.writeResponse(clientOutput, httpResponse);
        } catch (Exception ex) {
            logger.error("Error handling client socket", ex);
        }
    }
}
