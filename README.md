# liquibase-ext-dbms-checker

:pushpin: Ajout d'éléments de validation supplémentaires des fichiers changelogs pour certains DBMS cibles.

## Principe

Le but de ce composant est de rajouter des contrôles de validité des fichiers changelogs.

Généralement (et c'est une bonne pratique) on valide les fichiers changelogs Liquibase avec une base de test : H2, HSQLDB ou Derby mais malgré les modes de compatibilité vers les systèmes cibles, tels qu'Oracle ou MySQL il reste encore des règles internes aux systèmes cibles qui ne sont pas détectées.

Le but de ce composant est de pouvoir en détecter le plus possible, le plus tôt possible (avant le 1er déploiement).

:information_source: **Le cas d'usage typique est via l'intégration continue pour détecter les problèmes au plus tôt (donc des gains de temps) sans attendre un déploiement sur un environnement cible.**

## Rappel : les modes de compatibilités

* pour H2 : `MODE=Oracle`, `MODE=MySQL`, `MODE=PostgreSQL`, `MODE=MSSQLServer`, ...
* Pour HSQLDB : `sql.syntax_ora=true`, `sql.syntax_mys=true`, `sql.syntax_pgs=true`, `sql.syntax_mss=true`, ...
* Pour Derby : aucun

## Principes d'extension de Liquibase

* [Liquibase Extensions extend Liquibase database support and capabilities](https://www.liquibase.org/extensions)
* OLD : [Liquibase Extensions - Overview](https://liquibase.jira.com/wiki/spaces/CONTRIB/pages/2424879/Overview)

### Généralités

Ces principes d'extensions de l'API _Liquibase_ sont décrits ici : <https://liquibase.jira.com/wiki/spaces/CONTRIB/pages/1998869/Extension+Points> :

> If you want to extend Liquibase to add new change types, alter the behavior of existing change types, or add support for new databases, these are the interfaces you need to know about.

### Choix d'un mode d'implémentation

Différents points d'accroches y sont décrits :

* Change
  * _liquibase.change.Change implementations are the objects created from database change log files_
  * **Non adapté pour rajouter des contrôles**
* ChangeLogParser
  * _liquibase.parser.ChangeLogParser implementations allow you to read database changelog files written in different formats._
  * **Non adapté pour rajouter des contrôles**
* ChangeLogSerializer
  * _liquibase.serializer.ChangeLogSerializer implementation are used by the generateChangeLog command and other functions that need to convert a DatabaseChangeLog object into a state that can be saved to disk_
  * **Non adapté au besoin**
* Database
  * _liquibase.database.Database implementations are the facade Liquibase uses to access the database_
  * **Non adapté au besoin**
* Precondition
  * _liquibase.precondition.Precondition implementations allow you to run validation and checks against your database before running a changelog as a whole_
  * **Non adapté au besoin, le but n'est pas de rajouter de pré-conditions dans un changelog mais de rajouter des contrôles au _runtime_**
* SqlGenerator
  * _liquibase.sqlgenerator.SqlGenerator implementations convert database-independent SqlStatement classes into database-specific SQL._
  * Propose 2 méthodes intéressantes :
    * `public ValidationErrors validate(StatementType statement, Database database, SqlGeneratorChain sqlGeneratorChain);`
    * `public Warnings warn(SqlStatement sqlStatement, Database database, SqlGeneratorChain sqlGeneratorChain);`
  * **C'est la solution retenue**

### Limites de l'extension via SqlGenerator

:link: <https://liquibase.jira.com/wiki/spaces/CONTRIB/pages/1998878/SqlGenerator>

Ce mode d'extension nous limite à utiliser des objets :

* `liquibase.statement.SqlStatement`
* `liquibase.database.Database`

Ce qui implique les contraintes suivantes :

* Impossible d'obtenir des informations sur le changeset contenant le changement.
* Impossible de récupérer les éventuels _changelog parameters_.
* Dans le cas de H2 impossible de récupérer via l'URL de connexion le mode de compatibilité s'il est précisé.
* Il n'y a aucune notion d'héritage entre les différents _SlqStatement_ ils héritent tous de `AbstractSqlStatement` (qui ne nous apporte pas grand chose). Il faut donc abuser de l'opérateur `instanceof` du style `if (sqlStatement instanceof CreateTableStatement)`.

## Contrôles pris en charge par DBMS cible

### Cible Oracle

#### ORA-00972: identifier is too long (since 1.0.0)

But de la règle :

> Veiller à ce que les noms d'objets Oracle comportent au maximum 30 caractères.

Cause: An identifier with more than 30 characters was specified.

Action: Specify at most 30 characters.

Références :

* <https://kb.tableau.com/articles/issue/error-ora-00972?lang=fr-fr>
* <http://www.dba-oracle.com/sf_ora_00972_identifier_is_too_long.htm>


#### ORA-00910: specified length too long for its datatype (since 1.0.0)

But de la règle :

> Veiller à ce que la taille des types CHAR/VARCHAR ne dépasse pas les limites.

Cause: for datatypes CHAR and RAW, the length specified was > 2000; otherwise, the length specified was > 4000.

Action: use a shorter length or switch to a datatype permitting a longer length such as a VARCHAR2, LONG CHAR, or LONG RAW

### Cible MySQL

#### Identifier name is too long (since 1.0.0)

But de la règle :

> Veiller à ce que les noms d'objets MySQL comportent au maximum 64 caractères.

Références :

* <https://dev.mysql.com/doc/refman/8.0/en/identifier-length.html>

#### columnDataType is required for setColumnRemarks/renameColumn on mysql (since 1.1.0)

But de la règle :

> Veiller à ce que l'attribut `columnDataType` (à priori inutile) soit spécifié sur le _change type_ `setColumnRemarks` dans le cas d'une cible MySQL.

Références :

* <https://docs.liquibase.com/change-types/set-column-remarks.html>
* <https://docs.liquibase.com/change-types/rename-column.html>

## Exemple d'utilisation

### Etape 1 : Ajouter l'extension au classpath

* Pour Maven ajouter la dépendance `com.github.lebezout:liquibase-ext-dbms-checker:1.1.0`
* Pour la ligne de commande ajouter `liquibase-ext-dbms-checker-1.1.0.jar`

### Etape 2 : Activer les contrôles de syntaxe

Pour une cible Oracle :

* Pour un test JUnit : `System.setProperty("liquibase.ext.dbms.OracleSyntaxChecker.enable", "true");`
* Pour Maven via les properties : `<liquibase.ext.dbms.OracleSyntaxChecker.enable>true</liquibase.ext.dbms.OracleSyntaxChecker.enable>`
* Pour la ligne de commande : `-Dliquibase.ext.dbms.OracleSyntaxChecker.enable=true`

Pour une cible MySQL :

* Pour un test JUnit : `System.setProperty("liquibase.ext.dbms.MySQLSyntaxChecker.enable", "true");`
* Pour Maven via les properties : `<liquibase.ext.Mdbms.ySQLSyntaxChecker.enable>true</liquibase.ext.dbms.MySQLSyntaxChecker.enable>`
* Pour la ligne de commande : `-Dliquibase.ext.dbms.MySQLSyntaxChecker.enable=true`

### Etape 3 : Exécuter Liquibase

* Pour un test JUnit : `liquibase.validate()` ou `liquibase.update(new Contexts());`
* Pour Maven : `mvn resources:resources liquibase:validate` ou `mvn resources:resources liquibase:update`
* Pour la ligne de commande : `JAVA_OPTS="-Dliquibase.ext.dbms.MySQLSyntaxChecker.enable=true" liquibase update`

Exemple de résultat :

```text
liquibase.exception.ValidationFailedException: Validation Failed:
     11 changes have validation failures
          ERROR 1059 (42000): Identifier name is too long : 'mysql_identifier_table1_Loremipsumdolorsitametconsecteturadipiscingelit' is more than 64 characters, db-changelog-mysql.xml::test-createTable::demo-liquibase
          ERROR 1059 (42000): Identifier name is too long : 'mysql_identifier_field1_Loremipsumdolorsitametconsecteturadipiscingelit' is more than 64 characters, db-changelog-mysql.xml::test-TABLE2::demo-liquibase
          ERROR 1059 (42000): Identifier name is too long : 'mysql_identifier_constraint1_Loremipsumdolorsitametconsecteturadipiscingelit' is more than 64 characters, db-changelog-mysql.xml::test-TABLE3::demo-liquibase
          ERROR 1059 (42000): Identifier name is too long : 'mysql_identifier_field2_Loremipsumdolorsitametconsecteturadipiscingelit' is more than 64 characters, db-changelog-mysql.xml::test-addColumn::demo-liquibase
          ERROR 1059 (42000): Identifier name is too long : 'mysql_identifier_constraint2_Loremipsumdolorsitametconsecteturadipiscingelit' is more than 64 characters, db-changelog-mysql.xml::test-addUniqueConstraint::demo-liquibase
          ERROR 1059 (42000): Identifier name is too long : 'mysql_identifier_vue1_Loremipsumdolorsitametconsecteturadipiscingelit' is more than 64 characters, db-changelog-mysql.xml::test-createView::demo-liquibase
          ERROR 1059 (42000): Identifier name is too long : 'mysql_identifier_index_Loremipsumdolorsitametconsecteturadipiscingelit' is more than 64 characters, db-changelog-mysql.xml::test-createIndex::demo-liquibase
          ERROR 1059 (42000): Identifier name is too long : 'mysql_identifier_pk_Loremipsumdolorsitametconsecteturadipiscingelit' is more than 64 characters, db-changelog-mysql.xml::test-addPrimaryKey::demo-liquibase
          ERROR 1059 (42000): Identifier name is too long : 'mysql_identifier_fk_Loremipsumdolorsitametconsecteturadipiscingelit' is more than 64 characters, db-changelog-mysql.xml::test-addForeignKeyConstraint::demo-liquibase
          ERROR 1059 (42000): Identifier name is too long : 'mysql_identifier_field3_Loremipsumdolorsitametconsecteturadipiscingelit' is more than 64 characters, db-changelog-mysql.xml::test-renameColumn::demo-liquibase
          ERROR 1059 (42000): Identifier name is too long : 'mysql_identifier_table2_Loremipsumdolorsitametconsecteturadipiscingelit' is more than 64 characters, db-changelog-mysql.xml::test-renameTable::demo-liquibase


	at liquibase.changelog.DatabaseChangeLog.validate(DatabaseChangeLog.java:276)
```
