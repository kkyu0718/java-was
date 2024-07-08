package codesquad.handler;

import codesquad.global.Path;

import java.io.IOException;

public interface StaticFileReaderSpec {

    byte[] readFile(Path path) throws IOException;

    boolean exists(Path path) throws IOException;
}
