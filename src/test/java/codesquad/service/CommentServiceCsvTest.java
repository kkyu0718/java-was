package codesquad.service;

import codesquad.exception.NotFoundException;
import codesquad.model.Comment;
import codesquad.model.dao.CommentCreateDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommentServiceCsvTest {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String CSV_FILE_PATH = TEMP_DIR + File.separator + "comments.csv";

    private CommentServiceCsv commentService;

    @BeforeEach
    public void setUp() throws IOException {
        // Ensure the CSV file is deleted before each test
        Files.deleteIfExists(Paths.get(CSV_FILE_PATH));
        commentService = new CommentServiceCsv(CSV_FILE_PATH);
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Clean up the CSV file after each test
        Files.deleteIfExists(Paths.get(CSV_FILE_PATH));
    }

    @Test
    public void testCreateComment() {
        // Given
        CommentCreateDao dao = new CommentCreateDao(1, "user1", "첫번째 댓글 내용");

        // When
        commentService.createComment(dao);

        // Then
        List<Comment> comments = commentService.getComments();
        assertEquals(1, comments.size());
        assertEquals(1, comments.get(0).getPostId());
        assertEquals("user1", comments.get(0).getUserId());
        assertEquals("첫번째 댓글 내용", comments.get(0).getContent());
    }

    @Test
    public void testGetCommentById() {
        // Given
        CommentCreateDao dao1 = new CommentCreateDao(1, "user1", "첫번째 댓글 내용");
        CommentCreateDao dao2 = new CommentCreateDao(1, "user2", "두번째 댓글 내용");
        commentService.createComment(dao1);
        commentService.createComment(dao2);

        // When
        Comment comment = commentService.getComment(2L);

        // Then
        assertEquals(2, comment.getCommentId());
        assertEquals(1, comment.getPostId());
        assertEquals("user2", comment.getUserId());
        assertEquals("두번째 댓글 내용", comment.getContent());
    }

    @Test
    public void testGetCommentByIdNotFound() {
        // Given
        CommentCreateDao dao = new CommentCreateDao(1, "user1", "첫번째 댓글 내용");
        commentService.createComment(dao);

        // When, Then
        assertThrows(NotFoundException.class, () -> commentService.getComment(2L));
    }

    @Test
    public void testGetCommentsInReverseOrder() {
        // Given
        CommentCreateDao dao1 = new CommentCreateDao(1, "user1", "첫번째 댓글 내용");
        CommentCreateDao dao2 = new CommentCreateDao(1, "user2", "두번째 댓글 내용");
        commentService.createComment(dao1);
        commentService.createComment(dao2);

        // When
        List<Comment> comments = commentService.getComments();

        // Then
        assertEquals(2, comments.size());
        assertEquals(2, comments.get(0).getCommentId());
        assertEquals(1, comments.get(0).getPostId());
        assertEquals("user2", comments.get(0).getUserId());
        assertEquals("두번째 댓글 내용", comments.get(0).getContent());
        assertEquals(1, comments.get(1).getCommentId());
        assertEquals(1, comments.get(1).getPostId());
        assertEquals("user1", comments.get(1).getUserId());
        assertEquals("첫번째 댓글 내용", comments.get(1).getContent());
    }

    @Test
    public void testGetCommentsByPostId() {
        // Given
        CommentCreateDao dao1 = new CommentCreateDao(1, "user1", "첫번째 댓글 내용");
        CommentCreateDao dao2 = new CommentCreateDao(2, "user2", "두번째 댓글 내용");
        CommentCreateDao dao3 = new CommentCreateDao(1, "user3", "세번째 댓글 내용");
        commentService.createComment(dao1);
        commentService.createComment(dao2);
        commentService.createComment(dao3);

        // When
        List<Comment> commentsForPost1 = commentService.getCommentsByPostId(1L);

        // Then
        assertEquals(2, commentsForPost1.size());
        assertEquals(1, commentsForPost1.get(0).getPostId());
        assertEquals("user1", commentsForPost1.get(0).getUserId());
        assertEquals("첫번째 댓글 내용", commentsForPost1.get(0).getContent());
        assertEquals(1, commentsForPost1.get(1).getPostId());
        assertEquals("user3", commentsForPost1.get(1).getUserId());
        assertEquals("세번째 댓글 내용", commentsForPost1.get(1).getContent());
    }
}
