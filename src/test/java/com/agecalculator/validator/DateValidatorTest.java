package com.agecalculator.validator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.agecalculator.exception.FutureDateException;
import com.agecalculator.exception.InvalidDateException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Comprehensive JUnit Jupiter unit tests for {@link DateValidator}.
 *
 * <p>This test class validates the behavior of the {@code DateValidator.validate(String dateStr)}
 * method across all specified scenarios:</p>
 * <ul>
 *   <li><strong>Happy path</strong> — valid DD/MM/YYYY dates return the correct {@link LocalDate}</li>
 *   <li><strong>Boundary conditions</strong> — today's date (age 0) and yesterday are accepted</li>
 *   <li><strong>Future date rejection</strong> — tomorrow, far future dates throw {@link FutureDateException}</li>
 *   <li><strong>Invalid format rejection</strong> — ISO format, alphabetic input, empty/whitespace/null throw {@link InvalidDateException}</li>
 *   <li><strong>Impossible date rejection</strong> — February 30/31, April 31, non-leap Feb 29 throw {@link InvalidDateException}</li>
 *   <li><strong>Leap year correctness</strong> — Feb 29 on a valid leap year is accepted</li>
 * </ul>
 *
 * <p>{@code DateValidator} is stateless (default no-arg constructor, no mutable fields),
 * so a single instance is shared across all test methods safely.</p>
 *
 * @see DateValidator
 * @see InvalidDateException
 * @see FutureDateException
 */
public class DateValidatorTest {

    /**
     * Shared formatter used to convert dynamically generated {@link LocalDate} objects
     * (today, yesterday, tomorrow, far future) into DD/MM/YYYY formatted strings for test input.
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Stateless validator instance reused across all test methods.
     * {@link DateValidator} has a default no-arg constructor and holds no mutable state.
     */
    private final DateValidator validator = new DateValidator();

    // ========================================================================================
    // Phase 2: Valid Date Tests — Happy Path
    // ========================================================================================

    @Test
    @DisplayName("validate() returns correct LocalDate for a valid DD/MM/YYYY date")
    void testValidateValidDateReturnsCorrectLocalDate() throws InvalidDateException, FutureDateException {
        LocalDate result = validator.validate("15/03/1990");

        assertEquals(LocalDate.of(1990, 3, 15), result);
    }

    @Test
    @DisplayName("validate() returns a non-null LocalDate for valid input")
    void testValidateValidDateReturnsNonNull() throws InvalidDateException, FutureDateException {
        LocalDate result = validator.validate("01/01/2000");

        assertNotNull(result);
    }

    @Test
    @DisplayName("validate() accepts today's date as valid (age 0 scenario)")
    void testValidateTodaysDateIsValid() throws InvalidDateException, FutureDateException {
        String today = LocalDate.now().format(FORMATTER);

        LocalDate result = validator.validate(today);

        assertEquals(LocalDate.now(), result);
    }

    @Test
    @DisplayName("validate() accepts yesterday's date as valid")
    void testValidateYesterdaysDateIsValid() {
        String yesterday = LocalDate.now().minusDays(1).format(FORMATTER);

        assertDoesNotThrow(() -> validator.validate(yesterday));
    }

    @Test
    @DisplayName("validate() accepts historical date from year 1900")
    void testValidateHistoricalDate() throws InvalidDateException, FutureDateException {
        LocalDate result = validator.validate("01/01/1900");

        assertEquals(LocalDate.of(1900, 1, 1), result);
    }

    // ========================================================================================
    // Phase 3: Future Date Tests — FutureDateException
    // ========================================================================================

    @Test
    @DisplayName("validate() throws FutureDateException for a future date")
    void testValidateFutureDateThrowsFutureDateException() {
        String futureDate = LocalDate.now().plusDays(1).format(FORMATTER);

        assertThrows(FutureDateException.class, () -> validator.validate(futureDate));
    }

    @Test
    @DisplayName("validate() throws FutureDateException for a date far in the future")
    void testValidateFarFutureDateThrowsFutureDateException() {
        String farFuture = LocalDate.now().plusYears(10).format(FORMATTER);

        assertThrows(FutureDateException.class, () -> validator.validate(farFuture));
    }

    @Test
    @DisplayName("validate() throws FutureDateException for tomorrow's date")
    void testValidateTomorrowThrowsFutureDateException() {
        String tomorrow = LocalDate.now().plusDays(1).format(FORMATTER);

        assertThrows(FutureDateException.class, () -> validator.validate(tomorrow));
    }

    // ========================================================================================
    // Phase 4: Invalid Format Tests — InvalidDateException
    // ========================================================================================

    @Test
    @DisplayName("validate() throws InvalidDateException for ISO format YYYY-MM-DD")
    void testValidateIsoFormatThrowsInvalidDateException() {
        assertThrows(InvalidDateException.class, () -> validator.validate("2020-03-15"));
    }

    @ParameterizedTest
    @DisplayName("validate() throws InvalidDateException for various invalid formats")
    @ValueSource(strings = {
            "2020-03-15",
            "hello",
            "abc/de/fghi",
            "March 15, 2020",
            "15-03-2020",
            "15.03.2020"
    })
    void testValidateVariousInvalidFormatsThrowException(String invalidInput) {
        assertThrows(InvalidDateException.class, () -> validator.validate(invalidInput));
    }

    @Test
    @DisplayName("validate() throws InvalidDateException for empty string")
    void testValidateEmptyStringThrowsInvalidDateException() {
        assertThrows(InvalidDateException.class, () -> validator.validate(""));
    }

    @Test
    @DisplayName("validate() throws InvalidDateException for whitespace-only string")
    void testValidateWhitespaceStringThrowsInvalidDateException() {
        assertThrows(InvalidDateException.class, () -> validator.validate("   "));
    }

    @Test
    @DisplayName("validate() throws InvalidDateException for null input")
    void testValidateNullInputThrowsException() {
        assertThrows(InvalidDateException.class, () -> validator.validate(null));
    }

    // ========================================================================================
    // Phase 5: Impossible Date Tests — InvalidDateException (Strict Resolution)
    // ========================================================================================

    @Test
    @DisplayName("validate() throws InvalidDateException for impossible date 31/02/2020")
    void testValidateImpossibleDateFeb31ThrowsInvalidDateException() {
        assertThrows(InvalidDateException.class, () -> validator.validate("31/02/2020"));
    }

    @Test
    @DisplayName("validate() throws InvalidDateException for impossible date 30/02/2020")
    void testValidateImpossibleDateFeb30ThrowsInvalidDateException() {
        assertThrows(InvalidDateException.class, () -> validator.validate("30/02/2020"));
    }

    @Test
    @DisplayName("validate() throws InvalidDateException for impossible date 31/04/2020")
    void testValidateImpossibleDateApril31ThrowsInvalidDateException() {
        assertThrows(InvalidDateException.class, () -> validator.validate("31/04/2020"));
    }

    @Test
    @DisplayName("validate() throws InvalidDateException for Feb 29 on non-leap year 2023")
    void testValidateFeb29NonLeapYearThrowsInvalidDateException() {
        assertThrows(InvalidDateException.class, () -> validator.validate("29/02/2023"));
    }

    @Test
    @DisplayName("validate() accepts February 29 on a valid leap year (2000)")
    void testValidateFeb29OnLeapYearIsValid() throws InvalidDateException, FutureDateException {
        LocalDate result = validator.validate("29/02/2000");

        assertEquals(LocalDate.of(2000, 2, 29), result);
    }
}
