package codesquad.reader;

import codesquad.exception.NotFoundException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

class StaticFileReaderTest {
    StaticFileReader reader;

    @BeforeEach
    void setup() {
        reader = new StaticFileReader();
    }

    @Test
    void readFileLines_메서드가_파일내용을_정확히_String으로_반환하는지_검증한다() throws IOException {
        String content = reader.readFileLinesWithPrefix("/index.html");

        Assertions.assertNotNull(content);
        Assertions.assertTrue(content.contains("<!DOCTYPE html>"));
        Assertions.assertTrue(content.contains("<html>"));
        Assertions.assertTrue(content.contains("</html>"));
    }

    @Test
    void readFileLines_메서드가_존재하지_않는_파일에_대해_예외를_던지는지_검증한다() {
        Assertions.assertThrows(NotFoundException.class, () -> {
            reader.readFileLinesWithPrefix("/non_existent_file.html");
        });
    }

    @Test
    void readFileLines_메서드가_빈_파일을_처리할_수_있는지_검증한다() throws IOException {
        // 빈 파일이 'empty.txt'라는 이름으로 클래스패스에 존재한다고 가정
        String content = reader.readFileLinesWithPrefix("/empty.txt");

        Assertions.assertEquals("", content);
    }

    @Test
    void readFileBytes_메서드가_파일내용을_정확히_byte배열로_반환하는지_검증한다() throws IOException {
        byte[] content = reader.readFileBytesWithPrefix("/index.html");

        Assertions.assertNotNull(content);
        String contentString = new String(content);
        Assertions.assertTrue(contentString.contains("<!DOCTYPE html>"));
        Assertions.assertTrue(contentString.contains("<html>"));
        Assertions.assertTrue(contentString.contains("</html>"));
    }

    @Test
    void readFileBytes_메서드가_존재하지_않는_파일에_대해_예외를_던지는지_검증한다() {
        Assertions.assertThrows(NotFoundException.class, () -> {
            reader.readFileBytesWithPrefix("/non_existent_file.html");
        });
    }

    @Test
    void checkExistWithPrefix_메서드가_파일_존재여부를_정확히_반환하는지_검증한다() {
        boolean exists = reader.checkExistWithPrefix("/index.html");
        Assertions.assertTrue(exists);

        boolean notExists = reader.checkExistWithPrefix("/non_existent_file.html");
        Assertions.assertFalse(notExists);
    }

    @Test
    void getResourceAsStream_메서드가_InputStream을_정확히_반환하는지_검증한다() throws IOException {
        InputStream inputStream = reader.getResourceAsStream("static/index.html");

        Assertions.assertNotNull(inputStream);
        String content = new String(inputStream.readAllBytes());
        Assertions.assertTrue(content.contains("<!DOCTYPE html>"));
        Assertions.assertTrue(content.contains("<html>"));
        Assertions.assertTrue(content.contains("</html>"));
    }

    @Test
    void getResourceAsStream_메서드가_존재하지_않는_파일에_대해_null을_반환하는지_검증한다() throws IOException {
        InputStream inputStream = reader.getResourceAsStream("static/non_existent_file.html");

        Assertions.assertNull(inputStream);
    }
}
