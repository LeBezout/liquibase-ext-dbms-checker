package liquibase.sqlgenerator.ext;

import liquibase.Liquibase;
import liquibase.exception.ValidationFailedException;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.sqlgenerator.ext.utils.LiquibaseUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OracleSyntaxCheckerTest {
    private Liquibase liquibase;
    private static LiquibaseUtils liquibaseUtils;

    @BeforeAll
    static void configure() throws Exception {
        liquibaseUtils = LiquibaseUtils.ofFile("/liquibase-h2-oracle.properties");
    }

    @BeforeEach
    void beforeTest_OpenConnection() throws Exception {
        liquibase = liquibaseUtils.buildLiquibase();
    }

    @AfterEach
    void afterTest_CloseConnection() throws Exception {
        liquibaseUtils.closeConnection();
        System.clearProperty("liquibase.ext.OracleSyntaxChecker.enable");
        SqlGeneratorFactory.getInstance().unregister(OracleSyntaxChecker.class);
    }

    @Test
    void test_extension_enable() {
        System.setProperty("liquibase.ext.OracleSyntaxChecker.enable", "true");
        ValidationFailedException ex = Assertions.assertThrows(ValidationFailedException.class, () -> liquibase.update(""));
        liquibase.getLog().info(ex.getMessage());
    }

    @Test
    void test_extension_disable() throws Exception {
        liquibase.update("");
        liquibase.getLog().info("update successful");
    }
}