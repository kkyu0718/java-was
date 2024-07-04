package codesquad.global;

public class Path {
    private String path;

    private Path(String path) {
        this.path = path;
    }

    public static Path of(String path) {
        return new Path(path);
    }

    public String getPath() {
        return path;
    }

    public boolean isFilePath() {
        return path.split("\\.").length == 2;
    }

    public String getExt() {
        if (isFilePath()) {
            return path.split("\\.")[1];
        }

        throw new IllegalArgumentException("확장자가 존재하지 않습니다. " + path);
    }
}
