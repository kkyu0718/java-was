package codesquad.handler;

import java.io.IOException;

public interface StaticFileReaderSpec {

    byte[] readFile(String path) throws IOException;

    boolean exists(String path) throws IOException;
}
