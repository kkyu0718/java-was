package codesquad.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

public class StaticFileReader implements StaticFileReaderSpec {
    private static Logger logger = LoggerFactory.getLogger(StaticFileReader.class);
    private static String prefix = "static";

    public StaticFileReader() {
    }


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
        logger.debug("class loading {}", path);
        return this.getClass().getClassLoader().getResourceAsStream(path);
    }
}
