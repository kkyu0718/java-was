package codesquad.utils;

import codesquad.model.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

public class UploadUtils {
    private static final Logger logger = LoggerFactory.getLogger(UploadUtils.class);
    private static final String UPLOAD_DIR = System.getProperty("user.home") + File.separator + "uploads";

    public static void createUploadDir() {
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            if (!uploadDir.mkdirs()) {
                logger.error("Failed to create upload directory");
            }
        }
    }

    public static String saveFileToLocalResource(MultipartFile file) {
        String uniqueFileName = UUID.randomUUID() + "_" + file.getFileName();
        File targetFile = new File(UPLOAD_DIR, uniqueFileName);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile))) {
            bos.write(file.getContent(), 0, file.getContent().length);
        } catch (IOException e) {
            logger.error("Error saving file", e);
        }

        logger.debug("File saved at: {}", targetFile.getAbsolutePath());
        return "/uploads/" + uniqueFileName;
    }
}
