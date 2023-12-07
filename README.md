# Balta

Scala library to write Postgres DB code tests with

Balta is a Scala library to help creating database tests, particularly testing Database functions. It is based on the 
popular [ScalaTest](http://www.scalatest.org/) library and uses [PostgreSQL](https://www.postgresql.org/) as the 
database engine.

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


## How to generate JaCoCo code coverage report
Run command from path `{project-root}`
```
sbt jacoco
```
Report should be available on path `{project-root}/balta/target/scala-2.12/jacoco/report/html`
