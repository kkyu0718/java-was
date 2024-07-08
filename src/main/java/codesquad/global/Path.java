package codesquad.global;

import java.util.Objects;

public class Path {
    private String path;

    public Path(String path) {
        this.path = path;
    }

    public static Path of(String pathString) {
        return new Path(pathString);
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

    @Override
    public String toString() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path1 = (Path) o;
        return Objects.equals(path, path1.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }
}
