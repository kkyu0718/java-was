package codesquad.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MimeTypeTest {
    @Test
    void MimeType_Enum이_주어지고_css파일을_변환하면_MimeType_CSS로_변환된다() {
        MimeType mimeType = MimeType.fromExt("index.css");

        Assertions.assertEquals(MimeType.CSS, mimeType);
    }

    @Test
    void MimeType_Enum이_주어지고_js파일을_변환하면_MimeType_JS로_변환된다() {
        MimeType mimeType = MimeType.fromExt("index.js");

        Assertions.assertEquals(MimeType.JS, mimeType);
    }

    @Test
    void MimeType_Enum이_주어지고_html파일을_변환하면_MimeType_HTML로_변환된다() {
        MimeType mimeType = MimeType.fromExt("index.html");

        Assertions.assertEquals(MimeType.HTML, mimeType);
    }

    @Test
    void MimeType_Enum이_주어지고_정의되지않은_확장자_파일을_변환하면_Illegal_Argument에러가_발생한다() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> MimeType.fromExt("index.abcd"));
    }
}