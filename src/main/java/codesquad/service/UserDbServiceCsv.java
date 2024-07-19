package codesquad.service;

import codesquad.exception.NotFoundException;
import codesquad.model.User;
import codesquad.utils.CsvFileHandler;
import codesquad.utils.StringUtils;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class UserDbServiceCsv implements UserDbServiceSpec {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(UserDbServiceCsv.class);
    private final CsvFileHandler csvFileHandler;

    public UserDbServiceCsv(String csvFilePath) {
        this.csvFileHandler = new CsvFileHandler(csvFilePath, "user_id,password,name,email" + StringUtils.LINE_SEPERATOR);
    }

    @Override
    public User getUser(String userId) {
        List<String[]> records = csvFileHandler.readCsvFile();
        for (String[] values : records) {
            if (values[0].equals(userId)) {
                return User.of(values[0], values[1], values[2], values[3]);
            }
        }
        throw new NotFoundException("User not found: " + userId);
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        List<String[]> records = csvFileHandler.readCsvFile();
        for (String[] values : records) {
            users.add(User.of(values[0], values[1], values[2], values[3]));
        }
        return users;
    }

    @Override
    public boolean exists(String userId) {
        List<String[]> records = csvFileHandler.readCsvFile();
        for (String[] values : records) {
            if (values[0].equals(userId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void add(User user) {
        csvFileHandler.appendToCsvFile(new String[]{
                user.getUserId(), user.getPassword(), user.getName(), user.getEmail()
        });

        logger.info("User added: {}", user);
    }
}
