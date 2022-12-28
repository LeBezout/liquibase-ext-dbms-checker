package liquibase.ext.dbms.check.mysql;

import liquibase.ext.dbms.check.SQLSyntaxChecker;
import liquibase.statement.core.RenameColumnStatement;
import liquibase.statement.core.SetColumnRemarksStatement;

/**
 * Controle pour éviter l'erreur "columnDataType is required for setColumnRemarks/renameColumn on mysql" au moment du déploiement.
 * @see <a href="https://docs.liquibase.com/change-types/set-column-remarks.html">setColumnRemarks</a>
 * @see <a href="https://docs.liquibase.com/change-types/rename-column.html">renameColumn</a>
 */
public class MySQLRequiredColumnDataTypeChecker implements SQLSyntaxChecker {
    private final MySQLTargetedSqlStatement statement;

    private MySQLRequiredColumnDataTypeChecker(MySQLTargetedSqlStatement statement) {
        this.statement = statement;
    }

    /**
     * Initialise un nouvel objet SQLSyntaxChecker pour valider la règle.
     * @param statement SetColumnRemarksStatement à valider
     * @return instance du checker.
     */
    public static MySQLRequiredColumnDataTypeChecker of(SetColumnRemarksStatement statement) {
        return new MySQLRequiredColumnDataTypeChecker(new MySQLTargetedSqlStatement("setColumnRemarks", statement.getTableName(), statement.getColumnName(), statement.getColumnDataType(), statement.getRemarks()));
    }
    /**
     * Initialise un nouvel objet SQLSyntaxChecker pour valider la règle.
     * @param statement RenameColumnStatement à valider
     * @return instance du checker.
     */
    public static MySQLRequiredColumnDataTypeChecker of(RenameColumnStatement statement) {
        return new MySQLRequiredColumnDataTypeChecker(new MySQLTargetedSqlStatement("renameColumn", statement.getTableName(), statement.getOldColumnName(), statement.getColumnDataType(), statement.getNewColumnName()));
    }

    @Override
    public String getMessage() {
        return "ERROR : 'columnDataType' attribute is required for '" + statement.name + "' change type on mysql";
    }

    @Override
    public String getCause() {
        return statement.toString();
    }

    @Override
    public boolean check() {
        if (statement.isSupported()) {
            return statement.hasColumnDataType();
        }
        return true; // invalid statement not covered by this rule
    }

    private static class MySQLTargetedSqlStatement {
        private String name;
        private String tableName;
        private String columnName;
        private String columnDataType;
        private String specificAttribut;

        private MySQLTargetedSqlStatement(String name, String tableName, String columnName, String columnDataType, String specificAttribut) {
            this.name = name;
            this.tableName = tableName;
            this.columnName = columnName;
            this.columnDataType = columnDataType;
            this.specificAttribut = specificAttribut;
        }
        boolean isSupported() {
            return tableName != null && columnName != null;
        }
        boolean hasColumnDataType() {
            return columnDataType != null && !columnDataType.trim().isEmpty();
        }
        @Override
        public String toString() {
            return String.format("%s [Table=%s, Column=%s, %s]", name, tableName, columnName, specificAttribut);
        }
    }
}
