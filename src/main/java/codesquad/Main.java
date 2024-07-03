package codesquad;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class Main {
    public static Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int PORT_NUMBER = 8080;

    public static void main(String[] args) throws IOException {
        WasServer server = new WasServer(PORT_NUMBER);
        server.run();
    }
}
