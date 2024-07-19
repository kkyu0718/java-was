package codesquad.service;

import codesquad.exception.NotFoundException;
import codesquad.model.Post;
import codesquad.model.dao.PostCreateDao;
import codesquad.utils.CsvFileHandler;
import codesquad.utils.StringUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PostServiceCsv implements PostServiceSpec {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(PostServiceCsv.class);
    private final CsvFileHandler csvFileHandler;

    public PostServiceCsv(String csvFilePath) {
        this.csvFileHandler = new CsvFileHandler(csvFilePath, "id,userId,postContent,imageUrl" + StringUtils.LINE_SEPERATOR);
    }

    @Override
    public void createPost(PostCreateDao dao) {
        long nextId = getNextId();
        Post post = new Post(nextId, dao.getUserId(), dao.getContent(), dao.getImageUrl());

        csvFileHandler.appendToCsvFile(new String[]{
                String.valueOf(post.getId()), post.getUserId(), post.getPostContent(), post.getImageUrl()
        });

        logger.info("Post created: {}", post);
    }

    @Override
    public Post getPost(Long id) {
        List<String[]> records = csvFileHandler.readCsvFile();
        for (String[] values : records) {
            if (values[0].equals(id.toString())) {
                return new Post(Long.parseLong(values[0]), values[1], values[2], values[3]);
            }
        }
        throw new NotFoundException("Post not found: " + id);
    }

    @Override
    public List<Post> getPosts() {
        List<Post> posts = new ArrayList<>();
        List<String[]> records = csvFileHandler.readCsvFile();
        for (String[] values : records) {
            posts.add(new Post(Long.parseLong(values[0]), values[1], values[2], values[3]));
        }
        Collections.reverse(posts);
        return posts;
    }

    private long getNextId() {
        long maxId = 0;
        List<String[]> records = csvFileHandler.readCsvFile();
        for (String[] values : records) {
            long id = Long.parseLong(values[0]);
            if (id > maxId) {
                maxId = id;
            }
        }
        return maxId + 1;
    }
}
