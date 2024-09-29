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

Advantages of this approach is that the tests repeateble, they are isolated from each other and the database is always 
in a known state before and after each test.

## How to Test
There are integration tests part of the package that can be run with the following command:

```bash
sbt testIT
```

The tests to finish successfully, a Postgres database must be running and populated.
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
