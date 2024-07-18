package codesquad.adapter;

import codesquad.db.DbConfig;
import codesquad.http.*;
import codesquad.service.PostServiceJdbc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PostAdapterTest {

    private DbConfig dbConfig;
    private PostServiceJdbc postService;
    private PostAdapter postAdapter;

    @BeforeEach
    void setUp() throws Exception {
        dbConfig = new DbConfig("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        postService = new PostServiceJdbc(dbConfig);
        postAdapter = new PostAdapter(postService);

        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS `User` (id VARCHAR(50) PRIMARY KEY, password VARCHAR(50), name VARCHAR(50), email VARCHAR(100))");
            stmt.execute("CREATE TABLE IF NOT EXISTS Post (id INT AUTO_INCREMENT PRIMARY KEY, user_id VARCHAR(50), content VARCHAR(100), image_url VARCHAR(200) NOT NULL)");
            stmt.execute("ALTER TABLE Post ADD FOREIGN KEY (user_id) REFERENCES `User`(id)");
            stmt.execute("INSERT INTO `User` VALUES ('user1', 'pass1', 'User One', 'user1@example.com')");
        }
    }

    @AfterEach
    void tearDown() throws Exception {
        try (Connection conn = dbConfig.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE IF EXISTS Post");
            stmt.execute("DROP TABLE IF EXISTS `User`");
        }
    }

    @Test
    void createPost() throws Exception {
        // Given
        String json = "{\"userId\":\"user1\", \"content\":\"Test content\"}";
        HttpRequest request = new HttpRequest.Builder(HttpMethod.POST, "/posts/create", HttpVersion.HTTP11)
                .body(HttpBody.of(json.getBytes(), MimeType.APPLICATION_JSON))
                .build();

        // When
        HttpResponse response = postAdapter.createPost(request);

        // Then
        assertEquals(HttpStatus.OK, response.getStatus());
        assertEquals(1, postService.getPosts().size());
        assertEquals("Test content", postService.getPosts().get(0).getPostContent());
    }
}
