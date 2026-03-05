# Technical Specification

# 0. Agent Action Plan

## 0.1 Executive Summary

Based on the bug description, the Blitzy platform understands that the bug is the complete absence of a functional Java-based Age Calculator application in the repository. The project `05march_1-age` requires a console application that accepts a user's Date of Birth in `DD/MM/YYYY` format, computes the exact age in years, months, and days using Java's `java.time` API, validates all inputs defensively, and displays the result in a human-readable format. Currently, the repository is in a scaffold state containing only a single `README.md` file with the heading `# 05march_1-age` — no Java source files, no build configuration, and no test infrastructure exist.

**Technical Failure Classification:** Missing Implementation — the required Java application logic, input validation, and OOP class structure have not been created.

**Precise Technical Description:**
- The repository at `github.com/shaliniblitzy/05march_1-age` contains zero executable source files
- No Java classes exist to accept `DD/MM/YYYY` input, parse it via `java.time.format.DateTimeFormatter`, compute age via `java.time.Period.between()`, or display results
- No input validation exists to reject future dates, logically impossible dates (e.g., `31/02/2020`), or malformed strings
- No exception handling (`try-catch`) scaffolding exists to provide meaningful error messages

**Reproduction Steps:**
- Clone the repository
- Attempt to compile any Java source: `javac src/*.java` — fails (no `.java` files found)
- Attempt to run the application: `java Main` — fails (class not found)

**Error Type:** Missing implementation / no-op state — the application cannot function because no code has been committed.

**User-Specified Technical Requirements (Preserved Exactly):**
- Use `java.time.LocalDate`, `java.time.Period`, `java.time.format.DateTimeFormatter`
- Accept DOB as input in `DD/MM/YYYY` format
- Calculate exact age in years, months, and days
- Display result: `Your age is X years, Y months, and Z days.`
- Validate: DOB must not be a future date; handle invalid dates (e.g., `31/02/2020`)
- Follow Object-Oriented Programming principles
- Use proper exception handling with `try-catch`
- Handle leap years correctly
- Work for users born in any valid year

## 0.2 Root Cause Identification

Based on research, THE root cause is: **No Java source code has been implemented in the repository, rendering the Age Calculator application entirely non-functional.**

**Located in:** The repository root directory — the entire `src/` directory and all Java class files are absent.

**Triggered by:** The project is in its initial scaffold state. The only committed artifact is `README.md` (line 1: `# 05march_1-age`), confirmed by direct repository inspection. No Java files, no build manifests (`pom.xml`, `build.gradle`), and no package structure exist.

**Evidence from Repository Analysis:**

| Finding | Evidence |
|---------|---------|
| Repository root contains only `README.md` | `get_source_folder_contents("")` returned a single child: `README.md` |
| `README.md` is a single-line file | Line 1: `# 05march_1-age` — no installation instructions, no usage guide |
| No Java source files anywhere | Repository tree shows zero `.java` files |
| No build configuration | No `pom.xml`, `build.gradle`, or `Makefile` detected |
| No test infrastructure | No test directory, no JUnit dependencies |

**Secondary Root Causes (Technical Gaps to Address):**

- **RC-1: No Date Parsing Implementation** — Without a `DateTimeFormatter` configured with the `dd/MM/uuuu` pattern and `ResolverStyle.STRICT`, the application cannot accept or validate `DD/MM/YYYY` input. The default `DateTimeFormatter.ofPattern("dd/MM/yyyy")` uses `ResolverStyle.SMART`, which silently adjusts invalid dates (e.g., Feb 30 becomes Feb 28) instead of rejecting them. Using `uuuu` (proleptic year) instead of `yyyy` (year-of-era) is required for strict mode to function correctly with `LocalDate`.

- **RC-2: No Age Calculation Logic** — Without `Period.between(dob, currentDate)` invocation, no years/months/days computation can occur. The `Period` class correctly handles leap year boundaries and calendar edge cases natively.

- **RC-3: No Input Validation Pipeline** — Without explicit validation, future dates, logically impossible dates (e.g., `31/02/2020`, `00/13/2000`), and malformed strings cannot be rejected with meaningful error messages.

- **RC-4: No OOP Structure** — Without dedicated classes separating concerns (input handling, validation, computation, result modeling), the application violates the OOP principles specified in the requirements.

**This conclusion is definitive because:** Direct inspection of the repository confirms zero source files exist. The `README.md` contains only a project identifier heading. The tech spec (§1.2.1) explicitly confirms the repository is in its "initial scaffold state" with "no source code yet committed." All functionality must be created from scratch.

## 0.3 Diagnostic Execution

### 0.3.1 Code Examination Results

- **File analyzed:** `README.md` (the only file in the repository)
- **Problematic code block:** Lines 1-1 (entire file)
- **Specific failure point:** No Java source files exist at any path in the repository
- **Execution flow leading to bug:** Repository clone → no `src/` directory → no `.java` files → compilation fails → application cannot run

The repository is in a greenfield scaffold state. There is no Java code to trace through, no classes to examine for logic errors, and no methods to inspect for incorrect API usage. The entire application must be authored.

### 0.3.2 Repository Analysis Findings

| Tool Used | Command Executed | Finding | File:Line |
|-----------|-----------------|---------|-----------|
| `get_source_folder_contents` | `folder_path=""` | Repository root contains only `README.md` | Root directory |
| `read_file` | `README.md [1, -1]` | Single line: `# 05march_1-age` | `README.md:1` |
| `bash` | `find / -name ".blitzyignore" 2>/dev/null` | No `.blitzyignore` files found | N/A |
| `bash` | `java -version` | OpenJDK 21.0.10 installed on system | N/A |
| `bash` | `javac -version` | javac 21.0.10 available for compilation | N/A |
| `bash` | `ls /tmp/environments_files/` | No environment files provided by user | N/A |

### 0.3.3 Web Search Findings

**Search Queries Executed:**
- `java.time.Period age calculation LocalDate edge cases leap year`
- `Java DateTimeFormatter DD/MM/YYYY parse DateTimeParseException invalid date`
- `Java ResolverStyle STRICT uuuu DD/MM/YYYY DateTimeFormatter validate invalid dates`

**Web Sources Referenced:**
- Baeldung — Calculate Age in Java (baeldung.com/java-get-age)
- HowToDoInJava — Java Strict Date Parsing with ResolverStyle (howtodoinjava.com)
- Baeldung — Check If a String Is a Valid Date in Java (baeldung.com/java-string-valid-date)
- Mkyong — How to Check if Date is Valid in Java (mkyong.com)
- JodaOrg/joda-time Issue #295 — Wrong period calculation for leap years (GitHub)
- Medium — Java's Period.between() Method Explained

**Key Findings and Discoveries Incorporated:**

- **CRITICAL: Use `uuuu` not `yyyy` with `ResolverStyle.STRICT`** — In Java's `DateTimeFormatter`, `yyyy` represents "year-of-era" (always positive, AD/BC context), while `uuuu` represents "proleptic year" (signed). When `ResolverStyle.STRICT` is applied, `yyyy` requires an era field and will fail to parse valid dates. The correct pattern for strict date validation is `dd/MM/uuuu`, not `dd/MM/yyyy`.

- **`ResolverStyle.STRICT` is mandatory for proper validation** — The default `ResolverStyle.SMART` silently adjusts impossible dates (e.g., February 30 becomes February 28). Only `STRICT` mode throws `DateTimeParseException` for invalid calendar dates, which is required to satisfy the user's requirement of handling invalid dates like `31/02/2020`.

- **`Period.between()` handles leap years natively** — Java's `Period.between(startDate, endDate)` correctly handles leap year boundaries. A person born on February 29 in a leap year will have their age computed correctly even when the current date falls in a non-leap year. `LocalDate` treats February 29 as February 28 in non-leap years when computing period differences.

- **`DateTimeParseException` is the primary exception** — This exception is thrown by `LocalDate.parse()` when the input string does not conform to the expected format or contains logically invalid date values. It should be caught via `try-catch` to display user-friendly error messages.

### 0.3.4 Fix Verification Analysis

**Steps to Reproduce the Bug:**
- Clone repository: the `src/` directory does not exist
- Run `javac src/*.java` — fails with "no such file or directory"
- Run `java Main` — fails with "could not find or load main class"

**Confirmation Tests for Fix:**
- After creating all Java source files, compile with: `javac -d out src/*.java`
- Run with valid input: `echo "15/06/1990" | java -cp out Main` — expect age output
- Run with invalid input `31/02/2020`: expect error message about invalid date
- Run with future date: expect error message about future date
- Run with leap year DOB `29/02/2000`: expect correct age calculation
- Run with malformed input `abc`: expect format error message

**Boundary Conditions and Edge Cases Covered:**
- DOB is today's date → `Your age is 0 years, 0 months, and 0 days.`
- DOB is yesterday → `Your age is 0 years, 0 months, and 1 days.`
- February 29 birth in leap year, evaluated in non-leap year
- End of month transitions (e.g., born January 31, current date February 28)
- Invalid months (13, 0), invalid days (32, 0)
- Non-numeric input, empty input
- Future date (tomorrow, next year)

**Verification Confidence Level:** 95% — The Java `java.time` API is well-tested and natively handles calendar edge cases. The combination of `ResolverStyle.STRICT` with `uuuu` pattern provides robust validation. The remaining 5% uncertainty accounts for any environment-specific date locale behavior.

## 0.4 Bug Fix Specification

### 0.4.1 The Definitive Fix

The fix requires creating four Java source files that together form a complete, OOP-compliant Age Calculator console application. Each class has a single, well-defined responsibility.

**Files to Create:**

| File Path | Responsibility | OOP Principle |
|-----------|---------------|---------------|
| `src/AgeResult.java` | Encapsulates age computation result (years, months, days) | Encapsulation, Single Responsibility |
| `src/DateValidator.java` | Validates date input format and logical correctness | Single Responsibility, Separation of Concerns |
| `src/AgeCalculator.java` | Core age computation using `Period.between()` | Single Responsibility, Open/Closed |
| `src/Main.java` | Application entry point, user I/O handling | Single Responsibility, Composition |

**This fixes the root cause by:** Implementing all missing Java classes required for DOB input, strict date validation, age computation, and result display — directly addressing the empty repository state.

### 0.4.2 Change Instructions

#### File 1: `src/AgeResult.java` — CREATE (New File)

**Purpose:** Model class encapsulating the age calculation result with years, months, and days. Follows encapsulation via private fields and public getter methods.

INSERT entire file with the following structure:
- Package-level class `AgeResult` with three `private final int` fields: `years`, `months`, `days`
- Constructor accepting years, months, days parameters
- Getter methods: `getYears()`, `getMonths()`, `getDays()`
- Override `toString()` returning the display format: `Your age is X years, Y months, and Z days.`
- Comment: `// Model class encapsulating the computed age breakdown`

Key implementation detail:
```java
public String toString() {
  return "Your age is " + years + " years, " + months + " months, and " + days + " days.";
}
```

#### File 2: `src/DateValidator.java` — CREATE (New File)

**Purpose:** Utility class responsible for parsing and validating date-of-birth strings in `DD/MM/YYYY` format using strict date resolution.

INSERT entire file with the following structure:
- A `private static final DateTimeFormatter` field configured with pattern `dd/MM/uuuu` and `ResolverStyle.STRICT` — this ensures invalid calendar dates (e.g., `31/02/2020`, `29/02/2019`) throw `DateTimeParseException` immediately
- A `public static LocalDate parseAndValidate(String dobString)` method that:
  - Parses the input string using the strict formatter
  - Checks if the parsed date is after `LocalDate.now()` (future date check)
  - Throws `IllegalArgumentException` with message `"Date of birth cannot be a future date."` if future
  - Throws `DateTimeParseException` for any invalid/malformed date input
  - Returns the valid `LocalDate` on success
- Comment: `// Uses ResolverStyle.STRICT with 'uuuu' (proleptic year) to reject invalid dates`

Critical implementation detail — the formatter MUST use `uuuu` not `yyyy`:
```java
private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu")
    .withResolverStyle(ResolverStyle.STRICT);
```

The reason `uuuu` is used instead of `yyyy`: In Java's `DateTimeFormatter`, `yyyy` means "year-of-era" (requires an era context like AD/BC). With `ResolverStyle.STRICT`, using `yyyy` fails on valid modern dates because no era field is present. `uuuu` means "proleptic year" and works correctly with `STRICT` mode.

#### File 3: `src/AgeCalculator.java` — CREATE (New File)

**Purpose:** Core computation class that calculates the exact age difference between a date of birth and the current system date using `java.time.Period`.

INSERT entire file with the following structure:
- A `public static AgeResult calculateAge(LocalDate dob)` method that:
  - Fetches the current system date via `LocalDate.now()`
  - Computes the period difference via `Period.between(dob, LocalDate.now())`
  - Extracts `getYears()`, `getMonths()`, `getDays()` from the `Period` object
  - Returns a new `AgeResult` object with the extracted values
- A `public static AgeResult calculateAge(LocalDate dob, LocalDate referenceDate)` overloaded method (for testability) accepting an explicit reference date
- Comment: `// Period.between() natively handles leap years and calendar edge cases`

Key implementation detail:
```java
Period period = Period.between(dob, currentDate);
return new AgeResult(period.getYears(), period.getMonths(), period.getDays());
```

#### File 4: `src/Main.java` — CREATE (New File)

**Purpose:** Application entry point that orchestrates user interaction through `Scanner`, delegates to `DateValidator` and `AgeCalculator`, and handles all exceptions with meaningful error messages.

INSERT entire file with the following structure:
- `import java.time.LocalDate`, `import java.time.Period`, `import java.time.format.DateTimeFormatter`, `import java.time.format.DateTimeParseException`, `import java.time.format.ResolverStyle`, `import java.util.Scanner`
- `public static void main(String[] args)` method that:
  - Creates a `Scanner` for `System.in`
  - Prompts: `"Enter your Date of Birth (DD/MM/YYYY): "`
  - Reads the user input string
  - Wraps the following in a `try-catch` block:
    - Calls `DateValidator.parseAndValidate(input)` to get a validated `LocalDate`
    - Calls `AgeCalculator.calculateAge(dob)` to get an `AgeResult`
    - Prints the result using `AgeResult.toString()`
  - Catches `DateTimeParseException` → prints `"Error: Invalid date format or impossible date. Please enter a valid date in DD/MM/YYYY format."`
  - Catches `IllegalArgumentException` → prints the exception message (future date)
  - Catches `Exception` → prints `"Error: An unexpected error occurred. "` + exception message
  - Closes the `Scanner` in a `finally` block

Key implementation flow:
```java
try {
  LocalDate dob = DateValidator.parseAndValidate(input);
  AgeResult result = AgeCalculator.calculateAge(dob);
  System.out.println(result);
} catch (DateTimeParseException e) { /* format/validity error */ }
```

### 0.4.3 Fix Validation

**Test command to verify fix:**
```
javac -d out src/AgeResult.java src/DateValidator.java src/AgeCalculator.java src/Main.java
```

**Expected compilation output:** Zero errors, zero warnings

**Runtime validation commands and expected outputs:**

| Test Case | Input | Expected Output |
|-----------|-------|-----------------|
| Normal DOB | `15/06/1990` | `Your age is X years, Y months, and Z days.` (computed from current date) |
| Leap year DOB | `29/02/2000` | Correct age with proper leap year handling |
| Invalid date (Feb 31) | `31/02/2020` | `Error: Invalid date format or impossible date...` |
| Invalid date (Feb 29 non-leap) | `29/02/2019` | `Error: Invalid date format or impossible date...` |
| Future date | `01/01/2030` | `Error: Date of birth cannot be a future date.` |
| Malformed input | `abc` | `Error: Invalid date format or impossible date...` |
| Today's date | Current date | `Your age is 0 years, 0 months, and 0 days.` |
| Empty input | (empty) | `Error: Invalid date format or impossible date...` |
| Wrong separator | `15-06-1990` | `Error: Invalid date format or impossible date...` |
| Month 13 | `15/13/1990` | `Error: Invalid date format or impossible date...` |
| Day 0 | `00/06/1990` | `Error: Invalid date format or impossible date...` |

**Confirmation method:** Compile all source files and run each test case via piped input:
```
echo "15/06/1990" | java -cp out Main
```

## 0.5 Scope Boundaries

### 0.5.1 Changes Required (Exhaustive List)

All changes are file creations since the repository is in a scaffold state with no existing source code.

| Action | File Path | Description |
|--------|-----------|-------------|
| **CREATE** | `src/AgeResult.java` | Model class encapsulating age result (years, months, days) with getters and `toString()` |
| **CREATE** | `src/DateValidator.java` | Validation utility using `DateTimeFormatter` with `ResolverStyle.STRICT` and `dd/MM/uuuu` pattern; future date rejection |
| **CREATE** | `src/AgeCalculator.java` | Core computation class using `Period.between()` for exact age calculation with leap year support |
| **CREATE** | `src/Main.java` | Entry point with `Scanner` input, `try-catch` exception handling, and user-facing error messages |
| **UNCHANGED** | `README.md` | No modifications to the existing README file |

**Total files:** 4 created, 0 modified, 0 deleted

### 0.5.2 Explicitly Excluded

- **Do not modify:** `README.md` — the existing project identifier file remains unchanged
- **Do not create:** Maven `pom.xml` or Gradle `build.gradle` — the user did not specify a build tool; the application is a simple console program compilable with `javac` directly
- **Do not create:** JUnit test files — the user's requirements focus on the application itself; test coverage is validated manually through the test cases in §0.4.3
- **Do not create:** REST API endpoints or Flask/Gunicorn infrastructure — while the tech spec describes a Python API service, the user explicitly requested a Java console application
- **Do not create:** MongoDB integration, Auth0 authentication, or AWS infrastructure — these are out of scope for the console application
- **Do not add:** Batch processing, multi-format date support beyond `DD/MM/YYYY`, or audit logging — these are enterprise features from the tech spec, not part of the user's console application requirements
- **Do not refactor:** No existing code to refactor — the repository is empty
- **Do not create:** GUI, web interface, or mobile interface — the user specified a console application
- **Do not create:** Configuration files, `.env` files, or property files — the application uses no external configuration

## 0.6 Verification Protocol

### 0.6.1 Bug Elimination Confirmation

**Compilation Verification:**
```
javac -d out src/AgeResult.java src/DateValidator.java src/AgeCalculator.java src/Main.java
```
- Verify output: zero compilation errors, zero warnings
- Confirm four `.class` files are generated in the `out/` directory

**Functional Verification — Valid Inputs:**

| Command | Expected Result |
|---------|-----------------|
| `echo "15/06/1990" \| java -cp out Main` | Prints `Your age is X years, Y months, and Z days.` with correct computed values |
| `echo "29/02/2000" \| java -cp out Main` | Prints correct age for leap year DOB (Feb 29, 2000) |
| `echo "01/01/2000" \| java -cp out Main` | Prints correct age for January 1, 2000 |
| `echo "31/12/1999" \| java -cp out Main` | Prints correct age for end-of-year DOB |

**Functional Verification — Invalid Inputs (Error Messages):**

| Command | Expected Error Output |
|---------|----------------------|
| `echo "31/02/2020" \| java -cp out Main` | Error message about invalid date format or impossible date |
| `echo "29/02/2019" \| java -cp out Main` | Error message (Feb 29 in non-leap year) |
| `echo "01/01/2030" \| java -cp out Main` | Error message about future date |
| `echo "abc" \| java -cp out Main` | Error message about invalid format |
| `echo "" \| java -cp out Main` | Error message about invalid format |
| `echo "00/06/1990" \| java -cp out Main` | Error message (day 0 is invalid) |
| `echo "15/13/1990" \| java -cp out Main` | Error message (month 13 is invalid) |

**Confirm error no longer appears:** After creating all source files, `javac` compilation succeeds and `java -cp out Main` executes without `ClassNotFoundException` or `NoSuchFileException`.

### 0.6.2 Regression Check

**Run existing test suite:** Not applicable — the repository had no existing tests or code. There is nothing to regress against.

**Verify unchanged behavior:** `README.md` remains unmodified with its original content (`# 05march_1-age`).

**Cross-validation of edge cases:**

| Scenario | Verification Method |
|----------|-------------------|
| Leap year correctness | Test DOB `29/02/2000` against known current date — `Period.between()` handles this natively |
| End-of-month transition | Test DOB `31/01/1990` — verify months/days are non-negative |
| Same-day birth | Test today's date as DOB — expect `0 years, 0 months, and 0 days` |
| New Year boundary | Test DOB `31/12/YYYY` crossing year boundary — verify year count is correct |
| Century boundary | Test DOB `01/01/1900` — verify large year spans compute correctly |
| `ResolverStyle.STRICT` enforcement | Test `31/04/2020` (April has 30 days) — must be rejected |

**Performance check:** Compilation should complete in under 5 seconds. Runtime for a single age calculation should be near-instantaneous (well under 50ms) as `Period.between()` is a pure arithmetic operation.

## 0.7 Rules

### 0.7.1 User-Specified Technical Requirements

The following rules are directly extracted from the user's prompt and must be followed exactly:

- **Use `java.time.LocalDate`** — All date representations must use `LocalDate` from the `java.time` package, never legacy `java.util.Date` or `java.util.Calendar`
- **Use `java.time.Period`** — Age computation must use `Period.between()` to derive years, months, and days, not manual arithmetic
- **Use `java.time.format.DateTimeFormatter`** — Date parsing must use `DateTimeFormatter` with the `DD/MM/YYYY` pattern (implemented as `dd/MM/uuuu` with `ResolverStyle.STRICT`)
- **Accept DOB in `DD/MM/YYYY` format** — The input format is fixed; the application does not need to support alternative formats
- **Calculate exact age in years, months, and days** — All three components must be displayed; partial results are not acceptable
- **Display format: `Your age is X years, Y months, and Z days.`** — This exact phrasing must be used in the output
- **DOB must not be a future date** — Explicit validation rejecting dates after `LocalDate.now()`
- **Handle invalid dates (e.g., `31/02/2020`)** — Logically impossible dates must produce error messages, not silently adjusted results
- **Display meaningful error messages** — Error output must help the user understand what went wrong and how to correct it
- **Follow Object-Oriented Programming principles** — Separate concerns into distinct classes with single responsibilities
- **Proper exception handling using `try-catch`** — All potential parse failures and validation errors must be caught and handled gracefully
- **Handle leap years correctly** — February 29 births must be supported; `Period.between()` handles this natively
- **Work for users born in any valid year** — No arbitrary lower-bound on birth year

### 0.7.2 Coding and Development Guidelines

- **Make only the specified changes** — Create only the four Java source files defined in §0.4.2; no additional features
- **Zero modifications outside the bug fix** — Do not alter `README.md` or create infrastructure beyond what is required
- **Java `java.time` API only** — Do not introduce third-party date libraries (Joda-Time, Apache Commons Lang, etc.)
- **Standard library only** — No external dependencies; the application must compile with `javac` alone
- **`ResolverStyle.STRICT` is mandatory** — The default `SMART` mode silently adjusts invalid dates; `STRICT` rejects them per user requirements
- **Use `uuuu` for year pattern** — In `DateTimeFormatter` with `ResolverStyle.STRICT`, `yyyy` (year-of-era) requires an era context and fails on valid dates; `uuuu` (proleptic year) works correctly
- **Immutable date objects** — `LocalDate` and `Period` are immutable and thread-safe by design; do not attempt to mutate them
- **Clean and readable coding standards** — Use meaningful variable names, include comments explaining key decisions (e.g., why `uuuu` instead of `yyyy`), follow standard Java naming conventions (camelCase for methods/variables, PascalCase for classes)
- **Close resources properly** — `Scanner` must be closed after use, ideally in a `finally` block or try-with-resources

## 0.8 References

### 0.8.1 Repository Files and Folders Searched

| Path | Type | Finding |
|------|------|---------|
| `""` (root) | Folder | Contains only `README.md`; no source code, build files, or configuration |
| `README.md` | File | Single line: `# 05march_1-age` — project identifier only |
| `.blitzyignore` | Search | Not found anywhere in the repository |
| `/tmp/environments_files/` | Directory | Empty — no user-provided environment files |

### 0.8.2 Technical Specification Sections Referenced

| Section | Key Information Extracted |
|---------|--------------------------|
| §1.1 Executive Summary | Project is greenfield; no source code committed; age computation focus |
| §1.2 System Overview | Current scaffold state confirmed; system capabilities defined |
| §1.3 Scope | In-scope features: age calculation, validation, error handling |
| §2.1 Feature Catalog | Seven features (F-001 through F-007) across four categories |
| §2.2 Functional Requirements | Detailed requirements for age calculation, validation, error handling |
| §3.1 Technology Stack Overview | Tech spec defines Python 3.13 / Flask stack (user overrides with Java) |
| §3.2 Programming Languages | Python 3.13.12 specified in tech spec; user requires Java instead |
| §3.3 Frameworks & Libraries | Flask 3.1.3, python-dateutil 2.9.0 in tech spec; Java `java.time` per user |
| §3.9 Technology Version Summary | Complete version matrix for tech spec components |
| §5.1 High-Level Architecture | API-only microservice architecture in tech spec |
| §6.1 Core Services Architecture | Single microservice with component details |
| §6.6 Testing Strategy | Comprehensive testing framework defined for Python/pytest |

### 0.8.3 Web Sources Referenced

| Source | URL | Key Finding |
|--------|-----|-------------|
| Baeldung — Calculate Age in Java | baeldung.com/java-get-age | `Period.between(birthDate, currentDate).getYears()` pattern; `LocalDate` and `Period` for age |
| HowToDoInJava — Strict Date Parsing | howtodoinjava.com/java/date-time/resolverstyle-strict-date-parsing/ | Use `uuuu` with `ResolverStyle.STRICT`; `yyyy` means year-of-era, not proleptic year |
| Baeldung — Check Valid Date | baeldung.com/java-string-valid-date | `DateTimeFormatter` with `ResolverStyle.STRICT` rejects Feb 30, Feb 29 in non-leap years |
| Mkyong — Check Valid Date | mkyong.com/java/how-to-check-if-date-is-valid-in-java/ | `uuuu-M-d` pattern with STRICT mode for comprehensive validation |
| Joda-Time Issue #295 | github.com/JodaOrg/joda-time/issues/295 | Java `java.time` correctly returns 3y 11m 30d for leap year period (Feb 29 → Feb 28 test) |
| Medium — Period.between() | medium.com/@AlexanderObregon | `Period.between()` calculates years, months, days; useful for age determination |
| JavaSpring.net — Age Calculation | javaspring.net/blog/how-do-i-calculate-someone-s-age-in-java/ | Leap day handling: `LocalDate` treats Feb 29 as Feb 28 in non-leap years |
| LabEx — DateTimeParseException | labex.io/tutorials/java-how-to-handle-java-time-format-datetimeparseexception-417320 | Invalid calendar dates (Feb 30, Sep 31) cause `DateTimeParseException` |
| Spring Framework Issue #18143 | github.com/spring-projects/spring-framework/issues/18143 | Confirms `yyyy` vs `uuuu` difference in STRICT mode; `uuuu` recommended for year pattern |

### 0.8.4 Attachments

No attachments were provided for this project. No Figma screens, design mockups, or supplementary documents were included.

### 0.8.5 Environment Details

| Attribute | Value |
|-----------|-------|
| Java Runtime | OpenJDK 21.0.10 (2026-01-20) |
| Compiler | javac 21.0.10 |
| Operating System | Ubuntu (Noble) |
| Repository | github.com/shaliniblitzy/05march_1-age |
| Repository State | Scaffold — single `README.md` file |
| User-Provided Setup Instructions | None provided |
| Environment Variables | None provided |
| Secrets | None provided |

