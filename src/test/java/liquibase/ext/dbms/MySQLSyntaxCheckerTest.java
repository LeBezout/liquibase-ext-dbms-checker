package liquibase.ext.dbms;

import liquibase.Liquibase;
import liquibase.changelog.ChangeSet;
import liquibase.exception.LiquibaseException;
import liquibase.exception.ValidationFailedException;
import liquibase.ext.dbms.utils.LiquibaseUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.StringWriter;
import java.util.List;

@TestMethodOrder(MethodOrderer.MethodName.class)
class MySQLSyntaxCheckerTest {
    private static LiquibaseUtils liquibaseUtils;

    @BeforeAll
    static void configure() throws Exception {
        liquibaseUtils = LiquibaseUtils.ofFile("/liquibase-h2-mysql.properties");
    }

    @AfterEach
    void afterTest() throws Exception {
        liquibaseUtils.disableMySQLSyntaxChecker();
        liquibaseUtils.closeConnection();
    }

    @Test
    void test_01_extension_disable() throws Exception {
        try (Liquibase liquibase = liquibaseUtils.buildLiquibase()) {
            liquibase.update("");
            liquibase.getLog().info("update successful");
            List<ChangeSet> changes = liquibase.listUnrunChangeSets(null, null);
            Assertions.assertTrue(changes.isEmpty());
        }
    }

    @Test
    void test_02_extension_enable() throws Exception {
        liquibaseUtils.enableMySQLSyntaxChecker();
        try (Liquibase liquibase = liquibaseUtils.buildLiquibase()) {
            ValidationFailedException ex = Assertions.assertThrows(ValidationFailedException.class, () -> liquibase.update(""));
            liquibase.getLog().info(ex.getMessage());
        }
    }

    @Test
    void test_03_rollback() throws Exception {
        try (Liquibase liquibase = liquibaseUtils.buildLiquibase()) {
            // Update without checker
            liquibase.update("");
            liquibase.getLog().info("############ -------------------------------------------- FIN UPDATE");
            // Enable checker and then rollback
            liquibaseUtils.enableMySQLSyntaxChecker();
            StringWriter output = new StringWriter();
            LiquibaseException ex = Assertions.assertThrows(LiquibaseException.class, () -> liquibase.rollback("V0", "", output));
            liquibase.getLog().info("############ -------------------------------------------- FIN ROLLBACK");
            liquibase.getLog().info(ex.getMessage());
            // liquibase.getLog().info(output.toString());
        }
    }
}
