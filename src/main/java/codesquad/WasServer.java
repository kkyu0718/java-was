package codesquad;

import codesquad.handler.StaticFileHandler;
import codesquad.http.HttpRequest;
import codesquad.http.HttpResponse;
import codesquad.processor.Http11Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WasServer {
    private static Logger logger = LoggerFactory.getLogger(WasServer.class);
    private static int MAX_THREAD_POOL_SIZE = 100;

    private ServerSocket serverSocket;
    private StaticFileHandler staticFileHandler;
    private ExecutorService executorService;

    public WasServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        staticFileHandler = new StaticFileHandler();
        executorService = Executors.newFixedThreadPool(MAX_THREAD_POOL_SIZE);
        logger.debug("Listening for connection on port 8080 ....");
    }

    public void run() {
        executorService.execute(() -> {
            while (true) {
                InputStream clientInput;

                try (Socket clientSocket = serverSocket.accept()) {
                    logger.debug("Client connected");

                    clientInput = clientSocket.getInputStream();

                    //TODO http 버젼 별 분기처리 필요
                    Http11Processor processor = new Http11Processor();
                    HttpRequest httpRequest = processor.parseRequest(clientInput);
                    logger.debug(httpRequest.toString());

                    //TODO handler 별 분기처리 필요
                    // do service
                    HttpResponse response = staticFileHandler.handle(httpRequest);

                    OutputStream clientOutput = clientSocket.getOutputStream();
                    processor.writeResponse(clientOutput, response);

                } catch (IOException ex) {
                    logger.error("Server accept failed ");
                    ex.printStackTrace();
                }
            }
        });
    }


}
