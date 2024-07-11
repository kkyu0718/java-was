package codesquad.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class StaticFileReader implements StaticFileReaderSpec {
    private static Logger logger = LoggerFactory.getLogger(StaticFileReader.class);
    private static String staticPath = "static";

    public StaticFileReader() {
    }

    private InputStream getResourceAsStream(String path) {
        return this.getClass().getClassLoader().getResourceAsStream(staticPath + "/" + path);
    }

    @Override
    public String readFileLines(String path) throws IOException {
        try (InputStream resourceAsStream = getResourceAsStream(path)) {
            if (resourceAsStream == null) {
                throw new IOException("File not found: " + path);
            }
            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
            }
            return content.toString();
        }
    }

}
