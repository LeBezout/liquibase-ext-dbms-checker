package liquibase.sqlgenerator.ext.check;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MySQLIdentifierLengthLimitCheckerTest {

    @Test
    void check_should_return_true_if_identifier_length_is_less_than_64_characters() {
        Assertions.assertTrue(MySQLIdentifierLengthLimitChecker.of("ident").check());
    }

    @Test
    void check_should_return_true_if_identifier_length_is_equal_64_characters() {
        Assertions.assertTrue(MySQLIdentifierLengthLimitChecker.of("LoremipsumdolorsitametconsecteturadipiscingelitLoremipsumdolors").check());
    }

    @Test
    void check_should_return_false_if_identifier_length_is_greater_than_64_characters() {
        Assertions.assertFalse(MySQLIdentifierLengthLimitChecker.of("LoremipsumdolorsitametconsecteturadipiscingelitLoremipsumdolorsitametcons").check());
    }
}
