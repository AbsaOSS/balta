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
- When a unit test adds value — write one:
  - Method has any own logic: branching (`if`/`match`), exception handling or swallowing, non-trivial transformation, config/resource read, or reflection.
- When to add to `jmf-rules.txt` instead of writing a unit test:
  - Body is a single call with no own logic: forwards to another overload, calls its non-deprecated replacement, returns a field, or wraps a constructor with no transformation.
  - Litmus: "Does this method have any logic of its own?" — No → JMF.
- Global rule collision check (CRITICAL):
  - When adding any new method, check if it matches a pattern in the `# GLOBALS RULES` section of `jmf-rules.txt` (line ~22+).
  - If method name matches a global rule AND the method has domain logic: immediately create an INCLUDE rescue rule (`+FQCN#method(*)`) in the `# INCLUDE RULES` section of `jmf-rules.txt`.
  - High-risk method names: refer to `jmf-rules.txt` GLOBALS RULES section for complete list; most common collisions: `apply()`, `toString()`, `equals()`, `copy()`, `name()`, `groups()`, `optionalAttributes()`.
  - Rationale: Broad global rules designed for compiler-generated boilerplate can silently hide coverage for domain methods. INCLUDE rules rescue specific methods from broad exclusions.
  - Example: If adding `def apply(id: String): Record`, add `+*Record$#apply(*)  id:keep-record-factory` to rescue from the `*$*#apply(*)` global rule in GLOBALS RULES section.
- Review rule — JMF drift check: when modifying a method that appears in `jmf-rules.txt`, verify its body still qualifies; if own logic has been added, remove the JMF rule and write a unit test instead.

Tooling
- Must format with scalafmt (`.scalafmt.conf`); lint with scalastyle (`scalastyle-config.xml`) or wartremover as configured.
- Compiler warnings treated as errors where configured; coverage ≥ 80% via sbt-jacoco (excluding JMF-filtered methods in `jmf-rules.txt`).

Coverage filtering (JMF)
- A method qualifies when its body is a single call with no own logic (see Testing section for the full decision rule and qualifying patterns).
- Must not add JMF rules for methods with branching logic, error handling, or non-trivial transformations.
- Rules file: `jmf-rules.txt` (version `[jmf:1.0.0]`); one rule per line, `#` comments and blank lines ignored.
- Rule syntax: `<FQCN_glob>#<method_glob>(<descriptor_glob>) [FLAGS] [PREDICATES]`
  - FQCN_glob: dot-form class pattern (`*`, `*.model.*`, `com.example.*`; `$` for inner/companion classes).
  - method_glob: glob on method name (`copy`, `get*`, `$anonfun$*`, `*_$eq`).
  - descriptor_glob: JVM descriptor `(args)ret`; omitting or `(*)` means any args/any return. Must use JVM internal format: `(I)*` not `(int)`, `(Z)*` not `(boolean)`, `(Ljava/lang/String;)*` not `(java.lang.String)`. Non-matching descriptors are silently ignored — no warning is emitted.
  - FLAGS (space/comma separated): `public`, `protected`, `private`, `synthetic`, `bridge`, `static`, `abstract`.
  - PREDICATES: `ret:<glob>` (return type), `id:<string>` (log label), `name-contains:<s>`, `name-starts:<s>`, `name-ends:<s>`.
- Every rule Must include an `id:` label for traceability.
- Adoption order: start with CONSERVATIVE (case-class boilerplate, compiler synthetics), then STANDARD; use AGGRESSIVE only for DTO/auto-generated packages.
- Prefer narrow package scopes; prefer `synthetic`/`bridge` flags for compiler artifacts over broad wildcards.
- Must prefix project-specific FQCN globs with `*` so they match the full qualified class name (e.g. `*QueryResult#noMore()`, not `QueryResult#noMore()`).
- When adding a project-specific rule, add it under the `# PROJECT RULES` section with a comment explaining why the method qualifies.

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
- JMF silent failures: rules with non-matching FQCN or descriptor globs are silently ignored; always verify new rules take effect by comparing JaCoCo method-level output before and after.
- Scala reflection: case classes used with `currentMirror.reflectClass` in tests must be top-level (package scope), not inner classes — Scala reflection cannot handle inner class mirrors.

Learned rules
- Must not change `QueryResultRow` getter return types or `stringPerConvention` output for existing scenarios.
- Must not change externally-visible SQL generation patterns without updating dependent tests.
- Scala 2.12 compiler artifacts that generate coverable bytecode: `$anonfun$` lambdas (ACC_SYNTHETIC), `$deserializeLambda$` (private static, not synthetic), Function1 mixin forwarders (`andThen`/`compose`), value class `$extension` methods, `$default$` parameter methods, Iterator trait mixin (~80+ forwarders). These are JMF candidates, not unit-test candidates.

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
