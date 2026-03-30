# 05march_1-age — Age Calculator

A Java console application that calculates a user's exact age from their Date of Birth (DOB), broken down into **years**, **months**, and **days**.

Built with the Java `java.time` standard library APIs:

- **`java.time.LocalDate`** — for date representation and current-date resolution
- **`java.time.Period`** — for calendar-aware arithmetic producing years, months, and days
- **`java.time.format.DateTimeFormatter`** — for strict parsing of user input in `DD/MM/YYYY` format

The application follows Object-Oriented Programming principles with clear separation of concerns: dedicated classes for input handling, validation, calculation, formatting, and utility operations.

---

## Prerequisites

| Requirement | Version | Notes |
|-------------|---------|-------|
| **JDK** | 21 (OpenJDK 21.0.10 or compatible) | Java 21 is the current Long-Term Support (LTS) release |
| **Apache Maven** | 3.8+ (verified with 3.8.7) | Used for build, test, and packaging |

### Environment Variable

Ensure `JAVA_HOME` points to your JDK 21 installation:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
```

Verify your setup:

```bash
java -version
# Expected: openjdk version "21.0.10" ...

mvn --version
# Expected: Apache Maven 3.8.7 ...
```

---

## Project Structure

The project follows the standard Maven directory layout:

```
05march_1-age/
├── pom.xml                                          (Maven project descriptor)
├── README.md                                        (This file)
├── .gitignore                                       (Git exclusion rules)
└── src/
    ├── main/
    │   └── java/
    │       └── com/agecalculator/
    │           ├── AgeCalculatorApp.java             (Main entry point)
    │           ├── model/
    │           │   └── AgeResult.java                (Immutable data model)
    │           ├── service/
    │           │   ├── AgeCalculator.java            (Calculation engine)
    │           │   └── DateValidator.java            (Validation service)
    │           ├── io/
    │           │   ├── DateInput.java                (Console input handler)
    │           │   └── ResultFormatter.java          (Output formatter)
    │           ├── util/
    │           │   └── AgeUtils.java                 (Utility methods)
    │           └── exception/
    │               └── InvalidDateException.java     (Custom exception)
    └── test/
        └── java/
            └── com/agecalculator/
                ├── AgeCalculatorAppTest.java         (Integration test)
                ├── model/
                │   └── AgeResultTest.java            (Model unit tests)
                ├── service/
                │   ├── AgeCalculatorTest.java        (Calculation unit tests)
                │   └── DateValidatorTest.java        (Validation unit tests)
                ├── io/
                │   └── ResultFormatterTest.java      (Formatter unit tests)
                └── util/
                    └── AgeUtilsTest.java             (Utility unit tests)
```

### Module Overview

| Package | Class | Responsibility |
|---------|-------|---------------|
| `com.agecalculator` | `AgeCalculatorApp` | Main entry point — orchestrates input → validation → calculation → output |
| `com.agecalculator.model` | `AgeResult` | Immutable data object holding computed years, months, and days |
| `com.agecalculator.service` | `AgeCalculator` | Computes age using `Period.between(dob, today)` |
| `com.agecalculator.service` | `DateValidator` | Parses and validates DOB input with strict calendar rules |
| `com.agecalculator.io` | `DateInput` | Reads DOB string from console via `Scanner` |
| `com.agecalculator.io` | `ResultFormatter` | Formats `AgeResult` into the display string |
| `com.agecalculator.util` | `AgeUtils` | Static utility methods for total months, total days, and next-birthday countdown |
| `com.agecalculator.exception` | `InvalidDateException` | Custom exception for validation failures |

---

## Build Instructions

All commands should be run from the project root directory.

### Compile

```bash
mvn compile
```

### Run Tests

```bash
mvn test
```

### Package as Executable JAR

```bash
mvn package
```

This produces `target/age-calculator-1.0.0-SNAPSHOT.jar` with the `Main-Class` manifest entry set to `com.agecalculator.AgeCalculatorApp`.

### Run the Application

```bash
java -jar target/age-calculator-1.0.0-SNAPSHOT.jar
```

### Clean Build

```bash
mvn clean package
```

---

## Usage Examples

### Standard Age Calculation

```plaintext
Enter your Date of Birth (DD/MM/YYYY): 15/08/1998
Your age is 27 years, 6 months, and 15 days.
```

### Additional Information (Optional Enhancements)

The application also displays supplementary age details:

```plaintext
Enter your Date of Birth (DD/MM/YYYY): 15/08/1998
Your age is 27 years, 6 months, and 15 days.

Additional Details:
  Total months: 330
  Total days: 10,090
  Days until next birthday: 138
```

> **Note:** The exact numbers in the examples above depend on the current date when the application is run.

### Error Handling Examples

**Invalid date format:**

```plaintext
Enter your Date of Birth (DD/MM/YYYY): 1998-08-15
Error: Invalid date format. Please use DD/MM/YYYY.
```

**Impossible calendar date:**

```plaintext
Enter your Date of Birth (DD/MM/YYYY): 31/02/2020
Error: Invalid date. Please enter a real calendar date.
```

**Future date:**

```plaintext
Enter your Date of Birth (DD/MM/YYYY): 25/12/2099
Error: Date of Birth cannot be a future date.
```

---

## Input Validation

The application enforces a strict four-layer validation pipeline:

### Accepted Format

- **Pattern:** `DD/MM/YYYY` (day/month/year separated by forward slashes)
- **Examples:** `15/08/1998`, `01/01/2000`, `29/02/2000`
- The parser uses `DateTimeFormatter.ofPattern("dd/MM/uuuu")` with `ResolverStyle.STRICT`

### Rejection Rules

| Rule | Example Input | Error Message |
|------|--------------|---------------|
| **Malformed format** | `abc`, `1998-08-15`, `15-08-1998` | `Invalid date format. Please use DD/MM/YYYY.` |
| **Impossible calendar date** | `31/02/2020`, `32/01/2000`, `00/05/1990` | `Invalid date. Please enter a real calendar date.` |
| **Future date** | Any date after today | `Date of Birth cannot be a future date.` |
| **Empty or blank input** | ` `, `""` | `Invalid date format. Please use DD/MM/YYYY.` |

### Leap Year Handling

- `29/02/2000` — **Accepted** (2000 is a leap year)
- `29/02/1900` — **Rejected** (1900 is not a leap year; divisible by 100 but not 400)
- `29/02/2024` — **Accepted** (2024 is a leap year)

---

## Test Cases

The test suite is executed via JUnit 5 Jupiter and covers the following scenarios:

### Mandatory Test Scenarios

| # | Scenario | Input | Expected Outcome |
|---|----------|-------|-----------------|
| 1 | **Normal DOB** | `15/08/1998` | Age computed as non-zero years, months, and days |
| 2 | **Leap year DOB** | `29/02/2000` | Correct age calculation for Feb 29 birth date |
| 3 | **Invalid date** | `31/02/2020` | `InvalidDateException` — impossible calendar date |
| 4 | **Future date** | Tomorrow's date | `InvalidDateException` — future date rejected |
| 5 | **Wrong format** | `abc`, `1998-08-15` | `InvalidDateException` — malformed input |

### Additional Test Coverage

| Scenario | Description |
|----------|-------------|
| **Same-day DOB** | DOB equals today — age is 0 years, 0 months, 0 days |
| **Yesterday DOB** | DOB is yesterday — age is 0 years, 0 months, 1 day |
| **Centenarian** | DOB from 100+ years ago — verifies large year values |
| **Year boundary** | DOB on Dec 31 tested against Jan 1 — verifies year rollover |
| **Total months** | Utility method verified against known arithmetic |
| **Total days** | Utility method verified against known arithmetic |
| **Next birthday countdown** | Days-until-next-birthday for various DOBs including Feb 29 |
| **Output formatting** | Exact string format verification: `Your age is X years, Y months, and Z days.` |
| **Zero-component edge case** | Age with exactly 0 months or 0 days renders correctly |
| **End-to-end integration** | Simulated console input → captured `System.out` output verification |

### Running Tests

```bash
# Run all tests
mvn test

# Run a specific test class
mvn test -Dtest=com.agecalculator.service.AgeCalculatorTest

# Run with verbose output
mvn test -X
```

---

## Technology Stack

| Component | Technology | Version | Purpose |
|-----------|-----------|---------|---------|
| **Language** | Java (OpenJDK) | 21 (LTS) | Core application language |
| **Build Tool** | Apache Maven | 3.8+ | Dependency management, compilation, testing, packaging |
| **Testing** | JUnit 5 Jupiter | 5.11.4 | Unit and integration testing framework |
| **Date API** | `java.time` | JDK 21 built-in | `LocalDate`, `Period`, `DateTimeFormatter`, `ChronoUnit` |
| **I/O** | `java.util.Scanner` | JDK 21 built-in | Console input reading |

### Build Plugins

| Plugin | Version | Purpose |
|--------|---------|---------|
| `maven-compiler-plugin` | 3.13.0 | Compiles Java source with `--release 21` |
| `maven-surefire-plugin` | 3.5.5 | Discovers and runs JUnit 5 tests |
| `maven-jar-plugin` | 3.4.2 | Packages executable JAR with `Main-Class` manifest |

### Key Design Decisions

- **No external runtime dependencies** — the application runs entirely on the Java Standard Library
- **`ResolverStyle.STRICT`** with `"dd/MM/uuuu"` pattern — ensures impossible dates (e.g., Feb 31) are rejected at parse time
- **`Period.between()`** — provides calendar-aware arithmetic that correctly handles varying month lengths and leap years
- **Immutable `AgeResult` model** — thread-safe data transfer object with `final` fields and no setters
- **Custom `InvalidDateException`** — enables structured error propagation from validation layer to application layer

---

## License

This project is part of the `05march_1-age` repository.
