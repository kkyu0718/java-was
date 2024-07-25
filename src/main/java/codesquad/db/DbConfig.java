package codesquad.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DbConfig {
    private static final Logger logger = LoggerFactory.getLogger(DbConfig.class);
    private String jdbcUrl;
    private String user;
    private String password;

    public DbConfig(String jdbcUrl, String user, String password) {
        this.jdbcUrl = jdbcUrl;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, user, password);
    }

    public void initializeDatabase() {
        logger.debug("initializing database...");
        try {
            // JDBC 드라이버 로드
            Class.forName("org.h2.Driver");

            // 데이터베이스 연결
            try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password);
                 Statement stmt = conn.createStatement()) {

                // schema.sql 파일 읽기
                String schema = readSchemaFile();

                // SQL 실행
                for (String sql : schema.split(";")) {
                    if (!sql.trim().isEmpty()) {
                        stmt.execute(sql);
                    }
                }

                System.out.println("Database initialized successfully.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readSchemaFile() {
        try (InputStream inputStream = getClass().getResourceAsStream("/schema.sql");
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to read schema.sql file", e);
        }
    }
}