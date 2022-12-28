package liquibase.ext.dbms.utils;

import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import org.junit.jupiter.api.Assertions;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class LiquibaseUtils {
    private final Properties properties;
    private Connection connection;

    private LiquibaseUtils(Properties props) {
        properties = props;
    }

    public static LiquibaseUtils ofFile(String classpathFile) throws Exception {
        SLF4JBridgeHandler.install();

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
        SqlGeneratorFactory.reset();
        openConnection();
        Liquibase liquibase = new Liquibase(properties.getProperty("changeLogFile"), new ClassLoaderResourceAccessor(), new JdbcConnection(connection));
        liquibase.getLog().info("Liquibase helper OK");
        liquibase.getLog().info("OracleSyntaxChecker enable: " + System.getProperty("liquibase.ext.dbms.OracleSyntaxChecker.enable"));
        liquibase.getLog().info("MySQLSyntaxChecker  enable: " + System.getProperty("liquibase.ext.dbms.MySQLSyntaxChecker.enable"));
        return liquibase;
    }

    public void openConnection() throws Exception {
        Class.forName(properties.getProperty("driver")).getDeclaredConstructor().newInstance();
        connection = DriverManager.getConnection(properties.getProperty("url"), properties.getProperty("username"), properties.getProperty("password"));
    }

    public void closeConnection() throws Exception {
        if (connection != null) {
            connection.close();
        }
    }

    public void enableMySQLSyntaxChecker() {
        System.setProperty("liquibase.ext.dbms.MySQLSyntaxChecker.enable", "true");
    }
    public void disableMySQLSyntaxChecker() {
        System.clearProperty("liquibase.ext.dbms.MySQLSyntaxChecker.enable");
    }
    public void enableOracleSyntaxChecker() {
        System.setProperty("liquibase.ext.dbms.OracleSyntaxChecker.enable", "true");
    }
    public void disableOracleSyntaxChecker() {
        System.clearProperty("liquibase.ext.dbms.OracleSyntaxChecker.enable");
    }
}
