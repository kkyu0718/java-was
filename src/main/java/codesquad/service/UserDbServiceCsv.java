package codesquad.service;

import codesquad.exception.InternalServerError;
import codesquad.exception.NotFoundException;
import codesquad.model.User;
import codesquad.utils.StringUtils;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserDbServiceCsv implements UserDbServiceSpec {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(UserDbServiceCsv.class);
    private static final String CSV_FILE_PATH = System.getProperty("user.home")
            + File.separator
            + "database"
            + File.separator
            + "users.csv";
    private static final String CSV_HEADER = "user_id,password,name,email" + StringUtils.LINE_SEPERATOR;

    private final Map<String, Long> userIdToPositionIndex = new HashMap<>();

    public UserDbServiceCsv() {
        initializeCsvFileIfNotExists();
        buildIndex();
    }

    private void initializeCsvFileIfNotExists() {
        Path path = Paths.get(CSV_FILE_PATH);
        if (!Files.exists(path)) {
            try {
                Files.write(path, CSV_HEADER.getBytes(StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new InternalServerError("Failed to initialize CSV file: " + e.getMessage());
            }
        }
    }

    private void buildIndex() {
        try (RandomAccessFile file = new RandomAccessFile(CSV_FILE_PATH, "r")) {
            String line;
            long position = 0;
            file.readLine(); // Skip header
            while ((line = file.readLine()) != null) {
                String[] values = line.split(",");
                userIdToPositionIndex.put(values[0], position);
                position = file.getFilePointer();
            }
        } catch (IOException e) {
            throw new InternalServerError("Failed to build index: " + e.getMessage());
        }
    }

    @Override
    public User getUser(String userId) {
        Long position = userIdToPositionIndex.get(userId);
        if (position == null) {
            throw new NotFoundException("User not found: " + userId);
        }

        try {
            List<String> lines = Files.readAllLines(Paths.get(CSV_FILE_PATH), StandardCharsets.UTF_8);
            String line = lines.get(position.intValue() + 1); // +1 to skip header
            String[] values = line.split(",");
            return User.of(values[0], values[1], values[2], values[3]);
        } catch (IOException e) {
            throw new InternalServerError("Failed to read CSV file: " + e.getMessage());
        }
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(CSV_FILE_PATH), StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) { // Start from 1 to skip header
                String[] values = lines.get(i).split(",");
                users.add(User.of(values[0], values[1], values[2], values[3]));
            }
        } catch (IOException e) {
            throw new InternalServerError("Failed to read CSV file: " + e.getMessage());
        }
        return users;
    }

    @Override
    public boolean exists(String userId) {
        return userIdToPositionIndex.containsKey(userId);
    }

    @Override
    public void add(User user) {
        try {
            String userLine = String.format("%s,%s,%s,%s%n",
                    user.getUserId(), user.getPassword(), user.getName(), user.getEmail());
            Files.write(Paths.get(CSV_FILE_PATH),
                    userLine.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND);

            // Update index
            userIdToPositionIndex.put(user.getUserId(), (long) userIdToPositionIndex.size());

            logger.info("User added: {}", user);
        } catch (IOException e) {
            throw new InternalServerError("Failed to write to CSV file: " + e.getMessage());
        }
    }

    // Helper method to update or delete users
    private void updateCsvFile(List<String> lines) {
        try {
            Files.write(Paths.get(CSV_FILE_PATH), lines);
            buildIndex(); // Rebuild index after update
        } catch (IOException e) {
            throw new InternalServerError("Failed to update CSV file: " + e.getMessage());
        }
    }
}