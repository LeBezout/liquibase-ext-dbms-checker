package liquibase.sqlgenerator.ext.check;

import java.util.Optional;

/**
 * Décrit le comportant de vérification de syntaxe SQL.
 */
public interface SQLSyntaxChecker {
    /**
     * Le message générique à afficher dans le cas où la validation échoue.
     * @return message
     */
    String getMessage();
    /**
     * Le message complémentaire à afficher dans le cas où la validation échoue.
     * @return message
     */
    String getCause();

    /**
     * Effectue la validation
     * @return true si OK false sinon
     */
    boolean check();
    /**
     * Effectue la validation et renvoie le message d'erreur dans le cas où la validation échoue.
     * @return Optional message d'erreur complet
     */
    default Optional<String> validate() {
        if (check()) return Optional.empty();
        return Optional.of(getMessage() + " : " + getCause());
    }
}
