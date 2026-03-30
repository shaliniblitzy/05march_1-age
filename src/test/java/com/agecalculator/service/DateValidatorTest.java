package com.agecalculator.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import com.agecalculator.exception.InvalidDateException;

/**
 * Unit tests for {@link DateValidator} — the date validation service.
 *
 * <p>Tests the {@link DateValidator#parseAndValidate(String)} method with various
 * input scenarios including valid formats, invalid formats, impossible calendar dates,
 * future dates, boundary conditions, and empty input.</p>
 *
 * <p>The production method enforces three layers of validation:</p>
 * <ol>
 *     <li>Blank/null rejection — rejects empty or whitespace-only input</li>
 *     <li>Format and calendar validation — parses DD/MM/YYYY with strict resolver,
 *         distinguishing format errors from impossible calendar dates</li>
 *     <li>Temporal validation — rejects future dates</li>
 * </ol>
 *
 * @author Age Calculator Application
 * @version 1.0.0
 * @see DateValidator
 * @see InvalidDateException
 */
class DateValidatorTest {

    // ========================================================================
    // Valid Date Parsing Tests
    // ========================================================================

    /**
     * Tests that a valid date string in DD/MM/YYYY format is parsed correctly
     * into the corresponding {@link LocalDate}. Uses the user-specified example
     * date of 15/08/1998 from the AAP.
     */
    @Test
    @DisplayName("Valid date '15/08/1998' is parsed successfully")
    void testValidDateFormat() throws InvalidDateException {
        LocalDate result = DateValidator.parseAndValidate("15/08/1998");

        assertNotNull(result, "Parsed date should not be null");
        assertEquals(LocalDate.of(1998, 8, 15), result, "Parsed date should be 1998-08-15");
    }

    /**
     * Tests that New Year's Day 2000 is parsed correctly, verifying
     * boundary handling for day-of-month = 1 and month = 1.
     */
    @Test
    @DisplayName("Valid date '01/01/2000' is parsed successfully")
    void testValidDateNewYear() throws InvalidDateException {
        LocalDate result = DateValidator.parseAndValidate("01/01/2000");

        assertNotNull(result, "Parsed date should not be null");
        assertEquals(LocalDate.of(2000, 1, 1), result, "Parsed date should be 2000-01-01");
    }

    // ========================================================================
    // Malformed Input Tests
    // ========================================================================

    /**
     * Tests that completely malformed input ("abc") is rejected with the
     * format error message. This is a mandatory test case per AAP §0.5.3.
     */
    @Test
    @DisplayName("Malformed input 'abc' throws InvalidDateException")
    void testMalformedInput() {
        InvalidDateException exception = assertThrows(InvalidDateException.class,
                () -> DateValidator.parseAndValidate("abc"),
                "Malformed input should throw InvalidDateException");

        assertEquals("Invalid date format. Please use DD/MM/YYYY.", exception.getMessage(),
                "Error message should indicate invalid format");
    }

    /**
     * Parameterized test that verifies multiple invalid format inputs all produce
     * the same format error message. Each input either does not match the
     * DD/MM/YYYY structural pattern or contains extraneous characters.
     *
     * <p>Tested inputs:</p>
     * <ul>
     *     <li>"abc" — completely non-date text</li>
     *     <li>"2020/08/15" — YYYY/MM/DD format (wrong order)</li>
     *     <li>"15-08-1998" — correct order but uses hyphens instead of slashes</li>
     *     <li>"1998-08-15" — ISO format (wrong order and delimiter)</li>
     *     <li>"not-a-date" — arbitrary text</li>
     *     <li>"15/8/1998x" — single-digit month and trailing character</li>
     * </ul>
     *
     * @param input the invalid format string to test
     */
    @ParameterizedTest
    @ValueSource(strings = {"abc", "2020/08/15", "15-08-1998", "1998-08-15", "not-a-date", "15/8/1998x"})
    @DisplayName("Invalid format inputs throw InvalidDateException")
    void testInvalidFormatInputs(String input) {
        InvalidDateException exception = assertThrows(InvalidDateException.class,
                () -> DateValidator.parseAndValidate(input),
                "Input '" + input + "' should throw InvalidDateException");

        assertEquals("Invalid date format. Please use DD/MM/YYYY.", exception.getMessage(),
                "Error message should indicate invalid format for input: " + input);
    }

    // ========================================================================
    // Impossible Calendar Date Tests
    // ========================================================================

    /**
     * Tests that an impossible calendar date (February 31) is rejected.
     * The input structurally matches DD/MM/YYYY (two-digit day, two-digit month,
     * four-digit year) but represents a date that does not exist on the calendar.
     * The production code distinguishes this from a pure format error by using a
     * regex pre-check, producing a calendar-specific error message.
     *
     * <p>This is a mandatory test case per AAP §0.5.3.</p>
     */
    @Test
    @DisplayName("Impossible date '31/02/2020' (Feb 31) throws InvalidDateException")
    void testImpossibleCalendarDate() {
        InvalidDateException exception = assertThrows(InvalidDateException.class,
                () -> DateValidator.parseAndValidate("31/02/2020"),
                "February 31 should throw InvalidDateException");

        assertEquals("Invalid date. Please enter a real calendar date.", exception.getMessage(),
                "Error message should indicate an impossible calendar date");
    }

    // ========================================================================
    // Future Date Tests
    // ========================================================================

    /**
     * Tests that a future date (one year from now) is rejected with the
     * future-date error message. The future date is dynamically computed
     * to prevent the test from becoming stale over time.
     *
     * <p>This is a mandatory test case per AAP §0.5.3.</p>
     */
    @Test
    @DisplayName("Future date throws InvalidDateException with 'cannot be a future date' message")
    void testFutureDateRejected() {
        LocalDate futureDate = LocalDate.now().plusYears(1);
        String futureDateStr = String.format("%02d/%02d/%04d",
                futureDate.getDayOfMonth(), futureDate.getMonthValue(), futureDate.getYear());

        InvalidDateException exception = assertThrows(InvalidDateException.class,
                () -> DateValidator.parseAndValidate(futureDateStr),
                "Future date should throw InvalidDateException");

        assertEquals("Date of Birth cannot be a future date.", exception.getMessage(),
                "Error message should indicate future date rejection");
    }

    // ========================================================================
    // Boundary Condition Tests
    // ========================================================================

    /**
     * Tests that today's date is accepted as a valid Date of Birth.
     * This is the boundary case: {@code dob.isAfter(LocalDate.now())} returns
     * {@code false} for today's date, so it should be accepted. The date string
     * is dynamically computed to remain valid regardless of when the test runs.
     */
    @Test
    @DisplayName("Today's date is accepted as valid DOB (boundary case)")
    void testTodayIsAccepted() throws InvalidDateException {
        LocalDate today = LocalDate.now();
        String todayStr = String.format("%02d/%02d/%04d",
                today.getDayOfMonth(), today.getMonthValue(), today.getYear());

        LocalDate result = DateValidator.parseAndValidate(todayStr);

        assertNotNull(result, "Today's date should be accepted as valid DOB");
        assertEquals(today, result, "Parsed date should equal today");
    }

    // ========================================================================
    // Empty and Null Input Tests
    // ========================================================================

    /**
     * Tests that an empty string input is rejected with the empty-input error
     * message. The production code checks for blank input before attempting any
     * date parsing, producing a specific error message for this case.
     */
    @Test
    @DisplayName("Empty input throws InvalidDateException")
    void testEmptyInput() {
        InvalidDateException exception = assertThrows(InvalidDateException.class,
                () -> DateValidator.parseAndValidate(""),
                "Empty input should throw InvalidDateException");

        assertEquals("Date of Birth input cannot be empty.", exception.getMessage(),
                "Error message should indicate empty input");
    }

    // ========================================================================
    // Leap Year Edge Case Tests
    // ========================================================================

    /**
     * Tests that February 29 in a leap year (2000) is parsed successfully.
     * The year 2000 is a leap year (divisible by 400), so February 29 is a
     * valid calendar date.
     */
    @Test
    @DisplayName("Leap year date '29/02/2000' is parsed successfully")
    void testLeapYearValidDate() throws InvalidDateException {
        LocalDate result = DateValidator.parseAndValidate("29/02/2000");

        assertNotNull(result, "Leap year date should be parsed successfully");
        assertEquals(LocalDate.of(2000, 2, 29), result,
                "Parsed date should be 2000-02-29 for leap year");
    }

    /**
     * Tests that February 29 in a non-leap year (2001) is rejected.
     * The year 2001 is not a leap year, so February 29 does not exist.
     * The input structurally matches DD/MM/YYYY, so the production code's
     * regex pre-check identifies this as an impossible calendar date rather
     * than a format error.
     */
    @Test
    @DisplayName("Non-leap year date '29/02/2001' (Feb 29 in non-leap year) throws InvalidDateException")
    void testNonLeapYearFeb29() {
        InvalidDateException exception = assertThrows(InvalidDateException.class,
                () -> DateValidator.parseAndValidate("29/02/2001"),
                "Feb 29 in non-leap year should throw InvalidDateException");

        assertEquals("Invalid date. Please enter a real calendar date.", exception.getMessage(),
                "Error message should indicate an impossible calendar date for non-leap year Feb 29");
    }
}
