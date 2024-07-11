package codesquad.adapter;

import java.io.IOException;

public interface StaticFileAdapterSpec {
    public String readFileLines(String path) throws IOException;

}
