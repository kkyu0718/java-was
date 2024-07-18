package codesquad.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SystemFileReader implements StaticFileReaderSpec {
    private static Logger logger = LoggerFactory.getLogger(SystemFileReader.class);
    private static String prefix = System.getProperty("user.home");

    @Override
    public boolean checkExistWithPrefix(String path) {
        return checkExist(prefix + path);
    }

    @Override
    public String readFileLinesWithPrefix(String path) throws IOException {
        return readFileLines(prefix + path);
    }

    @Override
    public byte[] readFileBytesWithPrefix(String path) throws IOException {
        return readFileBytes(prefix + path);
    }

    @Override
    public InputStream getResourceAsStream(String path) throws IOException {
        logger.debug("file input stream", path);
        return new FileInputStream(path);
    }
}
