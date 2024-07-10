package codesquad.reader;

import java.io.IOException;

public interface StaticFileReaderSpec {

    byte[] readFile(String path) throws IOException;

    boolean exists(String path) throws IOException;
}
