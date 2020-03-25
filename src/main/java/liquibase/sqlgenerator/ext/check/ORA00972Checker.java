package liquibase.sqlgenerator.ext.check;

/**
 * Controle que les noms d'objets Oracle comportent moins de trente caractères.
 * Message : ORA-00972: identifier is too long
 * Cause: An identifier with more than 30 characters was specified.
 * Action: Specify at most 30 characters.
 */
public class ORA00972Checker implements SQLSyntaxChecker {
    private final String identifier;
    private ORA00972Checker(final String identifier) {
        this.identifier = identifier;
    }

    /**
     * Initialise un nouvel objet SQLSyntaxChecker pour valider la règle ORA-00972.
     * @param identifier l'identifiant à valider
     * @return instance du checker.
     */
    public static ORA00972Checker of(final String identifier) {
        return new ORA00972Checker(identifier);
    }

    @Override
    public String getMessage() {
        return "ORA-00972: identifier is too long";
    }

    @Override
    public String getCause() {
        return String.format("'%s' is more than 30 characters", identifier);
    }

    @Override
    public boolean check() {
        return identifier.length() <= 30;
    }
}
