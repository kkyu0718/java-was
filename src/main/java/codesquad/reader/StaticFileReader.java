package codesquad.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class StaticFileReader implements StaticFileReaderSpec {
    private static Logger logger = LoggerFactory.getLogger(StaticFileReader.class);
    private static String staticPath = "static";

    public StaticFileReader() {
    }

    @Override
    public byte[] readFile(String path) throws IOException {
        try (InputStream resource = this.getClass().getClassLoader().getResourceAsStream(staticPath + "/" + path.toString())) {
            return resource.readAllBytes();
        }
    }

    @Override
    public boolean exists(String path) throws IOException {
        try (InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(staticPath + "/" + path.toString())) {
            return resourceAsStream != null;
        }
    }
}
