package liquibase.ext.dbms.check.mysql;

import liquibase.statement.core.SetColumnRemarksStatement;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MySQLRequiredColumnDataTypeCheckerTest {
    @Test
    void cause_should_contains_statement_data() {
        SetColumnRemarksStatement statement = new SetColumnRemarksStatement("", "", "table1", "column1", "comment", "VARCHAR2");
        MySQLRequiredColumnDataTypeChecker checker = MySQLRequiredColumnDataTypeChecker.of(statement);
        String cause = checker.getCause();
        System.out.println(cause);
        Assertions.assertAll(
            () -> Assertions.assertTrue(cause.contains("table1")),
            () -> Assertions.assertTrue(cause.contains("column1")),
            () -> Assertions.assertTrue(cause.contains("comment"))
        );
    }
    @Test
    void check_should_not_fail_if_sql_statement_is_valid() {
        SetColumnRemarksStatement statement = new SetColumnRemarksStatement("", "", "table1", "column1", "comment", "VARCHAR2");
        MySQLRequiredColumnDataTypeChecker checker = MySQLRequiredColumnDataTypeChecker.of(statement);
        Assertions.assertTrue(checker.check());
    }
    @Test
    void check_should_fail_if_column_data_type_is_null() {
        SetColumnRemarksStatement statement = new SetColumnRemarksStatement("", "", "table1", "column1", "comment");
        MySQLRequiredColumnDataTypeChecker checker = MySQLRequiredColumnDataTypeChecker.of(statement);
        Assertions.assertFalse(checker.check());
    }
    @Test
    void check_should_fail_if_column_data_type_is_empty() {
        SetColumnRemarksStatement statement = new SetColumnRemarksStatement("", "", "table1", "column1", "comment", "");
        MySQLRequiredColumnDataTypeChecker checker = MySQLRequiredColumnDataTypeChecker.of(statement);
        Assertions.assertFalse(checker.check());
    }
    @Test
    void check_should_fail_if_column_data_type_is_blank() {
        SetColumnRemarksStatement statement = new SetColumnRemarksStatement("", "", "table1", "column1", "comment", "   ");
        MySQLRequiredColumnDataTypeChecker checker = MySQLRequiredColumnDataTypeChecker.of(statement);
        Assertions.assertFalse(checker.check());
    }
}
