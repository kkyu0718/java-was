package codesquad.service;

import codesquad.db.DbConfig;
import codesquad.exception.InternalServerError;
import codesquad.model.MultipartFile;
import codesquad.model.Post;
import codesquad.model.dao.PostCreateDao;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PostServiceJdbcTest {
    private PostServiceJdbc postServiceJdbc;
    private DbConfig dbConfig;
    private String existUserId1 = "user1";
    private String existUserId2 = "user2";

    @BeforeEach
    void setup() {
        dbConfig = new DbConfig("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        postServiceJdbc = new PostServiceJdbc(dbConfig);

        // 테스트 데이터베이스 설정
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS `User` (id VARCHAR(50) PRIMARY KEY, password VARCHAR(50), name VARCHAR(50), email VARCHAR(100))");
            stmt.execute("CREATE TABLE IF NOT EXISTS Post (id INT AUTO_INCREMENT PRIMARY KEY, user_id VARCHAR(50), content VARCHAR(100), image_url VARCHAR(100))");
            stmt.execute("ALTER TABLE Post ADD FOREIGN KEY (user_id) REFERENCES `User`(id)");
            stmt.execute(String.format("INSERT INTO `USER` VALUES ('%s', 'pass1', 'User One', 'user1@example.com')", existUserId1));
            stmt.execute(String.format("INSERT INTO `USER` VALUES ('%s', 'pass2', 'User Two', 'user2@example.com')", existUserId2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @AfterEach
    void cleanup() {
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS Post");
            stmt.execute("DROP TABLE IF EXISTS `User`");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void 주어진_유저가_존재할_때_게시글을_작성하면_성공한다() {
        // given
        MultipartFile file = new MultipartFile("image1.jpg", "image/jpeg", "image content".getBytes());
        PostCreateDao dao = new PostCreateDao(existUserId1, "content1", file, "image1.jpg");

        // when
        postServiceJdbc.createPost(dao);

        // then
        List<Post> posts = postServiceJdbc.getPosts();
        assertEquals(1, posts.size());
        assertEquals("content1", posts.get(0).getPostContent());
        assertEquals("image1.jpg", posts.get(0).getImageUrl());

    }

    @Test
    void 주어진_게시글이_존재할_때_게시글을_가져올_수_있다() {
        // given
        MultipartFile file = new MultipartFile("image1.jpg", "image/jpeg", "image content".getBytes());
        PostCreateDao dao = new PostCreateDao(existUserId1, "content1", file, "image1.jpg");

        // when
        postServiceJdbc.createPost(dao);

        // then
        Post post = postServiceJdbc.getPost(1L);
        assertNotNull(post);
        assertEquals("content1", post.getPostContent());
        assertEquals("image1.jpg", post.getImageUrl());
    }

    @Test
    void 여러_게시글이_존재할_때_모든_게시글을_가져올_수_있다() {
        // given
        MultipartFile file1 = new MultipartFile("image1.jpg", "image/jpeg", "image content".getBytes());
        MultipartFile file2 = new MultipartFile("image2.jpg", "image/jpeg", "image content".getBytes());
        PostCreateDao dao1 = new PostCreateDao(existUserId1, "First post", file1, "image1.jpg");
        PostCreateDao dao2 = new PostCreateDao(existUserId2, "Second post", file2, "image2.jpg");

        // when
        postServiceJdbc.createPost(dao1);
        postServiceJdbc.createPost(dao2);

        // then
        List<Post> posts = postServiceJdbc.getPosts();

        for (Post post : posts) {
            System.out.println(post);
        }

        assertEquals(2, posts.size());
        assertEquals("First post", posts.get(0).getPostContent());
        assertEquals("Second post", posts.get(1).getPostContent());
    }

    @Test
    void 주어진_유저가_존재하지_않을_때_게시글을_작성하면_실패한다() {
        // given
        MultipartFile file = new MultipartFile("image1.jpg", "image/jpeg", "image content".getBytes());
        PostCreateDao dao = new PostCreateDao("not-existing-user", "content1", file, "image1.jpg");

        // when & then
        assertThrows(InternalServerError.class, () -> postServiceJdbc.createPost(dao));
    }
}
