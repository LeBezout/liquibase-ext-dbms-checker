package liquibase.sqlgenerator.ext.check;

/**
 * Controle que les noms d'objets MySQL comportent moins de 64 caractères.
 * Most identifiers have a maximum length of 64 characters. The maximum length for aliases is 256 characters.
 * @øee {@link <a href="https://dev.mysql.com/doc/refman/8.0/en/identifier-length.html">Identifier Length</a>}
 */
public class MySQLIdentifierLengthLimitChecker implements SQLSyntaxChecker {
    private final String identifier;
    private MySQLIdentifierLengthLimitChecker(final String identifier) {
        this.identifier = identifier;
    }

    /**
     * Initialise un nouvel objet SQLSyntaxChecker pour valider la règle.
     * @param identifier l'identifiant à valider
     * @return instance du checker.
     */
    public static MySQLIdentifierLengthLimitChecker of(final String identifier) {
        return new MySQLIdentifierLengthLimitChecker(identifier);
    }

    @Override
    public String getMessage() {
        return "ERROR 1059 (42000): Identifier name is too long";
    }

    @Override
    public String getCause() {
        return String.format("'%s' is more than 64 characters", identifier);
    }

    @Override
    public boolean check() {
        return identifier.length() <= 64;
    }
}
