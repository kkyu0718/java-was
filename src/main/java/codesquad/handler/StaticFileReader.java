package codesquad.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class StaticFileReader implements StaticFileReaderSpec {
    private final String resourceRootPath;

    public StaticFileReader(String resourceRootPath) {
        this.resourceRootPath = resourceRootPath;
    }

    @Override
    public byte[] readFile(String path) throws IOException {
        File filePath = getFilePath(path);
        byte[] bytes = new byte[(int) filePath.length()];

        try (FileInputStream fis = new FileInputStream(filePath)) {
            fis.read(bytes);
        }

        return bytes;
    }

    private File getFilePath(String path) {
        return new File(resourceRootPath + "/" + path);
    }

    @Override
    public boolean exists(String path) {
        File filePath = getFilePath(path);
        return filePath.exists();
    }
}
