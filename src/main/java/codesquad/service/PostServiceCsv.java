package codesquad.service;

import codesquad.exception.InternalServerError;
import codesquad.exception.NotFoundException;
import codesquad.model.Post;
import codesquad.model.dao.PostCreateDao;
import codesquad.utils.StringUtils;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostServiceCsv implements PostServiceSpec {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PostServiceCsv.class);
    private static final String CSV_FILE_PATH = System.getProperty("user.home")
            + File.separator
            + "database"
            + File.separator
            + "posts.csv";
    private static final String CSV_HEADER = "id,userId,postContent,imageUrl" + StringUtils.LINE_SEPERATOR;

    public PostServiceCsv() {
        initializeCsvFileIfNotExists();
    }

    private void initializeCsvFileIfNotExists() {
        Path path = Paths.get(CSV_FILE_PATH);
        if (!Files.exists(path)) {
            try {
                Files.write(path, CSV_HEADER.getBytes());
            } catch (IOException e) {
                throw new InternalServerError("Failed to initialize CSV file: " + e.getMessage());
            }
        }
    }

    @Override
    public void createPost(PostCreateDao dao) {
        long nextId = getNextId();
        Post post = new Post(nextId, dao.getUserId(), dao.getContent(), dao.getImageUrl());

        try (FileWriter writer = new FileWriter(CSV_FILE_PATH, true)) {
            String postLine = String.format("%d,%s,%s,%s%n",
                    post.getId(), post.getUserId(), post.getPostContent(), post.getImageUrl());
            writer.append(postLine);

            logger.info("Post created: {}", post);
        } catch (IOException e) {
            throw new InternalServerError("Failed to write to CSV file: " + e.getMessage());
        }
    }

    @Override
    public Post getPost(Long id) {
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values[0].equals(id.toString())) {
                    return new Post(Long.parseLong(values[0]), values[1], values[2], values[3]);
                }
            }
        } catch (IOException e) {
            throw new InternalServerError("Failed to read CSV file: " + e.getMessage());
        }
        throw new NotFoundException("Post not found: " + id);
    }

    @Override
    public List<Post> getPosts() {
        List<Post> posts = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                posts.add(new Post(Long.parseLong(values[0]), values[1], values[2], values[3]));
            }
        } catch (IOException e) {
            throw new InternalServerError("Failed to read CSV file: " + e.getMessage());
        }

        // 역순으로 반환
        Collections.reverse(posts); // Reverse the list to show the most recent posts first
        return posts;
    }

    private long getNextId() {
        long maxId = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                long id = Long.parseLong(values[0]);
                if (id > maxId) {
                    maxId = id;
                }
            }
        } catch (IOException e) {
            throw new InternalServerError("Failed to read CSV file: " + e.getMessage());
        }
        return maxId + 1;
    }
}
