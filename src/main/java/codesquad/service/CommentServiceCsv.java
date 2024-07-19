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
        this.csvFileHandler = new CsvFileHandler(csvFilePath, "commentId,userId,content" + StringUtils.LINE_SEPERATOR);
    }

    @Override
    public void createComment(CommentCreateDao dao) {
        long nextId = getNextId();
        Comment comment = new Comment(nextId, dao.getUserId(), dao.getContent());

        csvFileHandler.appendToCsvFile(new String[]{
                String.valueOf(comment.getCommentId()), comment.getUserId(), comment.getContent()
        });

        logger.info("Comment created: {}", comment);
    }

    @Override
    public Comment getComment(Long id) {
        List<String[]> records = csvFileHandler.readCsvFile();
        for (String[] values : records) {
            if (values[0].equals(id.toString())) {
                return new Comment(Long.parseLong(values[0]), values[1], values[2]);
            }
        }
        throw new NotFoundException("Comment not found: " + id);
    }

    @Override
    public List<Comment> getComments() {
        List<Comment> comments = new ArrayList<>();
        List<String[]> records = csvFileHandler.readCsvFile();
        for (String[] values : records) {
            comments.add(new Comment(Long.parseLong(values[0]), values[1], values[2]));
        }
        Collections.reverse(comments);
        return comments;
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
