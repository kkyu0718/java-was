package codesquad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class WasServer {
    public static Logger logger = LoggerFactory.getLogger(WasServer.class);
    private ServerSocket serverSocket;

    public WasServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        logger.debug("Listening for connection on port 8080 ....");
    }

    public void run() throws IOException {
        InputStream clientInput;
        while (true) {
            try (Socket clientSocket = serverSocket.accept()) {
                logger.debug("Client connected");

                clientInput = clientSocket.getInputStream();

                //TODO http 버젼 별 분기처리 필요
                Http11Processor processor = new Http11Processor();
                HttpRequest httpRequest = processor.parseRequest(clientInput);
                logger.debug(httpRequest.toString());

                // do service

                HttpResponse response = new HttpResponse(new HttpHeaders(), new HttpBody());
                OutputStream clientOutput = clientSocket.getOutputStream();
                processor.createResponse(clientOutput, response);

            } catch (IOException ex) {
                logger.error("Server accept failed");
            }
        }
    }


}
