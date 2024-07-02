package codesquad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class WasServer {
    public static Logger logger = LoggerFactory.getLogger(WasServer.class);
    private ServerSocket serverSocket;

    public WasServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        logger.debug("Listening for connection on port 8080 ....");
    }

    public void run() throws IOException {
        InputStream clientInput;
        while (true) { // 무한 루프를 돌며 클라이언트의 연결을 기다립니다.
            try (Socket clientSocket = serverSocket.accept()) { // 클라이언트 연결을 수락합니다.
                logger.debug("Client connected");

                // 클라이언트 요청 내용을 읽습니다.
                clientInput = clientSocket.getInputStream();
                Map<String, String> headers = parseHeaders(clientInput);

                // HTTP 응답을 생성합니다.
                OutputStream clientOutput = clientSocket.getOutputStream();
                clientOutput.write("HTTP/1.1 200 OK\r\n".getBytes());
                clientOutput.write("Content-Type: text/html\r\n".getBytes());
                clientOutput.write("\r\n".getBytes());
                clientOutput.write("<h1>Hello</h1>\r\n".getBytes()); // 응답 본문으로 "Hello"를 보냅니다.
                clientOutput.flush();
            } catch (IOException ex) {
                logger.error("Server accept failed");
            }
        }
    }

    private HashMap<String, String> parseHeaders(InputStream clientInput) throws IOException {
        HashMap<String, String> headers = new HashMap<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(clientInput));
        String line;

        parseStartLine(reader.readLine(), headers);
        parseRequestLine(reader.readLine(), headers);

        while (!(line = reader.readLine()).isEmpty()) { // 빈 줄이 나올 때까지 읽습니다.
            logger.debug(line);
            String[] headerSplits = line.split(":", 2);
            headers.put(headerSplits[0], headerSplits[1]);
        }

        return headers;
    }

    private void parseStartLine(String startLine, HashMap<String, String> headers) throws IOException {
        String[] startLineSplits = startLine.split(" ");
        String requestMethod = startLineSplits[0];
        String requestTarget = startLineSplits[1];
        String httpVersion = startLineSplits[2];

        headers.put("HttpMethod", requestMethod);
        headers.put("Path", requestTarget);
        headers.put("HttpVersion", httpVersion);
    }

    private void parseRequestLine(String requestLine, HashMap<String, String> headers) {
        String host = requestLine.split(":", 2)[1];
        headers.put("Host", host);
    }
}
