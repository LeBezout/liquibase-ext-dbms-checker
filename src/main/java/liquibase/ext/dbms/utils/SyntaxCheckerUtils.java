package liquibase.ext.dbms.utils;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.database.core.H2Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.ClearDatabaseChangeLogTableStatement;
import liquibase.statement.core.CreateDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.CreateDatabaseChangeLogTableStatement;
import liquibase.statement.core.InitializeDatabaseChangeLogLockTableStatement;
import liquibase.statement.core.LockDatabaseChangeLogStatement;
import liquibase.statement.core.MarkChangeSetRanStatement;
import liquibase.statement.core.RawSqlStatement;
import liquibase.statement.core.SelectFromDatabaseChangeLogLockStatement;
import liquibase.statement.core.SelectFromDatabaseChangeLogStatement;

public final class SyntaxCheckerUtils {
    private SyntaxCheckerUtils() {}

    /**
     * Only supports valid test databases
     * @param database database
     * @return true if supported database
     */
    public static boolean supportsDatabase(Database database) {
        if ((database instanceof H2Database) || (database instanceof HsqlDatabase) || (database instanceof DerbyDatabase)) {
            return database.toString().contains("jdbc:");
        }
        return false;
    }

    /**
     * Only supports valid user statements.
     * @param sqlStatement statement
     * @return true if supported statement
     */
    public static boolean supportsSqlStatement(SqlStatement sqlStatement) {
        // Ignore Raw SQL
        if (sqlStatement instanceof RawSqlStatement) {
            return false;
        }
        // Ignore Liquibase internal
        if (sqlStatement instanceof CreateDatabaseChangeLogTableStatement
            || sqlStatement instanceof CreateDatabaseChangeLogLockTableStatement
            || sqlStatement instanceof ClearDatabaseChangeLogTableStatement
            || sqlStatement instanceof InitializeDatabaseChangeLogLockTableStatement
            || sqlStatement instanceof SelectFromDatabaseChangeLogStatement
            || sqlStatement instanceof SelectFromDatabaseChangeLogLockStatement
            || sqlStatement instanceof LockDatabaseChangeLogStatement
            || sqlStatement instanceof MarkChangeSetRanStatement
        ) {
            return false;
        }
        return true;
    }
}
