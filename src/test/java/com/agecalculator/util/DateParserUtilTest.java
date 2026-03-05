package com.agecalculator.util;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import com.agecalculator.exception.InvalidDateException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link DateParserUtil}, the static utility class responsible
 * for converting user-input date strings in {@code DD/MM/YYYY} format into
 * {@link LocalDate} objects using strict date resolution.
 *
 * <p>Test categories covered:</p>
 * <ul>
 *   <li><strong>Happy Path</strong> — valid date strings parse to correct {@link LocalDate} values</li>
 *   <li><strong>Leap Year Edge Cases</strong> — February 29 on leap/non-leap years including century boundaries</li>
 *   <li><strong>Invalid Format</strong> — ISO format, dashes, dots, alphabetic garbage, single-digit fields</li>
 *   <li><strong>Null and Empty Input</strong> — null, empty string, whitespace-only strings</li>
 *   <li><strong>Impossible Dates</strong> — February 31, April 31, day zero, month 13, month zero</li>
 *   <li><strong>Return Type Validation</strong> — parsed result is non-null</li>
 * </ul>
 *
 * @see DateParserUtil#parse(String)
 * @see InvalidDateException
 */
public class DateParserUtilTest {

    // ========================================================================
    // Phase 2: Valid Date Parsing Tests — Happy Path
    // ========================================================================

    @Test
    @DisplayName("parse() converts valid DD/MM/YYYY string to correct LocalDate")
    void testParseValidDateReturnsCorrectLocalDate() throws InvalidDateException {
        LocalDate result = DateParserUtil.parse("15/03/2000");
        assertEquals(LocalDate.of(2000, 3, 15), result);
    }

    @ParameterizedTest
    @DisplayName("parse() correctly parses various valid DD/MM/YYYY dates")
    @CsvSource({
        "01/01/2000, 2000, 1, 1",
        "31/12/1999, 1999, 12, 31",
        "28/02/2023, 2023, 2, 28",
        "15/06/1985, 1985, 6, 15",
        "01/01/1900, 1900, 1, 1"
    })
    void testParseMultipleValidDates(String input, int year, int month, int day)
            throws InvalidDateException {
        LocalDate result = DateParserUtil.parse(input);
        assertEquals(LocalDate.of(year, month, day), result);
    }

    @Test
    @DisplayName("parse() correctly parses first day of a year")
    void testParseFirstDayOfYear() throws InvalidDateException {
        LocalDate result = DateParserUtil.parse("01/01/2024");
        assertEquals(LocalDate.of(2024, 1, 1), result);
    }

    @Test
    @DisplayName("parse() correctly parses last day of a year")
    void testParseLastDayOfYear() throws InvalidDateException {
        LocalDate result = DateParserUtil.parse("31/12/2024");
        assertEquals(LocalDate.of(2024, 12, 31), result);
    }

    // ========================================================================
    // Phase 3: Leap Year Tests — Critical Edge Cases
    // ========================================================================

    @Test
    @DisplayName("parse() accepts February 29 on a leap year (2024)")
    void testParseFeb29OnLeapYearSucceeds() throws InvalidDateException {
        LocalDate result = assertDoesNotThrow(() -> DateParserUtil.parse("29/02/2024"));
        assertEquals(LocalDate.of(2024, 2, 29), result);
    }

    @Test
    @DisplayName("parse() throws InvalidDateException for February 29 on non-leap year (2023)")
    void testParseFeb29OnNonLeapYearThrowsException() {
        assertThrows(InvalidDateException.class,
                () -> DateParserUtil.parse("29/02/2023"));
    }

    @Test
    @DisplayName("parse() throws InvalidDateException for Feb 29 on century non-leap year (1900)")
    void testParseFeb29OnCenturyNonLeapYearThrowsException() {
        assertThrows(InvalidDateException.class,
                () -> DateParserUtil.parse("29/02/1900"));
    }

    @Test
    @DisplayName("parse() accepts February 29 on century leap year (2000)")
    void testParseFeb29OnCenturyLeapYearSucceeds() throws InvalidDateException {
        LocalDate result = DateParserUtil.parse("29/02/2000");
        assertEquals(LocalDate.of(2000, 2, 29), result);
    }

    // ========================================================================
    // Phase 4: Invalid Format Tests
    // ========================================================================

    @Test
    @DisplayName("parse() throws InvalidDateException for ISO format YYYY-MM-DD")
    void testParseInvalidFormatIsoThrowsException() {
        assertThrows(InvalidDateException.class,
                () -> DateParserUtil.parse("2020-03-15"));
    }

    @ParameterizedTest
    @DisplayName("parse() throws InvalidDateException for various invalid formats")
    @ValueSource(strings = {
        "2020-03-15",
        "03/15/2020",
        "15-03-2020",
        "15.03.2020",
        "March 15, 2020",
        "hello",
        "abc/de/fghi",
        "15/3/2020",
        "5/03/2020"
    })
    void testParseVariousInvalidFormatsThrowException(String invalidInput) {
        assertThrows(InvalidDateException.class,
                () -> DateParserUtil.parse(invalidInput));
    }

    // ========================================================================
    // Phase 5: Empty String and Null Input Tests
    // ========================================================================

    @Test
    @DisplayName("parse() throws InvalidDateException for empty string input")
    void testParseEmptyStringThrowsException() {
        assertThrows(InvalidDateException.class,
                () -> DateParserUtil.parse(""));
    }

    @Test
    @DisplayName("parse() throws InvalidDateException for whitespace-only string")
    void testParseWhitespaceStringThrowsException() {
        assertThrows(InvalidDateException.class,
                () -> DateParserUtil.parse("   "));
    }

    @Test
    @DisplayName("parse() throws InvalidDateException for null input")
    void testParseNullInputThrowsException() {
        assertThrows(InvalidDateException.class,
                () -> DateParserUtil.parse(null));
    }

    // ========================================================================
    // Phase 6: Impossible Date Tests (Strict Resolution)
    // ========================================================================

    @Test
    @DisplayName("parse() throws InvalidDateException for impossible date February 31")
    void testParseFeb31ThrowsException() {
        assertThrows(InvalidDateException.class,
                () -> DateParserUtil.parse("31/02/2020"));
    }

    @Test
    @DisplayName("parse() throws InvalidDateException for impossible date April 31")
    void testParseApril31ThrowsException() {
        assertThrows(InvalidDateException.class,
                () -> DateParserUtil.parse("31/04/2020"));
    }

    @Test
    @DisplayName("parse() throws InvalidDateException for day zero")
    void testParseDayZeroThrowsException() {
        assertThrows(InvalidDateException.class,
                () -> DateParserUtil.parse("00/01/2020"));
    }

    @Test
    @DisplayName("parse() throws InvalidDateException for month 13")
    void testParseMonth13ThrowsException() {
        assertThrows(InvalidDateException.class,
                () -> DateParserUtil.parse("15/13/2020"));
    }

    @Test
    @DisplayName("parse() throws InvalidDateException for month zero")
    void testParseMonthZeroThrowsException() {
        assertThrows(InvalidDateException.class,
                () -> DateParserUtil.parse("15/00/2020"));
    }

    // ========================================================================
    // Phase 7: Return Type Validation
    // ========================================================================

    @Test
    @DisplayName("parse() returns a non-null LocalDate for valid input")
    void testParseReturnsNonNullLocalDate() throws InvalidDateException {
        LocalDate result = DateParserUtil.parse("10/05/1990");
        assertNotNull(result);
    }
}
