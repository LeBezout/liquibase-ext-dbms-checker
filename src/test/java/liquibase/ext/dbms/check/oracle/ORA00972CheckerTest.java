package liquibase.ext.dbms.check.oracle;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ORA00972CheckerTest {

    @Test
    void check_should_return_true_if_identifier_length_is_less_than_30_characters() {
        Assertions.assertTrue(ORA00972Checker.of("ident").check());
    }

    @Test
    void check_should_return_true_if_identifier_length_is_equal_30_characters() {
        Assertions.assertTrue(ORA00972Checker.of("Loremipsumdolorsitametconsect").check());
    }

    @Test
    void check_should_return_false_if_identifier_length_is_greater_than_30_characters() {
        Assertions.assertFalse(ORA00972Checker.of("Loremipsumdolorsitametconsecteturadipiscingelit").check());
    }
}
