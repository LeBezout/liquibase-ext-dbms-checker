package liquibase.ext.dbms;

import liquibase.database.Database;
import liquibase.exception.Warnings;
import liquibase.ext.dbms.utils.SyntaxCheckerUtils;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.SqlGenerator;
import liquibase.sqlgenerator.SqlGeneratorChain;
import liquibase.statement.SqlStatement;

public abstract class AbstractDbmsSyntaxChecker implements SqlGenerator {

    protected abstract String activationSystemPropertyKey();

    protected boolean isEnabled() {
        return Boolean.getBoolean(activationSystemPropertyKey());
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE + 5;
    }

    @Override
    public boolean supports(SqlStatement sqlStatement, Database database) {
        return SyntaxCheckerUtils.supportsDatabase(database)
            && SyntaxCheckerUtils.supportsSqlStatement(sqlStatement);
    }

    @Override
    public boolean generateStatementsIsVolatile(Database database) {
        return false;
    }

    @Override
    public boolean generateRollbackStatementsIsVolatile(Database database) {
        return false;
    }

    @Override
    public Warnings warn(SqlStatement sqlStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return sqlGeneratorChain.warn(sqlStatement, database);
    }

    @Override
    public Sql[] generateSql(SqlStatement sqlStatement, Database database, SqlGeneratorChain sqlGeneratorChain) {
        return sqlGeneratorChain.generateSql(sqlStatement, database);
    }
}
