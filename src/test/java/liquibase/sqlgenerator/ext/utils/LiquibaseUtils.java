package liquibase.sqlgenerator.ext.utils;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class LiquibaseUtils {
    private final Properties properties;
    private Connection connection;

    private LiquibaseUtils(Properties props) {
        properties = props;
    }

    public static LiquibaseUtils ofFile(String classpathFile) throws IOException {
        Properties properties = new Properties();
        properties.load(LiquibaseUtils.class.getResourceAsStream(classpathFile));

        Assertions.assertAll(
            () -> Assertions.assertFalse(properties.getProperty("driver").isEmpty(), "driver key is required"),
            () -> Assertions.assertFalse(properties.getProperty("url").isEmpty(), "url key is required"),
            () -> Assertions.assertFalse(properties.getProperty("changeLogFile").isEmpty(), "changeLogFile key is required")
        );

        return new LiquibaseUtils(properties);
    }

    public Liquibase buildLiquibase() throws Exception {
        // Open Connection
        Class.forName(properties.getProperty("driver")).getDeclaredConstructor().newInstance();
        connection = DriverManager.getConnection(properties.getProperty("url"), properties.getProperty("username"), properties.getProperty("password"));
        // Build Liquibase
        Liquibase liquibase = new Liquibase(properties.getProperty("changeLogFile"), new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
        liquibase.getLog().info("Liquibase helper OK");
        return liquibase;
    }

    public void closeConnection() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }
}
