package liquibase.ext.dbms;

import liquibase.Scope;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.ext.dbms.check.mysql.MySQLIdentifierLengthLimitChecker;
import liquibase.ext.dbms.check.mysql.MySQLRequiredColumnDataTypeChecker;
import liquibase.logging.Logger;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.AddColumnStatement;
import liquibase.statement.core.AddForeignKeyConstraintStatement;
import liquibase.statement.core.AddPrimaryKeyStatement;
import liquibase.statement.core.AddUniqueConstraintStatement;
import liquibase.statement.core.CreateIndexStatement;
import liquibase.statement.core.CreateProcedureStatement;
import liquibase.statement.core.CreateSequenceStatement;
import liquibase.statement.core.CreateTableStatement;
import liquibase.statement.core.CreateViewStatement;
import liquibase.statement.core.RenameColumnStatement;
import liquibase.statement.core.RenameSequenceStatement;
import liquibase.statement.core.RenameTableStatement;
import liquibase.statement.core.RenameViewStatement;
import liquibase.statement.core.SetColumnRemarksStatement;

/**
 * Ajoute des controles supplémentaires de validité pour une cible MySQL.
 * @see <a href="https://liquibase.jira.com/wiki/spaces/CONTRIB/pages/1998878/SqlGenerator">SqlGenerator</a>
 */
public class MySQLSyntaxChecker extends AbstractDbmsSyntaxChecker {
    public MySQLSyntaxChecker() {
        Scope.getCurrentScope().getLog(this.getClass()).info("Registering MySQLSyntaxChecker instance, enable : " + isEnabled());
    }

    @Override
    protected String activationSystemPropertyKey() {
        return "liquibase.ext.dbms.MySQLSyntaxChecker.enable";
    }

    @Override
    public int getPriority() {
        return super.getPriority() + 1;
    }

    @Override
    public ValidationErrors validate(SqlStatement sqlStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        ValidationErrors errors = sqlGeneratorChain.validate(sqlStatement, database);
        if (isEnabled()) {
            Logger log = Scope.getCurrentScope().getLog(this.getClass());
            log.info("Validating SqlStatement for MySQL target: " + sqlStatement.getClass().getSimpleName() + " (Database=" + database + ")");
            // validate
            if (sqlStatement instanceof CreateTableStatement) {
                CreateTableStatement statement = (CreateTableStatement) sqlStatement;
                validateIdentifier(errors, statement.getTableName());
                // Columns names
                statement.getColumns().forEach(c -> validateIdentifier(errors, c));
                // PK
                if (statement.getPrimaryKeyConstraint() != null) {
                    validateIdentifier(errors, statement.getPrimaryKeyConstraint().getConstraintName());
                }
                // FK
                if (statement.getForeignKeyConstraints() != null) {
                    statement.getForeignKeyConstraints().forEach(c -> validateIdentifier(errors, c.getForeignKeyName()));
                }
                // NotNull
                if (statement.getNotNullColumns() != null) {
                    statement.getNotNullColumns().values().forEach(c -> validateIdentifier(errors, c.getConstraintName()));
                }
                //TODO statement.getAutoIncrementConstraints()
                //TODO statement.getDefaultValueConstraintNames()
            } else if (sqlStatement instanceof CreateProcedureStatement) {
                CreateProcedureStatement statement = (CreateProcedureStatement) sqlStatement;
                validateIdentifier(errors, statement.getProcedureName());
            } else if (sqlStatement instanceof CreateViewStatement) {
                CreateViewStatement statement = (CreateViewStatement) sqlStatement;
                validateIdentifier(errors, statement.getViewName());
            } else if (sqlStatement instanceof CreateSequenceStatement) {
                // pas de sequence dans MySQL
            } else if (sqlStatement instanceof AddColumnStatement) {
                AddColumnStatement statement = (AddColumnStatement) sqlStatement;
                validateIdentifier(errors, statement.getColumnName());
            } else if (sqlStatement instanceof CreateIndexStatement) {
                CreateIndexStatement statement = (CreateIndexStatement) sqlStatement;
                validateIdentifier(errors, statement.getIndexName());
            } else if (sqlStatement instanceof AddPrimaryKeyStatement) {
                AddPrimaryKeyStatement statement = (AddPrimaryKeyStatement) sqlStatement;
                validateIdentifier(errors, statement.getConstraintName());
            } else if (sqlStatement instanceof AddUniqueConstraintStatement) {
                AddUniqueConstraintStatement statement = (AddUniqueConstraintStatement) sqlStatement;
                validateIdentifier(errors, statement.getConstraintName());
            } else if (sqlStatement instanceof AddForeignKeyConstraintStatement) {
                AddForeignKeyConstraintStatement statement = (AddForeignKeyConstraintStatement) sqlStatement;
                validateIdentifier(errors, statement.getConstraintName());
            } else if (sqlStatement instanceof RenameColumnStatement) {
                RenameColumnStatement statement = (RenameColumnStatement) sqlStatement;
                validateIdentifier(errors, statement.getNewColumnName());
                MySQLRequiredColumnDataTypeChecker.of(statement).validate().ifPresent(errors::addError);
            } else if (sqlStatement instanceof RenameSequenceStatement) {
                // pas de sequence dans MySQL
            } else if (sqlStatement instanceof RenameViewStatement) {
                RenameViewStatement statement = (RenameViewStatement) sqlStatement;
                validateIdentifier(errors, statement.getNewViewName());
            } else if (sqlStatement instanceof RenameTableStatement) {
                RenameTableStatement statement = (RenameTableStatement) sqlStatement;
                validateIdentifier(errors, statement.getNewTableName());
            } else if (sqlStatement instanceof SetColumnRemarksStatement) {
                SetColumnRemarksStatement statement = (SetColumnRemarksStatement) sqlStatement;
                MySQLRequiredColumnDataTypeChecker.of(statement).validate().ifPresent(errors::addError);
            } else {
                log.fine("Ignoring statement " + sqlStatement);
            }
        }
        return errors;
    }

    private static void validateIdentifier(ValidationErrors errors, String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return;
        }
        // Identifier Length
        MySQLIdentifierLengthLimitChecker.of(identifier).validate().ifPresent(errors::addError);
        // Other identifier checkers here
    }
}
