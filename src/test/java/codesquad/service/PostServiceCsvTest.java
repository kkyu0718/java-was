package codesquad.service;

import codesquad.exception.NotFoundException;
import codesquad.model.MultipartFile;
import codesquad.model.Post;
import codesquad.model.dao.PostCreateDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PostServiceCsvTest {
    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String CSV_FILE_PATH = TEMP_DIR + File.separator + "posts.csv";

    private PostServiceCsv postService;

    @BeforeEach
    public void setUp() throws IOException {
        // Ensure the CSV file is deleted before each test
        Files.deleteIfExists(Paths.get(CSV_FILE_PATH));
        postService = new PostServiceCsv(CSV_FILE_PATH);
    }

    @AfterEach
    public void tearDown() throws IOException {
        // Clean up the CSV file after each test
        Files.deleteIfExists(Paths.get(CSV_FILE_PATH));
    }

    @Test
    public void 게시물을_추가하면_정상적으로_저장된다() {
        // Given
        MultipartFile file = new MultipartFile("image1.jpg", "image/jpeg", "image content".getBytes());
        PostCreateDao dao = new PostCreateDao("user1", "첫번째 게시물 내용", file, "image1.jpg");

        // When
        postService.createPost(dao);

        // Then
        List<Post> posts = postService.getPosts();
        assertEquals(1, posts.size());
        assertEquals("user1", posts.get(0).getUserId());
        assertEquals("첫번째 게시물 내용", posts.get(0).getPostContent());
        assertEquals("image1.jpg", posts.get(0).getImageUrl());
    }

    @Test
    public void 게시물을_ID로_조회하면_정상적으로_반환된다() {
        // Given
        MultipartFile file1 = new MultipartFile("image1.jpg", "image/jpeg", "image content".getBytes());
        MultipartFile file2 = new MultipartFile("image2.jpg", "image/jpeg", "image content".getBytes());
        PostCreateDao dao1 = new PostCreateDao("user1", "첫번째 게시물 내용", file1, "image1.jpg");
        PostCreateDao dao2 = new PostCreateDao("user2", "두번째 게시물 내용", file2, "image2.jpg");
        postService.createPost(dao1);
        postService.createPost(dao2);

        // When
        Post post = postService.getPost(2L);

        // Then
        assertNotNull(post);
        assertEquals(2L, post.getId());
        assertEquals("user2", post.getUserId());
        assertEquals("두번째 게시물 내용", post.getPostContent());
        assertEquals("image2.jpg", post.getImageUrl());
    }

    @Test
    public void 존재하지_않는_ID로_조회하면_NotFoundException이_발생한다() {
        // Given
        MultipartFile file = new MultipartFile("image1.jpg", "image/jpeg", "image content".getBytes());
        PostCreateDao dao = new PostCreateDao("user1", "첫번째 게시물 내용", file, "image1.jpg");
        postService.createPost(dao);

        // When & Then
        assertThrows(NotFoundException.class, () -> postService.getPost(2L));
    }

    @Test
    public void 모든_게시물을_조회하면_역순으로_반환된다() {
        // Given
        MultipartFile file1 = new MultipartFile("image1.jpg", "image/jpeg", "image content".getBytes());
        MultipartFile file2 = new MultipartFile("image2.jpg", "image/jpeg", "image content".getBytes());
        PostCreateDao dao1 = new PostCreateDao("user1", "첫번째 게시물 내용", file1, "image1.jpg");
        PostCreateDao dao2 = new PostCreateDao("user2", "두번째 게시물 내용", file2, "image2.jpg");
        postService.createPost(dao1);
        postService.createPost(dao2);

        // When
        List<Post> posts = postService.getPosts();

        // Then
        assertEquals(2, posts.size());
        assertEquals(2L, posts.get(0).getId());
        assertEquals("user2", posts.get(0).getUserId());
        assertEquals("두번째 게시물 내용", posts.get(0).getPostContent());
        assertEquals("image2.jpg", posts.get(0).getImageUrl());
        assertEquals(1L, posts.get(1).getId());
        assertEquals("user1", posts.get(1).getUserId());
        assertEquals("첫번째 게시물 내용", posts.get(1).getPostContent());
        assertEquals("image1.jpg", posts.get(1).getImageUrl());
    }
}
