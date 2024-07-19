package codesquad.utils;

import java.util.List;

public interface CsvFileHandlerSpec {
    List<String[]> readCsvFile();

    void appendToCsvFile(String[] values);
}
