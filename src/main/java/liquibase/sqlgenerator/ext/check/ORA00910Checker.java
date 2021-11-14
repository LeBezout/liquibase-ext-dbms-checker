package liquibase.sqlgenerator.ext.check;

import liquibase.datatype.LiquibaseDataType;

/**
 * Contrôle que les types d'objets caractères ne dépassent la taille limite.
 * Message : ORA-00910: specified length too long for its datatype
 * Cause: Occurs when a column data type specified length is more than what Oracle supports while creating table, altering table, or declaring a variable in Oracle.
 * Action: Decrease length.
 */
public class ORA00910Checker implements SQLSyntaxChecker {
    private static final int CHAR_MAX_LENGTH = 2000;
    private static final int VARCHAR_MAX_LENGTH = 4000;
    private final String tableName;
    private final String columnName;
    private final LiquibaseDataType columnType;
    private ORA00910Checker(final String tableName, final String columnName, final LiquibaseDataType columnType) {
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnType = columnType;
    }

    /**
     * Initialise un nouvel objet SQLSyntaxChecker pour valider la règle ORA-009710.
     * @param tableName le nom de la table concernée
     * @param columnName le nom de la colonne concernée
     * @param columnType le type de la colonne concernée
     * @return instance du checker.
     */
    public static ORA00910Checker of(final String tableName, final String columnName, final LiquibaseDataType columnType) {
        return new ORA00910Checker(tableName, columnName, columnType);
    }

    @Override
    public String getMessage() {
        return "ORA-00910: specified length too long for its datatype";
    }

    @Override
    public String getCause() {
        return String.format("'%s' of type %s is more than %d length (table: %s)", columnName, columnType.getName(), getTypeMaxLength(), tableName);
    }

    private int getTypeMaxLength() {
        switch (columnType.getName()) {
            case "char" :
            case "nchar" :
                return CHAR_MAX_LENGTH;
            case "varchar" :
            case "nvarchar" :
                return VARCHAR_MAX_LENGTH;
            default : return 0;
        }
    }

    @Override
    public boolean check() {
        switch (columnType.getName()) {
            case "char" :
            case "nchar" :
                return checkLength(columnType, CHAR_MAX_LENGTH);
            case "varchar" :
            case "nvarchar" :
                return checkLength(columnType, VARCHAR_MAX_LENGTH);
            default : return true;
        }
    }

    private static boolean checkLength(final LiquibaseDataType columnType, int maxLength) {
        Object[] params = columnType.getParameters();
        if (params != null && params.length > 0) {
            int length = Integer.parseInt(String.valueOf(params[0]));
            return length <= maxLength;
        }
        return true;
    }
}
