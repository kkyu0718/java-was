package codesquad;

import codesquad.db.DbConfig;
import org.h2.tools.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.SQLException;


public class Main {
    public static Logger logger = LoggerFactory.getLogger(Main.class);
    private static final int PORT_NUMBER = 8080;

    public static void main(String[] args) throws IOException, SQLException {
        Server.createWebServer("-web", "-webAllowOthers", "-webPort", "9092").start();
        DbConfig dbConfig = new DbConfig(
                "jdbc:h2:~/h2db/test;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );
        dbConfig.initializeDatabase();

        WasServer wasServer = new WasServer(PORT_NUMBER, dbConfig);
        wasServer.run();
    }
}
