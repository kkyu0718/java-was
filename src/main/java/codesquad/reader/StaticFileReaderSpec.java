package codesquad.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public interface StaticFileReaderSpec {
    Logger logger = LoggerFactory.getLogger(StaticFileReaderSpec.class);

    default String readFileLines(String path) throws IOException {
        try (InputStream resourceAsStream = getResourceAsStream(path)) {
            checkExist(path);

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

    default byte[] readFileBytes(String path) throws IOException {
        try (InputStream resourceAsStream = getResourceAsStream(path)) {
            checkExist(path);

            return resourceAsStream.readAllBytes();
        }
    }

    default boolean checkExist(String path) {
        try (InputStream resourceAsStream = getResourceAsStream(path)) {
            logger.info("Check exist file: {}", path);
            logger.info("Resource as stream: {}", resourceAsStream);
            if (resourceAsStream == null) {
                return false;
            }
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    boolean checkExistWithPrefix(String path);

    String readFileLinesWithPrefix(String path) throws IOException;

    byte[] readFileBytesWithPrefix(String path) throws IOException;

    InputStream getResourceAsStream(String path) throws IOException;
}
