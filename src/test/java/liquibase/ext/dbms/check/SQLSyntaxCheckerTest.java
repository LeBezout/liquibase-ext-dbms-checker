package liquibase.ext.dbms.check;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SQLSyntaxCheckerTest {

    private static class DemoSQLSyntaxChecker implements SQLSyntaxChecker {
        private final boolean checkValue;
        private DemoSQLSyntaxChecker(boolean value) {
            checkValue = value;
        }
        @Override
        public String getMessage() {
            return "message";
        }
        @Override
        public String getCause() {
            return "cause";
        }
        @Override
        public boolean check() {
            return checkValue;
        }
    }

    @Test
    void validate_should_return_optional_empty() {
        assertFalse(new DemoSQLSyntaxChecker(true).validate().isPresent());
    }

    @Test
    void validate_should_return_optional_of_string() {
        assertEquals("message : cause", new DemoSQLSyntaxChecker(false).validate().get());
    }
}
