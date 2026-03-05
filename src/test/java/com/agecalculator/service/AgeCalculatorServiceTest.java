package com.agecalculator.service;

import java.time.LocalDate;
import java.time.Period;

import com.agecalculator.model.AgeResult;
import com.agecalculator.service.AgeCalculatorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit Jupiter unit tests for {@link AgeCalculatorService}.
 *
 * <p>This test class verifies the core age computation logic performed by
 * {@code AgeCalculatorService.calculateAge(LocalDate dob)}, which uses
 * {@code Period.between(dob, LocalDate.now())} to compute the exact age
 * decomposed into years, months, and days.</p>
 *
 * <p>Test scenarios covered:</p>
 * <ul>
 *     <li>Standard age calculation with a known past date</li>
 *     <li>Non-null return value verification</li>
 *     <li>Exact birthday edge case (DOB is today — all components zero)</li>
 *     <li>Leap year DOB (February 29)</li>
 *     <li>Non-leap year February 28 DOB</li>
 *     <li>Century boundary crossing</li>
 *     <li>Newborn scenario (yesterday's date)</li>
 *     <li>Very old person (born in 1900)</li>
 *     <li>Parameterized tests with multiple DOB variations</li>
 *     <li>Formatted toString() output verification</li>
 *     <li>Exactly one year ago DOB</li>
 * </ul>
 *
 * <p>All tests use {@code Period.between()} as the verification oracle to
 * dynamically compute expected values, ensuring tests remain valid regardless
 * of the date they are executed.</p>
 */
public class AgeCalculatorServiceTest {

    /** The service instance under test, re-initialized before each test method. */
    private AgeCalculatorService service;

    /**
     * Initializes a fresh {@link AgeCalculatorService} instance before each
     * test method to ensure test isolation and prevent state leakage between tests.
     */
    @BeforeEach
    void setUp() {
        service = new AgeCalculatorService();
    }

    /**
     * Verifies that the service computes the correct age for a standard,
     * well-known past date of birth. Uses {@code Period.between()} as the
     * independent verification oracle to compare years, months, and days.
     */
    @Test
    @DisplayName("Should calculate correct age for a standard date of birth")
    void shouldCalculateCorrectAgeForStandardDob() {
        LocalDate dob = LocalDate.of(1990, 3, 15);

        AgeResult result = service.calculateAge(dob);

        Period expected = Period.between(dob, LocalDate.now());
        assertEquals(expected.getYears(), result.getYears(),
                "Years component should match Period.between() oracle");
        assertEquals(expected.getMonths(), result.getMonths(),
                "Months component should match Period.between() oracle");
        assertEquals(expected.getDays(), result.getDays(),
                "Days component should match Period.between() oracle");
    }

    /**
     * Verifies that the service returns a non-null {@link AgeResult} for
     * any valid past date of birth, ensuring the contract of never returning
     * null is upheld.
     */
    @Test
    @DisplayName("Should return a non-null AgeResult for a valid date of birth")
    void shouldReturnNonNullAgeResult() {
        LocalDate dob = LocalDate.of(1985, 7, 20);

        AgeResult result = service.calculateAge(dob);

        assertNotNull(result, "AgeResult must not be null for a valid DOB");
    }

    /**
     * Verifies the exact birthday edge case where the date of birth is today.
     * All three age components (years, months, days) must be zero, confirming
     * that the service handles the zero-difference boundary correctly.
     */
    @Test
    @DisplayName("Should return 0 years, 0 months, and 0 days when DOB is today")
    void shouldReturnZeroAgeWhenDobIsToday() {
        LocalDate dob = LocalDate.now();

        AgeResult result = service.calculateAge(dob);

        assertEquals(0, result.getYears(),
                "Years should be 0 when DOB is today");
        assertEquals(0, result.getMonths(),
                "Months should be 0 when DOB is today");
        assertEquals(0, result.getDays(),
                "Days should be 0 when DOB is today");
    }

    /**
     * Verifies correct age computation for a person born on February 29
     * during a leap year. Year 2000 is a leap year (divisible by 400),
     * making February 29 a valid date. This test confirms that
     * {@code Period.between()} handles the leap day DOB correctly.
     */
    @Test
    @DisplayName("Should correctly calculate age for person born on February 29 leap year")
    void shouldCalculateAgeForLeapYearFeb29() {
        LocalDate dob = LocalDate.of(2000, 2, 29);

        AgeResult result = service.calculateAge(dob);

        Period expected = Period.between(dob, LocalDate.now());
        assertEquals(expected.getYears(), result.getYears(),
                "Years should match for leap year Feb 29 DOB");
        assertEquals(expected.getMonths(), result.getMonths(),
                "Months should match for leap year Feb 29 DOB");
        assertEquals(expected.getDays(), result.getDays(),
                "Days should match for leap year Feb 29 DOB");
    }

    /**
     * Verifies correct age computation for a person born on February 28
     * in a non-leap year (2001). This complements the leap year test by
     * confirming accurate computation for the last valid February date
     * in non-leap years.
     */
    @Test
    @DisplayName("Should correctly calculate age for person born on February 28")
    void shouldCalculateAgeForFeb28NonLeapYear() {
        LocalDate dob = LocalDate.of(2001, 2, 28);

        AgeResult result = service.calculateAge(dob);

        Period expected = Period.between(dob, LocalDate.now());
        assertEquals(expected.getYears(), result.getYears(),
                "Years should match for Feb 28 non-leap year DOB");
        assertEquals(expected.getMonths(), result.getMonths(),
                "Months should match for Feb 28 non-leap year DOB");
        assertEquals(expected.getDays(), result.getDays(),
                "Days should match for Feb 28 non-leap year DOB");
    }

    /**
     * Verifies that age computation works correctly when the date of birth
     * spans from the 20th century to the 21st century. Uses December 31, 1999
     * as the DOB to test the century transition boundary.
     */
    @Test
    @DisplayName("Should correctly calculate age spanning century boundary")
    void shouldCalculateAgeSpanningCenturyBoundary() {
        LocalDate dob = LocalDate.of(1999, 12, 31);

        AgeResult result = service.calculateAge(dob);

        Period expected = Period.between(dob, LocalDate.now());
        assertEquals(expected.getYears(), result.getYears(),
                "Years should match for century boundary DOB");
        assertEquals(expected.getMonths(), result.getMonths(),
                "Months should match for century boundary DOB");
        assertEquals(expected.getDays(), result.getDays(),
                "Days should match for century boundary DOB");
    }

    /**
     * Verifies the newborn edge case where the date of birth is yesterday.
     * The expected result is exactly 0 years, 0 months, and 1 day.
     */
    @Test
    @DisplayName("Should return 0 years, 0 months, and 1 day for yesterday's date")
    void shouldReturnOneDayForYesterday() {
        LocalDate dob = LocalDate.now().minusDays(1);

        AgeResult result = service.calculateAge(dob);

        assertEquals(0, result.getYears(),
                "Years should be 0 for yesterday's DOB");
        assertEquals(0, result.getMonths(),
                "Months should be 0 for yesterday's DOB");
        assertEquals(1, result.getDays(),
                "Days should be 1 for yesterday's DOB");
    }

    /**
     * Verifies that the service correctly handles very old dates of birth.
     * Uses January 1, 1900 as the DOB, which should produce an age of
     * more than 100 years. This test confirms large year counts are
     * computed accurately by the service.
     */
    @Test
    @DisplayName("Should correctly calculate age for very old person born in 1900")
    void shouldCalculateAgeForVeryOldPerson() {
        LocalDate dob = LocalDate.of(1900, 1, 1);

        AgeResult result = service.calculateAge(dob);

        Period expected = Period.between(dob, LocalDate.now());
        assertEquals(expected.getYears(), result.getYears(),
                "Years should match for very old person DOB");
        assertEquals(expected.getMonths(), result.getMonths(),
                "Months should match for very old person DOB");
        assertEquals(expected.getDays(), result.getDays(),
                "Days should match for very old person DOB");
        assertTrue(result.getYears() > 100,
                "Person born in 1900 should be over 100 years old");
    }

    /**
     * Parameterized test that verifies age computation across multiple
     * varied dates of birth. Each row in the CSV source provides a year,
     * month, and day triple that is used to construct a {@link LocalDate}
     * and assert correctness against the {@code Period.between()} oracle.
     *
     * @param year  the birth year
     * @param month the birth month
     * @param day   the birth day
     */
    @ParameterizedTest
    @DisplayName("Should calculate correct age for various dates of birth")
    @CsvSource({
            "1985, 6, 15",
            "2000, 1, 1",
            "1970, 12, 25",
            "2010, 7, 4"
    })
    void shouldCalculateCorrectAgeForVariousDates(int year, int month, int day) {
        LocalDate dob = LocalDate.of(year, month, day);

        AgeResult result = service.calculateAge(dob);

        Period expected = Period.between(dob, LocalDate.now());
        assertEquals(expected.getYears(), result.getYears(),
                "Years should match for DOB " + dob);
        assertEquals(expected.getMonths(), result.getMonths(),
                "Months should match for DOB " + dob);
        assertEquals(expected.getDays(), result.getDays(),
                "Days should match for DOB " + dob);
    }

    /**
     * Verifies that the {@link AgeResult#toString()} method produces output
     * matching the exact format specified by the AAP (Section 0.7.5):
     * {@code "Your age is X years, Y months, and Z days."}
     *
     * <p>The expected string is constructed dynamically using
     * {@code Period.between()} as the oracle, ensuring the test remains
     * valid regardless of execution date.</p>
     */
    @Test
    @DisplayName("Should produce correctly formatted toString output")
    void shouldProduceCorrectlyFormattedToString() {
        LocalDate dob = LocalDate.of(1995, 8, 22);

        AgeResult result = service.calculateAge(dob);

        Period expected = Period.between(dob, LocalDate.now());
        String expectedString = "Your age is " + expected.getYears() + " years, "
                + expected.getMonths() + " months, and "
                + expected.getDays() + " days.";
        assertEquals(expectedString, result.toString(),
                "toString() must match the exact format: 'Your age is X years, Y months, and Z days.'");
    }

    /**
     * Verifies that a date of birth exactly one year in the past produces
     * an age of exactly 1 year, 0 months, and 0 days. This confirms that
     * whole-year boundaries are computed accurately.
     */
    @Test
    @DisplayName("Should return exactly 1 year, 0 months, 0 days for date one year ago")
    void shouldReturnExactlyOneYearForDateOneYearAgo() {
        LocalDate dob = LocalDate.now().minusYears(1);

        AgeResult result = service.calculateAge(dob);

        assertEquals(1, result.getYears(),
                "Years should be exactly 1 for date one year ago");
        assertEquals(0, result.getMonths(),
                "Months should be 0 for date one year ago");
        assertEquals(0, result.getDays(),
                "Days should be 0 for date one year ago");
    }
}
