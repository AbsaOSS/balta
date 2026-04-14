Copilot instructions

Purpose
- Define consistent style; keep externally-visible behavior stable unless intentionally changing the contract.
- Sections order, bullet lists, and constraint words (Must / Must not / Prefer / Avoid) must be consistent across repos.
- Must keep one blank line at end of file.

Context
- Scala library consumed via sbt in CI/CD; treat JDBC connections and DB state as boundary concerns.
- Must keep library API backward-compatible unless a version bump is intentional.

Coding guidelines
- Must keep changes small and focused; prefer explicit over clever.
- Must keep pure logic free of environment access; route I/O through a single boundary module.
- Must keep externally-visible strings, formats, and exit codes stable unless intentional.

Output discipline
- Aim ≤ 10 lines in final recap; link and summarize deltas rather than pasting large content.
- Prefer actionable bullets; avoid deep rationale unless requested.
- Code changes must end with: what changed / why / how to verify.

PR Body Management
- Treat PR description as a changelog; append under `## Update [YYYY-MM-DD]` or `## Changes Added`.
- Must not rewrite the entire PR body; must reference the commit hash that introduced the change.

Configuration
- Must read DB connection details from `database.properties` test resource; centralize in `ConnectionInfo`.
- Must not hard-code credentials; document required vs optional `ConnectionInfo` fields with defaults.

Language and style
- Must target Scala 2.12 primary; cross-compile 2.11 and 2.13 where configured.
- Must add ScalaDoc for new public methods and classes.
- Must use slf4j (`LazyLogging` or explicit `Logger`), not `println`/`print`; pass logger arguments lazily (call-by-name), never pre-build interpolated strings.
- Must keep imports at top of file, grouped: stdlib → third-party → project-internal.
- Must include the Apache 2.0 copyright/license header in every source file using the project first-copyright year.
- Prefer `s"..."` interpolation for non-logging templates; avoid interpolation inside `logger.*` calls.

Docstrings and comments
- ScalaDoc: short summary line first; prefer Parameters/Returns/Raises sections; avoid tutorials and long prose.
- Comments: comment intent/edge cases only; avoid restating the code.

Patterns
- Prefer raising exceptions in leaf modules; translate to test/library failure at the entry point (`DBTestSuite.test()`).
- Must keep boundaries mockable; must not call real external systems (DB, APIs) in unit tests.

Testing
- Must use ScalaTest under `balta/src/test/scala/`.
- Must test return values, exceptions, and error messages.
- Unit test files: name ends with `UnitTests`; must not require a DB connection; mock JDBC (`PreparedStatement`/`ResultSet`) if needed.
- Integration test files: name ends with `IntegrationTests`; must extend `DBTestSuite` or mix in `DBTestingConnection`.
- Must not access private members of the class under test.
- Prefer `AnyFunSuite`/`AnyFunSuiteLike`; use `AnyWordSpec` only when given/when/then adds clarity.
- Must place shared test helpers and fixtures in the root test package (`za.co.absa.db.balta`) and reuse them across tests; shared files are excluded from the filename-inspector check.
- Must not add top-level comments in `*Tests.scala` outside test methods; use nested `"..."` / `should` / `when` blocks (idiomatic to the chosen suite style) to separate groups.
- Prefer TDD workflow:
  - Must create or update `SPEC.md` before writing any code, listing scenarios, inputs, and expected outputs.
  - Must propose the full test case set (name + intent + input + expected output) and wait for user confirmation before coding.
  - Must write all failing tests first (red), then implement until all pass (green).
  - Must cover all distinct combinations; each test must state its scenario in the ScalaDoc.
  - Must update `SPEC.md` after all tests pass with the confirmed test case table.

Tooling
- Must format with scalafmt (`.scalafmt.conf`); lint with scalastyle (`scalastyle-config.xml`) or wartremover as configured.
- Compiler warnings treated as errors where configured; coverage ≥ 80% via sbt-jacoco (excluding JMF-filtered methods in `jmf-rules.txt`).

Coverage filtering (JMF)
- A method qualifies for a JMF filter rule if it meets at least one criterion:
  - No added value: trivial delegate, one-liner factory, or pure field accessor — a test adds no assurance beyond testing the delegated method.
  - Not coverable without integration tests: requires a DB/external system; same path already exercised by integration tests.
- Qualifying patterns: deprecated single-call delegates; `columnLabel: String` overloads that only call `columnNumber(label)` + Int-based overload; one-liner factory wrappers; implicit single-field accessors.
- Must not add JMF rules for methods with branching logic, error handling, or non-trivial transformations.

Quality gates
- sbt "testOnly *UnitTests"   # unit tests, no DB needed
- sbt test                     # all tests, DB must be running
- sbt jacoco                   # coverage → balta/target/scala-*/jacoco/
- sbt scalafmtCheck            # format check
- sbt scalastyle               # lint

Common pitfalls to avoid
- Dependencies: verify compatibility across all cross-compiled Scala versions.
- Unsafe casts: avoid `.asInstanceOf`; document why when unavoidable.
- Logging: never pre-build log strings; always pass args lazily (call-by-name).
- Cleanup: remove unused imports/variables (scalac `-Ywarn-unused`).
- Stability: avoid changing externally-visible method signatures, SQL output, or parameter binding order.

Learned rules
- Must not change `QueryResultRow` getter return types or `stringPerConvention` output for existing scenarios.
- Must not change externally-visible SQL generation patterns without updating dependent tests.

Repo additions
- Project name: balta
- Entry points: `build.sbt`, `balta/src/main/scala/za/co/absa/db/balta/DBTestSuite.scala`
- Core package: `za.co.absa.db.balta`; supporting: `za.co.absa.db.mag` (naming conventions)
- DB boundary: `DBConnection` (JDBC wrapper), `DBTestSuite` (transaction rollback/commit)
- Contract-sensitive: SQL generation in `DBTable`/`DBFunction`; parameter binding order; `QueryResultRow` getter return types; `stringPerConvention` output
- Coverage tool: sbt-jacoco with JMF filter rules in `jmf-rules.txt`
- Commands:
  - sbt "testOnly *UnitTests"   # unit tests, no DB needed
  - sbt test                     # all tests, DB must be running
  - sbt jacoco                   # coverage report
