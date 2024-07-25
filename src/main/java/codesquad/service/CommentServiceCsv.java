package codesquad.service;

import codesquad.exception.NotFoundException;
import codesquad.model.Comment;
import codesquad.model.dao.CommentCreateDao;
import codesquad.utils.CsvFileHandler;
import codesquad.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommentServiceCsv implements CommentServiceSpec {
    private static final Logger logger = LoggerFactory.getLogger(CommentServiceCsv.class);
    private final CsvFileHandler csvFileHandler;

    public CommentServiceCsv(String csvFilePath) {
        this.csvFileHandler = new CsvFileHandler(csvFilePath, "commentId,postId,userId,content" + StringUtils.LINE_SEPERATOR);
    }

    @Override
    public void createComment(CommentCreateDao dao) {
        int nextId = getNextId();
        Comment comment = new Comment(nextId, dao.getPostId(), dao.getUserId(), dao.getContent());

        csvFileHandler.appendToCsvFile(new String[]{
                String.valueOf(comment.getCommentId()), String.valueOf(comment.getPostId()), comment.getUserId(), comment.getContent()
        });

        logger.info("Comment created: {}", comment);
    }

    @Override
    public Comment getComment(Long id) {
        List<String[]> records = csvFileHandler.readCsvFile();
        for (String[] values : records) {
            if (values[0].equals(id.toString())) {
                return new Comment(Integer.parseInt(values[0]), Integer.parseInt(values[1]), values[2], values[3]);
            }
        }
        throw new NotFoundException("Comment not found: " + id);
    }

    @Override
    public List<Comment> getComments() {
        List<Comment> comments = new ArrayList<>();
        List<String[]> records = csvFileHandler.readCsvFile();
        for (String[] values : records) {
            comments.add(new Comment(Integer.parseInt(values[0]), Integer.parseInt(values[1]), values[2], values[3]));
        }
        Collections.reverse(comments);
        return comments;
    }

    @Override
    public List<Comment> getCommentsByPostId(Long postId) {
        List<Comment> comments = new ArrayList<>();
        List<String[]> records = csvFileHandler.readCsvFile();
        for (String[] values : records) {
            if (values[1].equals(postId.toString())) {
                comments.add(new Comment(Integer.parseInt(values[0]), Integer.parseInt(values[1]), values[2], values[3]));
            }
        }
        return comments;
    }

    private int getNextId() {
        int maxId = 0;
        List<String[]> records = csvFileHandler.readCsvFile();
        for (String[] values : records) {
            int id = Integer.parseInt(values[0]);
            if (id > maxId) {
                maxId = id;
            }
        }
        return maxId + 1;
    }
}
