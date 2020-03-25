# liquibase-ext-dbms-checker

:pushpin: Ajout d'éléments de validation supplémentaires des fichiers changelogs pour certains DBMS cibles.

## Principe

Le but de ce composant est de rajouter des contrôles de validité des fichiers changelogs.

Généralement (et c'est une bonne pratique) on valide les fichiers changelogs Liquibase avec une base de test : H2, HSQLDB ou Derby mais malgré les modes de compatibilité vers les systèmes cibles, tels que Oracle ou MySQL il reste encore des règles internes aux systèmes cibles qui ne sont pas détectées.

Le but de ce composant est de pouvoir en détecter le plus possibles.

:information_source: **Le cas d'usage typique est via l'intégration continue pour détecter les problèmes au plus tôt sans attendre un déploiement sur un environnement cible.**

## Rappel : les modes de compatibilités

* pour H2 : `MODE=Oracle`, `MODE=MySQL`, `MODE=PostgreSQL`, `MODE=MSSQLServer`, ...
* Pour HSQLDB : `sql.syntax_ora=true`, `sql.syntax_mys=true`, `sql.syntax_pgs=true`, `sql.syntax_mss=true`, ...
* Pour Derby : aucun

## Principes d'extension de Liquibase

### Généralités

Ces principes d'extentions de l'API _Liquibase_ sont décrits ici : <https://liquibase.jira.com/wiki/spaces/CONTRIB/pages/1998869/Extension+Points> :

> If you want to extend Liquibase to add new change types, alter the behavior of existing change types, or add support for new databases, these are the interfaces you need to know about.

### Choix d'un mode d'implémentation

Différents points d'accroches y sont décrits :

* Change
  * _liquibase.change.Change implementations are the objects created from database change log files_
  * **Non adapté pour rajouter des contrôles**
* ChangeLogParser
  * _iquibase.parser.ChangeLogParser implementations allow you to read database changelog files written in different formats._
  * **Non adapté pour rajouter des contrôles**
* ChangeLogSerializer
  * _liquibase.serializer.ChangeLogSerializer implementation are used by the generateChangeLog command and other functions that need to convert a DatabaseChangeLog object into a state that can be saved to disk_
  * **Non adapté au besoin**
* Database
  * _liquibase.database.Database implementations are the facade Liquibase uses to access the database_
  * **Non adapté au besoin**
* Precondition
  *liquibase.precondition.Precondition implementations allow you to run validation and checks against your database before running a changelog as a whole_
  * **Non adapté au besoin, le but n'est pas de rajouter de pré conditions dans un changelog mais de rajouter des contrôles au _runtime_**
* SqlGenerator
  * _liquibase.sqlgenerator.SqlGenerator implementations convert database-independent SqlStatement classes into database-specific SQL._
  * Propose 2 méthodes intéressantes :
    * `public ValidationErrors validate(StatementType statement, Database database, SqlGeneratorChain sqlGeneratorChain);`
    * `public Warnings warn(SqlStatement sqlStatement, Database database, SqlGeneratorChain sqlGeneratorChain);`
  * **Solution retenue**
  
### Limites de l'extension via SqlGenerator

:link: <https://liquibase.jira.com/wiki/spaces/CONTRIB/pages/1998878/SqlGenerator>

Ce mode d'extension nous limite à utiliser des objets :

* `liquibase.statement.SqlStatement`
* `liquibase.database.Database`

Ce qui implique les contraintes suivantes :

* Impossible d'obtenir des informations sur le changeset contenant le changement.
* Impossible de récupérer les éventuels _changelog parameters_.
* Dans le cas de H2 impossible de récupérer via l'URL de connexion le mode de comptibilité s'il est précisé.
* Il n'y a aucune notion d'héritage entre les différents _SlqStatement_ ils héritent tous de `AbstractSqlStatement` (qui ne apporte pas grand chose). Il faut donc abuser de l'opérateur `instanceof`.

## Contrôles pris en charge par DBMS cible

### Cible Oracle

#### ORA-00972: identifier is too long

But de la règle : 

> Veillez à ce que les noms d'objets Oracle comportent au maximum 30 caractères.

Références :

* <https://kb.tableau.com/articles/issue/error-ora-00972?lang=fr-fr>
* <http://www.dba-oracle.com/sf_ora_00972_identifier_is_too_long.htm>

### Cible MySQL

#### Identifier name is too long

But de la règle : 

> Veillez à ce que les noms d'objets MySQL comportent au maximum 64 caractères.

Références :

* <https://dev.mysql.com/doc/refman/8.0/en/identifier-length.html>

## Exemple d'utilisation

TODO
