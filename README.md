# Age Calculator

A Java console application that calculates a user's exact age based on their Date of Birth (DOB). The application decomposes the computed age into three precise components — years, months, and days — using the `java.time` API (`LocalDate`, `Period`, and `DateTimeFormatter`) for accurate, leap-year-aware date arithmetic.

## Prerequisites

- **Java 21** (OpenJDK 21 or later)
- **Maven 3.8+** (for building and testing)

## Build

Compile the source code, run all tests, and produce an executable JAR:

```bash
mvn clean package
```

The packaged JAR is written to `target/age-calculator-1.0.0.jar`.

## Run

Launch the application from the command line:

```bash
java -jar target/age-calculator-1.0.0.jar
```

The application will prompt you to enter your Date of Birth in `DD/MM/YYYY` format.

## Usage

### Successful Calculation

```
Enter your Date of Birth (DD/MM/YYYY): 15/03/1990
Your age is 35 years, 11 months, and 18 days.
```

### Error Handling

**Future date:**

```
Enter your Date of Birth (DD/MM/YYYY): 25/12/2030
Error: Date of birth cannot be a future date.
```

**Invalid format:**

```
Enter your Date of Birth (DD/MM/YYYY): hello
Error: Invalid date format. Please use DD/MM/YYYY format.
```

**Impossible date:**

```
Enter your Date of Birth (DD/MM/YYYY): 31/02/2020
Error: Invalid date. Please enter a valid calendar date.
```

## Testing

Run the full test suite with Maven:

```bash
mvn test
```

Test coverage includes age computation accuracy, date validation (future dates, invalid formats, impossible calendar dates), date parsing with strict resolution, and model formatting verification.

## Project Structure

```
├── pom.xml
├── README.md
└── src/
    ├── main/java/com/agecalculator/
    │   ├── AgeCalculatorApp.java
    │   ├── model/
    │   │   └── AgeResult.java
    │   ├── service/
    │   │   └── AgeCalculatorService.java
    │   ├── validator/
    │   │   └── DateValidator.java
    │   ├── util/
    │   │   └── DateParserUtil.java
    │   └── exception/
    │       ├── InvalidDateException.java
    │       └── FutureDateException.java
    └── test/java/com/agecalculator/
        ├── service/
        │   └── AgeCalculatorServiceTest.java
        ├── validator/
        │   └── DateValidatorTest.java
        ├── util/
        │   └── DateParserUtilTest.java
        └── model/
            └── AgeResultTest.java
```

## Technology Stack

- **Java 21** — application runtime
- **Maven** — build tool and dependency management
- **JUnit Jupiter 5.11.4** — unit testing framework
- **java.time API** — `LocalDate`, `Period`, and `DateTimeFormatter` for date handling
