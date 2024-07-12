package codesquad.reader;

import java.io.IOException;

public interface StaticFileReaderSpec {
    public String readFileLines(String path) throws IOException;

}
