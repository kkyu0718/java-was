package codesquad.global;

import codesquad.http.Parameters;

public class Url {
    private Path path;
    private Parameters parameters;

    private Url(String path, Parameters parameters) {
        this.path = Path.of(path);
        this.parameters = parameters;
    }

    public static Url of(String urlString) {
        String[] splits = urlString.split("\\?");

        // no parameter
        if (splits.length == 1) {
            return new Url(splits[0], null);
        }

        // pameter exist
        return new Url(splits[0], Parameters.of(splits[1]));
    }

    public Parameters getParameters() {
        return parameters;
    }

    public Path getPath() {
        return path;
    }

}
