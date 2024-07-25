package codesquad.reader;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SystemFileReaderTest {
    private SystemFileReader reader;
    private static String testDirPath;
    private static String testFilePath;
    private static String emptyFilePath;
    private static String nonExistentFilePath;

    @BeforeAll
    static void setupClass() throws IOException {
        testDirPath = System.getProperty("user.home") + "/test_files";
        testFilePath = testDirPath + "/test_file.txt";
        emptyFilePath = testDirPath + "/empty_file.txt";
        nonExistentFilePath = testDirPath + "/non_existent_file.txt";

        // Create test directory and files
        Files.createDirectories(Path.of(testDirPath));
        Files.write(Path.of(testFilePath), "<!DOCTYPE html>\n<html>\n</html>".getBytes());
        Files.createFile(Path.of(emptyFilePath));
    }

    @AfterAll
    static void teardownClass() throws IOException {
        // Delete test files and directory
        Files.deleteIfExists(Path.of(testFilePath));
        Files.deleteIfExists(Path.of(emptyFilePath));
        Files.deleteIfExists(Path.of(nonExistentFilePath));
        Files.deleteIfExists(Path.of(testDirPath));
    }

    @BeforeEach
    void setup() {
        reader = new SystemFileReader();
    }

    @Test
    void readFileLines_메서드가_파일내용을_정확히_String으로_반환하는지_검증한다() throws IOException {
        String content = reader.readFileLinesWithPrefix("/test_files/test_file.txt");

        assertNotNull(content);
        assertTrue(content.contains("<!DOCTYPE html>"));
        assertTrue(content.contains("<html>"));
        assertTrue(content.contains("</html>"));
    }

    @Test
    void readFileLines_메서드가_존재하지_않는_파일에_대해_예외를_던지는지_검증한다() {
        assertThrows(FileNotFoundException.class, () -> {
            reader.readFileLinesWithPrefix("/test_files/non_existent_file.txt");
        });
    }

    @Test
    void readFileLines_메서드가_빈_파일을_처리할_수_있는지_검증한다() throws IOException {
        String content = reader.readFileLinesWithPrefix("/test_files/empty_file.txt");

        assertEquals("", content);
    }

    @Test
    void readFileBytes_메서드가_파일내용을_정확히_byte배열로_반환하는지_검증한다() throws IOException {
        byte[] content = reader.readFileBytesWithPrefix("/test_files/test_file.txt");

        assertNotNull(content);
        String contentString = new String(content);
        assertTrue(contentString.contains("<!DOCTYPE html>"));
        assertTrue(contentString.contains("<html>"));
        assertTrue(contentString.contains("</html>"));
    }

    @Test
    void readFileBytes_메서드가_존재하지_않는_파일에_대해_예외를_던지는지_검증한다() {
        assertThrows(FileNotFoundException.class, () -> {
            reader.readFileBytesWithPrefix("/test_files/non_existent_file.txt");
        });
    }

    @Test
    void checkExistWithPrefix_메서드가_파일_존재여부를_정확히_반환하는지_검증한다() {
        boolean exists = reader.checkExistWithPrefix("/test_files/test_file.txt");
        assertTrue(exists);

        boolean notExists = reader.checkExistWithPrefix("/test_files/non_existent_file.txt");
        assertFalse(notExists);
    }

    @Test
    void getResourceAsStream_메서드가_InputStream을_정확히_반환하는지_검증한다() throws IOException {
        InputStream inputStream = reader.getResourceAsStream(System.getProperty("user.home") + "/test_files/test_file.txt");

        assertNotNull(inputStream);
        String content = new String(inputStream.readAllBytes());
        assertTrue(content.contains("<!DOCTYPE html>"));
        assertTrue(content.contains("<html>"));
        assertTrue(content.contains("</html>"));
    }

    @Test
    void getResourceAsStream_메서드가_존재하지_않는_파일에_대해_500에러을_반환하는지_검증한다() throws IOException {

        assertThrows(FileNotFoundException.class, () -> reader.getResourceAsStream(System.getProperty("user.home") + "/test_files/non_existent_file.txt"));
    }
}
