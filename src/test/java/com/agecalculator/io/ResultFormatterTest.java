package com.agecalculator.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.agecalculator.model.AgeResult;

/**
 * Unit test class for {@link ResultFormatter}.
 *
 * <p>Verifies that the {@link ResultFormatter#format(AgeResult)} method produces
 * the exact output string format required by the application specification:
 * {@code "Your age is X years, Y months, and Z days."}</p>
 *
 * <p>Test coverage includes:</p>
 * <ul>
 *   <li>Standard age values with non-zero years, months, and days</li>
 *   <li>Zero-component edge cases (newborn / same-day DOB)</li>
 *   <li>Single non-zero component scenarios</li>
 *   <li>Large age values (centenarian)</li>
 *   <li>Maximum boundary values for months (11) and days (30)</li>
 * </ul>
 *
 * <p>Each test method is independent and stateless — a fresh {@link AgeResult}
 * instance is created within each test. No shared mutable state exists between
 * test methods.</p>
 *
 * @see com.agecalculator.io.ResultFormatter
 * @see com.agecalculator.model.AgeResult
 */
class ResultFormatterTest {

    /**
     * Verifies that the formatter produces the correct output string for
     * standard, non-zero age component values. This is the primary test
     * case from the user's example (DOB 15/08/1998 yielding 27 years,
     * 6 months, and 15 days).
     */
    @Test
    @DisplayName("format produces correct string for standard age values")
    void testFormatStandardAge() {
        AgeResult result = new AgeResult(27, 6, 15);
        String output = ResultFormatter.format(result);
        assertEquals("Your age is 27 years, 6 months, and 15 days.", output);
    }

    /**
     * Verifies that the formatter correctly handles the zero-age edge case
     * where all components (years, months, days) are zero. This corresponds
     * to a newborn or same-day DOB scenario.
     */
    @Test
    @DisplayName("format produces correct string for zero age components")
    void testFormatZeroAge() {
        AgeResult result = new AgeResult(0, 0, 0);
        String output = ResultFormatter.format(result);
        assertEquals("Your age is 0 years, 0 months, and 0 days.", output);
    }

    /**
     * Verifies that the formatter handles the case where only the years
     * component is non-zero (exactly one year) while months and days are
     * both zero. Confirms that the format uses "years" even for singular
     * values (no pluralization logic per AAP specification).
     */
    @Test
    @DisplayName("format produces correct string for one year zero months zero days")
    void testFormatOneYearZeroMonthsZeroDays() {
        AgeResult result = new AgeResult(1, 0, 0);
        String output = ResultFormatter.format(result);
        assertEquals("Your age is 1 years, 0 months, and 0 days.", output);
    }

    /**
     * Verifies that the formatter correctly handles the case where years
     * are zero but months and days are non-zero. Confirms that zero years
     * are still displayed in the output (no omission of zero components).
     */
    @Test
    @DisplayName("format produces correct string for zero years with months and days")
    void testFormatZeroYearsWithMonthsAndDays() {
        AgeResult result = new AgeResult(0, 5, 20);
        String output = ResultFormatter.format(result);
        assertEquals("Your age is 0 years, 5 months, and 20 days.", output);
    }

    /**
     * Verifies that the formatter correctly handles large age values
     * corresponding to a centenarian (100+ years). Confirms that
     * three-digit year values are formatted correctly without truncation.
     */
    @Test
    @DisplayName("format produces correct string for centenarian age")
    void testFormatCentenarianAge() {
        AgeResult result = new AgeResult(100, 11, 29);
        String output = ResultFormatter.format(result);
        assertEquals("Your age is 100 years, 11 months, and 29 days.", output);
    }

    /**
     * Verifies that the formatter correctly handles the minimum non-zero
     * scenario where only the days component is non-zero (e.g., a DOB
     * that was yesterday). Years and months remain zero.
     */
    @Test
    @DisplayName("format produces correct string for only days component")
    void testFormatOnlyDays() {
        AgeResult result = new AgeResult(0, 0, 1);
        String output = ResultFormatter.format(result);
        assertEquals("Your age is 0 years, 0 months, and 1 days.", output);
    }

    /**
     * Verifies that the formatter correctly handles the upper boundary
     * values that {@code Period.between} can produce for months (max 11)
     * and days (max approximately 30). Confirms that the formatter does
     * not break with these maximum component values.
     */
    @Test
    @DisplayName("format produces correct string for maximum months and days")
    void testFormatMaxMonthsAndDays() {
        AgeResult result = new AgeResult(50, 11, 30);
        String output = ResultFormatter.format(result);
        assertEquals("Your age is 50 years, 11 months, and 30 days.", output);
    }
}
