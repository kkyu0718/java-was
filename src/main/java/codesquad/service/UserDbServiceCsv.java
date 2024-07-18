package codesquad.service;

import codesquad.exception.InternalServerError;
import codesquad.exception.NotFoundException;
import codesquad.model.User;
import codesquad.utils.StringUtils;
import org.slf4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
                Files.write(path, CSV_HEADER.getBytes());
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

        try (RandomAccessFile file = new RandomAccessFile(CSV_FILE_PATH, "r")) {
            file.seek(position);
            String line = file.readLine();
            String[] values = line.split(",");
            return User.of(values[0], values[1], values[2], values[3]);
        } catch (IOException e) {
            throw new InternalServerError("Failed to read CSV file: " + e.getMessage());
        }
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(CSV_FILE_PATH))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
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
        try (RandomAccessFile file = new RandomAccessFile(CSV_FILE_PATH, "rw")) {
            file.seek(file.length());
            long position = file.getFilePointer();
            String userLine = String.format("%s,%s,%s,%s%n",
                    user.getUserId(), user.getPassword(), user.getName(), user.getEmail());
            file.writeBytes(userLine);

            // Update index
            userIdToPositionIndex.put(user.getUserId(), position);

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