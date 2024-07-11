package codesquad.reader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

class StaticFileReaderTest {
    StaticFileReader reader;

    @BeforeEach
    void setup() {
        reader = new StaticFileReader();
    }

    @Test
    void readFileLines_메서드가_파일내용을_정확히_String으로_반환하는지_검증한다() throws IOException {
        String content = reader.readFileLines("index.html");

        Assertions.assertNotNull(content);
        Assertions.assertTrue(content.contains("<!DOCTYPE html>"));
        Assertions.assertTrue(content.contains("<html>"));
        Assertions.assertTrue(content.contains("</html>"));
    }

    @Test
    void readFileLines_메서드가_존재하지_않는_파일에_대해_예외를_던지는지_검증한다() {
        Assertions.assertThrows(IOException.class, () -> {
            reader.readFileLines("non_existent_file.html");
        });
    }

    @Test
    void readFileLines_메서드가_빈_파일을_처리할_수_있는지_검증한다() throws IOException {
        // 빈 파일이 'empty.txt'라는 이름으로 클래스패스에 존재한다고 가정
        String content = reader.readFileLines("empty.txt");

        Assertions.assertEquals("", content);
    }
}