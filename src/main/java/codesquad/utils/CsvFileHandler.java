package codesquad.utils;

import codesquad.exception.InternalServerError;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
        initializeCsvFileIfNotExists();
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
