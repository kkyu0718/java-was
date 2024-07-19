package codesquad.utils;

import codesquad.exception.InternalServerError;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvFileHandler {
    private final String csvFilePath;
    private final String csvHeader;

    public CsvFileHandler(String csvFilePath, String csvHeader) {
        this.csvFilePath = csvFilePath;
        this.csvHeader = csvHeader;
        initializeFolederIfNotExists();
        initializeCsvFileIfNotExists();
    }

    private void initializeFolederIfNotExists() {
        // 사용자의 홈 디렉토리 경로 가져오기
        String userHome = System.getProperty("user.home");

        // database 폴더 경로
        String databaseFolderPath = userHome + File.separator + "database";

        // File 객체 생성
        File databaseFolder = new File(databaseFolderPath);

        // 폴더가 존재하지 않으면 생성
        if (!databaseFolder.exists()) {
            boolean created = databaseFolder.mkdirs(); // 폴더 생성
            if (created) {
                System.out.println("Database folder created successfully.");
            } else {
                System.out.println("Failed to create database folder.");
            }
        } else {
            System.out.println("Database folder already exists.");
        }
    }

    private void initializeCsvFileIfNotExists() {
        Path path = Paths.get(csvFilePath);
        if (!Files.exists(path)) {
            try {
                Files.write(path, csvHeader.getBytes());
            } catch (IOException e) {
                throw new InternalServerError("Failed to initialize CSV file: " + e.getMessage());
            }
        }
    }

    public List<String[]> readCsvFile() {
        List<String[]> records = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                records.add(line.split(","));
            }
        } catch (IOException e) {
            throw new InternalServerError("Failed to read CSV file: " + e.getMessage());
        }
        return records;
    }

    public void writeCsvFile(List<String[]> records) {
        try (FileWriter writer = new FileWriter(csvFilePath, false)) {
            writer.write(csvHeader);
            for (String[] record : records) {
                writer.write(String.join(",", record) + System.lineSeparator());
            }
        } catch (IOException e) {
            throw new InternalServerError("Failed to write to CSV file: " + e.getMessage());
        }
    }

    public void appendToCsvFile(String[] record) {
        try (FileWriter writer = new FileWriter(csvFilePath, true)) {
            writer.write(String.join(",", record) + System.lineSeparator());
        } catch (IOException e) {
            throw new InternalServerError("Failed to write to CSV file: " + e.getMessage());
        }
    }

    public String getCsvFilePath() {
        return csvFilePath;
    }
}
