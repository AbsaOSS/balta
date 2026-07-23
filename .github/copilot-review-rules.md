# Copilot Review Rules

Purpose
- Define consistent, concise, action-oriented review behavior.
- Use short headings and bullet lists; prefer do/avoid constraints over prose; point to code and impact.

Mode: Default review
- Scope: single PR, normal risk.
- Priority order: correctness → security → tests → maintainability → style.
- Checks:
  - Correctness: logic bugs, missing edge cases, regressions, contract changes.
  - Security: unsafe input handling, secrets exposure, auth/authz issues, insecure defaults.
  - Tests: tests exist for changed logic; success + failure paths covered.
  - Maintainability: unnecessary complexity, duplication, unclear naming/structure.
  - Style: flag only when readability or repo conventions are broken.
- Response format: short bullets; files + line ranges; severity groups (Blocker / Important / Nit); minimal actionable suggestions; no rewrites.

Mode: Double-check review
- Scope: higher-risk PRs (security, infra/CI, wide refactors, data migrations, auth changes).
- Additional focus:
  - Confirm previous review comments were addressed.
  - Re-check: auth, permissions, secrets, persistence, external calls, concurrency.
  - Hidden side effects: backward compatibility, failure modes, retries, idempotency.
  - Safe defaults: least privilege, secure logging, safe error messages, missing-input behavior.
- Response format: comment only where risk/impact is non-trivial; no repeated style notes; call out explicit risk acceptance (what / why acceptable / mitigation).

Commenting rules (all modes)
- Every comment must state: what the issue is · why it matters · minimal fix suggestion.
- Prefer linking to existing repo patterns over introducing new ones.
- If context is missing, ask a targeted question instead of assuming.

Non-goals
- No refactors unrelated to the PR's intent.
- No formatting bikeshedding when the formatter/linter handles it.
- No architectural rewrites unless explicitly requested.

Repo additions
- High-risk areas:
  - DB transactions: `DBTestSuite` rollback/commit; do not change `persistData` behavior unintentionally.
  - SQL injection: parameter binding uses `PreparedStatement`; no raw string concatenation of untrusted input.
  - JDBC lifecycle: connections must be rolled back or committed; validate `try/finally` in `DBQuerySupport`.
  - Contract-sensitive: `QueryResultRow` getter signatures; `stringPerConvention` output; `jmf-rules.txt`.
  - Deprecations: do not extend `setParamNull`/`addNull` deprecated API surface.
- Required tests:
  - `*UnitTests`: no DB connection; mock `PreparedStatement`/`ResultSet` if needed.
  - `*IntegrationTests`: extend `DBTestSuite` or `DBTestingConnection`.
  - New naming convention implementations must have a `UnitTests` covering all `LettersCase` variants.
