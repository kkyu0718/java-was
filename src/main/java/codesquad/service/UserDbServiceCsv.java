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
import java.util.List;

public class UserDbServiceCsv implements UserDbServiceSpec {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(UserDbServiceCsv.class);
    private String csvFilePath = System.getProperty("user.home")
            + File.separator
            + "database"
            + File.separator
            + "users.csv";
    private static final String CSV_HEADER = "user_id,password,name,email" + StringUtils.LINE_SEPERATOR;

    public UserDbServiceCsv() {
        initializeCsvFileIfNotExists();
    }

    private void initializeCsvFileIfNotExists() {
        Path path = Paths.get(csvFilePath);
        if (!Files.exists(path)) {
            try {
                Files.write(path, CSV_HEADER.getBytes());
            } catch (IOException e) {
                throw new InternalServerError("Failed to initialize CSV file: " + e.getMessage());
            }
        }
    }

    @Override
    public User getUser(String userId) {
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values[0].equals(userId)) {
                    return User.of(values[0], values[1], values[2], values[3]);
                }
            }
        } catch (IOException e) {
            throw new InternalServerError("Failed to read CSV file: " + e.getMessage());
        }
        throw new NotFoundException("User not found: " + userId);
    }

    @Override
    public List<User> getUsers() {
        List<User> users = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
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
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",");
                if (values[0].equals(userId)) {
                    return true;
                }
            }
        } catch (IOException e) {
            throw new InternalServerError("Failed to read CSV file: " + e.getMessage());
        }
        return false;
    }

    @Override
    public void add(User user) {
        try (FileWriter writer = new FileWriter(csvFilePath, true)) {
            String userLine = String.format("%s,%s,%s,%s%n",
                    user.getUserId(), user.getPassword(), user.getName(), user.getEmail());
            writer.append(userLine);

            logger.info("User added: {}", user);
        } catch (IOException e) {
            throw new InternalServerError("Failed to write to CSV file: " + e.getMessage());
        }
    }

    // Helper method to update or delete users
    private void updateCsvFile(List<String> lines) {
        try {
            Files.write(Paths.get(csvFilePath), lines);
        } catch (IOException e) {
            throw new InternalServerError("Failed to update CSV file: " + e.getMessage());
        }
    }
}