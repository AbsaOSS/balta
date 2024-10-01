# Balta

Balta makes testing DB entities from Scala easier. It's primarily focused on testing the behavior of [Postgres functions](https://www.postgresql.org/docs/current/xfunc.html).

---

### Build Status

[![Build](https://github.com/AbsaOSS/balta/workflows/Build/badge.svg)](https://github.com/AbsaOSS/balta/actions)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa.db/balta_2.12/badge.svg)](https://search.maven.org/search?q=g:za.co.absa.db.balta)

---

Balta is a Scala library to help creating database tests, particularly testing Database functions. It is based on the 
popular [ScalaTest](http://www.scalatest.org/) library and uses [PostgreSQL](https://www.postgresql.org/) as the database engine.

It's a natural complement to the use of [Fa-Db library](https://github.com/AbsaOSS/fa-db) in applications.

## Expected test-case structure
1. _Transaction start_*
2. Insert needed data into tables
3. Call the function to be tested
4. Verify the return values of the function via the `verify` function provided
5. Verify the data un the tables
6. _Transaction rollback_*

 * The transaction start and rollback are done automatically before or after the execution respectively of the `test` function provided

Advantages of this approach is that the tests are repeatable, they are isolated from each other and the database is always 
in a known state before and after each test.


## How to write tests

### [`DBTestSuite`](https://github.com/AbsaOSS/balta/blob/master/balta/src/main/scala/za/co/absa/db/balta/DBTestSuite.scala) class

The foundation is the [`DBTestSuite`](https://github.com/AbsaOSS/balta/blob/master/balta/src/main/scala/za/co/absa/db/balta/DBTestSuite.scala) 
class that provides the necessary setup and teardown for the tests to each run in its own transaction. It is an 
enhancement class to standard ScalaTest `AnyFunSuite` class.

Besides that, it provides easy access to tables, queries them and inserts data into them.

And it allows easy access to database functions and executing them.

### [`DBTable`](https://github.com/AbsaOSS/balta/blob/master/balta/src/main/scala/za/co/absa/db/balta/classes/DBTable.scala) class

Class for representing a database table. It provides methods for querying the table and inserting data into it. The class 
instance is spawned by each `DBTestSuite.table` method call. 

### [`DBFunction`](https://github.com/AbsaOSS/balta/blob/master/balta/src/main/scala/za/co/absa/db/balta/classes/DBFunction.scala) class

Class for representing a database function. It provides methods for calling the function and verifying the return values.
The class instance is spawned by each `DBTestSuite.function` method call.

### [`QueryResult`](https://github.com/AbsaOSS/balta/blob/master/balta/src/main/scala/za/co/absa/db/balta/classes/QueryResult.scala) class

This iterator represents the result of a query. It provides methods for iterating over the rows and columns of the result.

### [`QueryResultRow`](https://github.com/AbsaOSS/balta/blob/master/balta/src/main/scala/za/co/absa/db/balta/classes/QueryResultRow.scala) class

This class represents a row in a query result. It provides methods for accessing the columns of the row - via name or index.

To make specific Postgres types available in `QueryResultRow` there is the implicit class 
[`Postgres.PostgresRow`](https://github.com/AbsaOSS/balta/blob/master/balta/src/main/scala/za/co/absa/db/balta/implicits/Postgres.scala)
enhancing the `QueryResultRow` with the ability to get type like `JSON` (including JSONB) and `JSON[]` (JSON array).

There is also an implicit class 
[`QueryResultImplicits.ProductTypeConvertor`](https://github.com/AbsaOSS/balta/blob/master/balta/src/main/scala/za/co/absa/db/balta/implicits/QueryResultImplicits.scala) 
enhancing  the `QueryResultRow` with the ability to convert the row to a product type (case class or tuple) via function
`toProductType[T]`.





## How to test the library

Use the `test` command to execute all unit tests, skipping all other types of tests.
```bash
sbt test
```

There are integration tests part of the package that can be run with the following command:
```bash
sbt testIT
```

If you want to run all tests, use the following command.
```bash
sbt testAll
```

The integrations tests to finish successfully, a Postgres database must be running and populated.
* by default the database is expected to be running on `localhost:5432`
* if you wish to run against a different server modify the `src/test/resources/database.properties` file
* to populate the database run the scripts in the `src/test/resources/db/postgres` folder

## How to generate JaCoCo code coverage report

Run the following command from path `{project-root}`
```bash
sbt jacoco
```
Report should be available on path `{project-root}/balta/target/scala-2.12/jacoco/report/html`


## How to Release

Please see [this file](RELEASE.md) for more details.

## Known Issues

### Postgres
* `TIMESTAMP WITH TIME ZONE[]`, `TIME WITH TIME ZONE[]`, generally arrays of time related types are not translated to appropriate time zone aware Scala/Java types
* Conversion to product type won't work if there are complex members in the product type like subclasses; with container types like `List`, it hasn't been tested 