# JMF (jacoco-method-filter) — Lessons Learned

Practical notes for anyone maintaining or extending `jmf-rules.txt` in this project.
Accumulated from real debugging sessions; each entry caused at least one silent coverage regression.

---

## 1. Descriptor format must be JVM internal, not human-readable

**Wrong:**
```
*QueryResultRow#apply(int)*          id:qrr-apply-int
*QueryResultRow#getAs(java.lang.String,*)*  id:qrr-getas-str
```
**Right:**
```
*QueryResultRow#apply(I)*            id:qrr-apply-int
*QueryResultRow#getAs(Ljava/lang/String;*)*  id:qrr-getas-str
```

JMF compares against raw JVM bytecode descriptors, not source-level types.
Common type mappings:

| Source type    | JVM descriptor               |
|----------------|------------------------------|
| `int`          | `I`                          |
| `boolean`      | `Z`                          |
| `long`         | `J`                          |
| `double`       | `D`                          |
| `String`       | `Ljava/lang/String;`         |
| `Option[A]`    | `Lscala/Option;`             |
| `Unit` / void  | `V`                          |
| array of int   | `[I`                         |

Use `javap -p -verbose <ClassFile.class>` to see the actual descriptors for any method.

---

## 2. FQCN globs must start with `*` to match qualified class names

**Wrong:**
```
QueryResult#noMore()   id:qr-nomore
```
**Right:**
```
*QueryResult#noMore()  id:qr-nomore
```

Without the leading `*`, the glob is matched literally and will never match
`za.co.absa.db.balta.classes.QueryResult`.

---

## 3. Non-matching rules are silently ignored — no warning, no error

JMF does not report unmatched rules. A rule can be completely wrong and the tool will
load it, count it, and do nothing — while JaCoCo continues to count the method as missed.

**Consequence:** Coverage numbers may look fine if another test happens to cover the same
method via a different path (e.g., an integration test). The rule appears to work for months,
then suddenly "breaks" when that test is removed or the method is called less often.

**How to verify a rule is actually filtering:**
1. Run `sbt ++2.12.18 jacoco` and note the JMF log line: `Marked N methods`.
2. Add the new rule.
3. Run again and confirm `N` increased.
4. Cross-reference `balta/target/scala-2.12/jacoco/jacoco.xml` to confirm the specific
   method now has `mi="0"` (missed instructions = 0) AND `ci` dropped by the expected amount.

---

## 4. Diagnosing a missed-coverage method

When JaCoCo reports missed instructions after you believe you have a rule:

1. Find the method in `jacoco.xml`:
   ```
   grep -A2 'name="myMethod"' balta/target/scala-2.12/jacoco/jacoco.xml
   ```
2. Check `mi` (missed instructions) and `nr` (not-reached branches).
3. Get the actual bytecode descriptor:
   ```
   javap -p -verbose balta/target/scala-2.12/classes/za/co/absa/.../MyClass.class \
     | grep -A4 "myMethod"
   ```
4. Compare the descriptor in your rule against the bytecode output.
   Common mistakes: `(int)` vs `(I)`, missing `;` after object type, `*` vs explicit return type.

---

## 5. Scala 2.12 compiler-generated methods that produce coverable bytecode

These arise automatically and are JMF candidates (no own logic), not unit-test candidates:

| Pattern | Description |
|---------|-------------|
| `$anonfun$*` (ACC_SYNTHETIC) | Lambda bodies lifted to class methods |
| `$deserializeLambda$` (private static, NOT synthetic) | SerializedLambda support |
| `andThen` / `compose` | Function1 trait mixin forwarders |
| `*$extension` | Value class extension methods (boxed path) |
| `*$default$*` | Default parameter methods (`$default$1`, `$default$2`, …) |
| Iterator trait mixins (~80+) | `hasNext`, `next`, `drop`, `take`, `sliding`, etc. |
| `writeReplace` | Java serialization hook for case classes |

When a method's coverage is missed and its source is compiler-generated, add a JMF rule
rather than writing a test.

---

## 6. Verifying new rules end-to-end

Minimal workflow before committing any new JMF rule:

```bash
# 1. baseline — note "Marked N methods" in sbt output
sbt '++2.12.18; jacoco'

# 2. add rule to jmf-rules.txt

# 3. re-run — "Marked N+k methods" confirms the rule matched
sbt '++2.12.18; jacoco'

# 4. quick coverage check
awk -F',' 'NR>1{m+=$5; c+=$6} END{printf "Missed: %d  Covered: %d  Total: %d  Coverage: %.1f%%\n", m, c, m+c, c*100/(m+c)}' \
  balta/target/scala-2.12/jacoco/jacoco.csv
```

---

## 7. Avoid broad wildcards — prefer `synthetic`/`bridge` flags

A rule like `*#*$anonfun$*(*)` with the `synthetic` flag is safer than an unqualified
glob that might accidentally suppress coverage for hand-written lambdas.

```
*#$anonfun$*(*)  synthetic  id:scala-anonfun
```

The `synthetic` flag restricts the rule to bytecode methods with ACC_SYNTHETIC set,
which is exactly what the Scala compiler emits for lifted lambdas.

---

## 8. Scala reflection in tests — case classes must be top-level

When writing unit tests that call `currentMirror.reflectClass` (e.g., for
`toProductType`-style tests), the case class **must** be declared at package scope,
not as a nested/inner class inside the test class.

```scala
// WRONG: inner class, reflection fails at runtime
class MyUnitTests extends AnyFunSuite {
  case class MyRecord(id: Int, name: String)
  test("...") { ... currentMirror.reflectClass(mirror) ... } // NoHostException
}

// RIGHT: top-level, reflection works
case class MyRecord(id: Int, name: String)
class MyUnitTests extends AnyFunSuite {
  test("...") { ... currentMirror.reflectClass(mirror) ... } // OK
}
```

---

## 9. Rule file version header

The first non-comment line must be `[jmf:1.0.0]` (or the appropriate version).
Missing or malformed version headers cause the entire rule file to be skipped silently.
Confirm the file is loaded by checking the sbt jacoco log for `Loaded N rules`.

---

## 10. Misleading or incorrect hints in the official JMF tutorial

The bundled HowTo comment block (`jmf-rules.txt` header) contains several claims that are
inconsistent, misleading, or contradicted by tested behaviour. Do not copy examples from it
without checking below.

### 10a. Colon-prefix flag syntax in Quick Examples is wrong

The Quick Examples section writes:
```
*#*(*):synthetic            # any synthetic
*#*(*):bridge               # any bridge
```
The colon before `synthetic`/`bridge` does **not** match the FLAGS syntax described just
above it, which says "space or comma separated". Our confirmed working syntax is:
```
*#*  synthetic  id:some-label
*#$anonfun$*(*)  synthetic  name-contains:$anonfun$  id:scala-anonfun
```
The colon-prefix format may be silently ignored (rule loads but never matches), which is
the same failure mode as wrong descriptors. Do not use `:<flag>` — use space-separated flags.

### 10b. `ret:` predicate syntax is inconsistent within the tutorial

The tutorial shows `ret:` two different ways:
```
# inconsistent — colon-prefixed
*.jobs.*#*(*):ret:V

# inconsistent — space-separated
*.client.*#with*(*) ret:Lcom/api/client/*;
```
The space-separated form matches the PREDICATES spec. Use space-separated:
```
*MyClass#myMethod(*)  ret:V  id:my-label
```
Never use `:ret:` (double colon, colon-prefixed).

### 10c. Tutorial does not warn about the FQCN `*` prefix requirement

All Quick Examples use fully-anchored package globs like `*.model.*#copy(*)`, which
implicitly start with `*`. The tutorial never states that a bare class name
(`QueryResult#noMore()`) will NOT match a fully-qualified name
(`za.co.absa.db.balta.classes.QueryResult`).

This silence has caused real bugs: rules written as `ClassName#method()` compile and load,
appear in the "N rules loaded" count, but never filter anything.

**Always prefix project-specific class names with `*`:**
```
# WRONG — silently never matches
QueryResult#noMore()  id:qr-nomore

# RIGHT
*QueryResult#noMore()  id:qr-nomore
```

### 10d. Tutorial descriptor examples do not demonstrate JVM-format requirement

The ALLOWED SYNTAX section correctly shows JVM format (`(I)I`, `(Ljava/lang/String;)V`)
but the Quick Examples section uses only `(*)` everywhere, giving the impression that
type-specific descriptors are rarely needed. In practice:

- Any rule targeting a method that takes `int`, `boolean`, or any object type by specific
  type **must** use JVM format.
- Writing `(int)`, `(boolean)`, `(java.lang.String)` will silently not match.
- The tutorial provides no warning that human-readable types produce silent failures.

See section 1 of this file for the correct mapping table.

### 10e. Tutorial claims empty/short descriptor forms are equivalent — verify before trusting

The tutorial states: `"(*)" normalizes to "(*)*" ⇒ any args, any return.`
This is consistent with our tested experience for wildcard rules. However, when mixing
`(*)` with typed descriptors in the same class, `(*)` may match more broadly than intended.
Prefer an explicit descriptor when targeting a specific overload.

### 10f. Empty parens `()` reads as "no args" but means "any args" due to normalisation

The tutorial global rule examples include:
```
*#productElement()
*#productArity()
*#productIterator()
```
A reader naturally interprets `()` as "this method takes no arguments". But `productElement`
has the signature `def productElement(n: Int): Any` — its JVM descriptor is `(I)Ljava/lang/Object;`.
The rule still works because `()` normalises to `(*)*` (any args, any return).

**Consequence:** These rules match more than you think. A rule written as `*#myMethod()` to
target a specific no-arg overload will also match every overload of `myMethod`, including
ones you may want to test. Use an explicit descriptor when targeting a single overload:
```
*#mySpecificMethod()V          # actually no-arg, returns void
*#myOverloadedMethod(I)*       # specifically the int overload
```

### 10g. Tutorial's own Quick Example comments use human-readable types

The formal ALLOWED SYNTAX section correctly shows JVM format, but the very next section's
inline explanatory comments contradict it:
```
*.model.*#productElement(*)
    → Matches `productElement(int)` (or any descriptor form) on case classes.
                            ^^^^ human-readable — not valid if used in a rule
```
The parenthetical "(or any descriptor form)" hints at the issue but does not explain it.
Any reader who copies the comment text into a rule will get a silently non-matching rule.

### 10h. `ret:` object type globs — semicolon inconsistency

The tutorial shows `ret:` for object types with and without a trailing `;`:
```
*.client.*#with*(*) ret:Lcom/api/client/*;       # has semicolon
*.model.*#*(*):ret:Lcom/example/model/*          # missing semicolon
```
JVM object type descriptors always end with `;` (e.g., `Ljava/lang/String;`). The form
without `;` may silently fail to match for the same reason a wrong descriptor does.
Always include the trailing semicolon for object return type globs:
```
*MyClass#myMethod(*)  ret:Lcom/example/model/*;  id:my-label
```

### 10i. `id:` listed under PREDICATES implies it is optional — it is not

The tutorial lists `id:<string>` in the PREDICATES section alongside `ret:`, `name-contains:`,
etc., with the description "identifier shown in logs/reports". Nothing in the tutorial says
it is required.

In practice, omitting `id:` makes log output unreadable when rules fire — you see only the
method descriptor with no human-readable label. Treat `id:` as mandatory on every rule.
This project's instructions reflect this: "Every rule Must include an `id:` label for
traceability."

### 10j. `#` is both the comment marker and the FQCN/method separator — inline comments are ambiguous

The tutorial states "Comments (# …) and blank lines are ignored" but never clarifies that
`#` in the middle of a rule is the FQCN/method separator, not a comment.

This creates an ambiguity for inline trailing comments:
```
*$#<init>(*)  id:gen-ctor # constructors
```
Is `# constructors` an inline comment that JMF drops, or does it confuse the parser?
In our tested rules it appears to be harmless, but the tutorial is completely silent on
this. To be safe, keep `#`-style inline comments only on dedicated comment lines.
Use the `id:` label as the sole annotation on rule lines:
```
# constructors
*$#<init>(*)  id:gen-ctor
```

### 10k. CONSERVATIVE / STANDARD / AGGRESSIVE labels mentioned in intro but never applied to examples

The Quick Start says:
> "2) Start with the CONSERVATIVE section only. 3) If clean, enable STANDARD. Use AGGRESSIVE
> only inside DTO/auto-generated packages."

But the rule examples that follow are not labelled CONSERVATIVE, STANDARD, or AGGRESSIVE.
There is no way to know which category `*.model.*#copy(*)` or `*#$anonfun$*` belongs to
without guessing. In our project we use the section header comments to mark intent instead:
`# GLOBALS RULES`, `# PROJECT RULES`.

### 10l. "Always use dot-form" note contradicts necessary `$` usage

The "Notes" section says:
> "Always use dot-form (com.example.Foo) for class names."

But inner classes and companion objects require `$`, not `.`:
- `com.example.Foo$Bar` — inner class `Bar` of `Foo`
- `com.example.Foo$` — companion object of `Foo`

Both are used throughout the tutorial's own examples (`*.model.*$*#apply(*)`). The "dot-form"
note means "use `.` as the package separator" (not `/`), not "never use `$`". The note needs
the clarification: `$` is the required inner-class/companion separator and must be used as-is.

### 10m. Universal wildcard `*#*(*)` missing warning about falsely inflated coverage

The tutorial says:
> "`*#*(*)` — Match EVERY method in EVERY class. Useful only for diagnostics."

It does not warn that enabling this globally will suppress JaCoCo for every method in the
codebase, producing artificially inflated (up to 100%) coverage numbers. This rule left
enabled in a CI pipeline would silently mask all regressions. It should carry an explicit
**DO NOT commit** warning.

### 10n. Scala var setter compiled name not explained for `_$eq` glob

The tutorial shows:
```
*.dto.*#*_$eq(*)
    → Matches Scala var setters in DTO packages (e.g., `name_=(...)`).
```
The source-level name `name_=` is compiled to `name_$eq` in bytecode. The tutorial
uses `_$eq` as the glob suffix but never explains *why* — a reader unfamiliar with
Scala's name mangling might try `*_=(*)` (the source form), which will never match.
Always use the bytecode name in JMF globs, not the source name.

---

## 12. Real bugs found in this project's own jmf-rules.txt via audit

These were discovered by running the same JVM-descriptor audit described in section 1
and cross-referencing against the knowledge in section 10d. Both rules silently failed
to match and were only "covered" because integration tests happened to exercise the
same methods. **Fixed in jmf-rules.txt.**

| Rule (before fix) | Problem | Fix |
|---|---|---|
| `*DBFunction#execute(scala.Function1)` | Human-readable class name | `(Lscala/Function1;)*` |
| `*NamingConvention#fromClassNamePerConvention(java.lang.Object)` | Human-readable class name | `(Ljava/lang/Object;)*` |

Audit command used to find these:
```bash
grep -n '[a-z]\.[A-Z]' jmf-rules.txt | grep -v '^#'
```
This finds lines where a lower-case package segment is followed by an upper-case class
name in descriptor position — a strong indicator of human-readable format.

---

## 13. Scala source name vs bytecode name — glob always targets bytecode

JMF operates on bytecodes, so any glob must use the compiled method name, not the source
name. Common Scala name-mangling patterns:

| Source name | Bytecode name |
|---|---|
| `name_=` (var setter) | `name_$eq` |
| `a_+_b` (operator) | `a_$plus_b` |
| `?` | `$qmark` |
| `!` | `$bang` |
| `++` | `$plus$plus` |
| `::` | `$colon$colon` |
| Inner class `Foo.Bar` | `Foo$Bar` |
| Companion object `Foo` | `Foo$` |
| Lambda lifted from `foo` | `$anonfun$foo$1` |

Use `javap -p classfile` to confirm the exact bytecode name before writing a glob.

---


## 11. Quick reference — JMF rule anatomy

```
<FQCN_glob>#<method_glob>(<descriptor_glob>)  [FLAGS]  [PREDICATES]  id:<label>
```

- `FQCN_glob`: `*MyClass`, `*.model.*`, `com.example.*`; `$` for inner/companion.
- `method_glob`: `copy`, `get*`, `$anonfun$*`, `*_$eq`.
- `descriptor_glob`: JVM bytes, e.g., `(I)*`, `(Ljava/lang/String;)*`, `(*)`.
- `FLAGS`: `public`, `protected`, `private`, `synthetic`, `bridge`, `static`, `abstract`.
- `PREDICATES`: `ret:<glob>`, `name-contains:<s>`, `name-starts:<s>`, `name-ends:<s>`.
- Every rule must have `id:<label>` for traceability.
