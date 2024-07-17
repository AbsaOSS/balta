# Balta

Scala library to used for writing Postgres DB code tests.

---

### Build Status

[![Build](https://github.com/AbsaOSS/balta/workflows/Build/badge.svg)](https://github.com/AbsaOSS/fa-db/actions)

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/za.co.absa..db/balta_2.12/badge.svg)](https://search.maven.org/search?q=g:za.co.absa.fa-db)

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

## How to Release

Please see [this file](RELEASE.md) for more details.
