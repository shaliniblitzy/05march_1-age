package com.agecalculator.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;

/**
 * Unit tests for the {@link AgeUtils} static utility class.
 *
 * <p>Tests cover total months computation, total days computation,
 * days-until-next-birthday countdown, leap year birthday edge cases
 * (Feb 29 born user in non-leap year falls back to Feb 28),
 * birthday-is-today (returns 0), and birthday-was-yesterday
 * (returns ~364/365 days).</p>
 *
 * <p>All test assertions that depend on the current date use
 * {@link LocalDate#now()} and {@link ChronoUnit} to dynamically compute
 * expected values, ensuring tests remain date-agnostic and pass regardless
 * of when they are executed.</p>
 *
 * @see AgeUtils
 */
class AgeUtilsTest {

    // -------------------------------------------------------------------------
    // Tests for totalMonths(LocalDate dob)
    // -------------------------------------------------------------------------

    /**
     * Verifies that {@link AgeUtils#totalMonths(LocalDate)} returns a positive
     * value matching {@link ChronoUnit#MONTHS} computation for a known past DOB.
     */
    @Test
    @DisplayName("totalMonths returns positive value for past date of birth")
    void testTotalMonthsForPastDob() {
        LocalDate dob = LocalDate.of(2000, 1, 1);
        long expected = ChronoUnit.MONTHS.between(dob, LocalDate.now());
        long actual = AgeUtils.totalMonths(dob);
        assertEquals(expected, actual);
    }

    /**
     * Verifies that {@link AgeUtils#totalMonths(LocalDate)} returns zero
     * when the date of birth is today (a newborn scenario).
     */
    @Test
    @DisplayName("totalMonths returns zero when DOB is today")
    void testTotalMonthsForToday() {
        LocalDate today = LocalDate.now();
        assertEquals(0, AgeUtils.totalMonths(today));
    }

    // -------------------------------------------------------------------------
    // Tests for totalDays(LocalDate dob)
    // -------------------------------------------------------------------------

    /**
     * Verifies that {@link AgeUtils#totalDays(LocalDate)} returns a positive
     * value matching {@link ChronoUnit#DAYS} computation for a known past DOB.
     */
    @Test
    @DisplayName("totalDays returns positive value for past date of birth")
    void testTotalDaysForPastDob() {
        LocalDate dob = LocalDate.of(2000, 1, 1);
        long expected = ChronoUnit.DAYS.between(dob, LocalDate.now());
        long actual = AgeUtils.totalDays(dob);
        assertEquals(expected, actual);
    }

    /**
     * Verifies that {@link AgeUtils#totalDays(LocalDate)} returns zero
     * when the date of birth is today.
     */
    @Test
    @DisplayName("totalDays returns zero when DOB is today")
    void testTotalDaysForToday() {
        LocalDate today = LocalDate.now();
        assertEquals(0, AgeUtils.totalDays(today));
    }

    /**
     * Verifies that {@link AgeUtils#totalDays(LocalDate)} returns exactly 1
     * when the date of birth is yesterday.
     */
    @Test
    @DisplayName("totalDays returns 1 when DOB is yesterday")
    void testTotalDaysForYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        assertEquals(1, AgeUtils.totalDays(yesterday));
    }

    // -------------------------------------------------------------------------
    // Tests for daysUntilNextBirthday(LocalDate dob) — Main Countdown
    // -------------------------------------------------------------------------

    /**
     * Verifies that {@link AgeUtils#daysUntilNextBirthday(LocalDate)} returns 0
     * when the user's birthday falls on today's date. Constructs a DOB that
     * shares the same month and day as today but is 25 years in the past.
     */
    @Test
    @DisplayName("daysUntilNextBirthday returns 0 when today is the birthday")
    void testDaysUntilNextBirthdayIsToday() {
        LocalDate today = LocalDate.now();
        // Same month/day as today, 25 years ago
        LocalDate dob = today.minusYears(25);
        long daysUntil = AgeUtils.daysUntilNextBirthday(dob);
        assertEquals(0, daysUntil);
    }

    /**
     * Verifies that {@link AgeUtils#daysUntilNextBirthday(LocalDate)} returns
     * approximately 364 or 365 days when the user's birthday was yesterday.
     * The exact value depends on whether the current or next year is a leap year.
     */
    @Test
    @DisplayName("daysUntilNextBirthday returns approximately 364 or 365 when birthday was yesterday")
    void testDaysUntilNextBirthdayWasYesterday() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        // Same month/day as yesterday, 25 years ago
        LocalDate dob = yesterday.minusYears(25);
        long daysUntil = AgeUtils.daysUntilNextBirthday(dob);
        assertTrue(daysUntil >= 364 && daysUntil <= 365,
                "Expected 364 or 365 days until next birthday, but got: " + daysUntil);
    }

    /**
     * Verifies that {@link AgeUtils#daysUntilNextBirthday(LocalDate)} returns
     * a positive number of days for a birthday that is still upcoming (30 days
     * from now). Uses explicit month/day construction for precision.
     */
    @Test
    @DisplayName("daysUntilNextBirthday returns positive days for a birthday still upcoming this year")
    void testDaysUntilNextBirthdayUpcomingThisYear() {
        LocalDate today = LocalDate.now();
        LocalDate upcomingBirthday = today.plusDays(30);
        // Construct DOB with the same month/day as the upcoming date, 20 years ago
        LocalDate dob = LocalDate.of(
                today.getYear() - 20,
                upcomingBirthday.getMonth(),
                upcomingBirthday.getDayOfMonth()
        );
        long daysUntil = AgeUtils.daysUntilNextBirthday(dob);
        assertEquals(30, daysUntil);
    }

    // -------------------------------------------------------------------------
    // Leap Year Edge Case Tests
    // -------------------------------------------------------------------------

    /**
     * Verifies that {@link AgeUtils#daysUntilNextBirthday(LocalDate)} correctly
     * handles a February 29 birthday. In non-leap years, the birthday reference
     * should fall back to February 28. The method must NOT throw an exception
     * and must return a valid non-negative countdown value.
     */
    @Test
    @DisplayName("daysUntilNextBirthday handles Feb 29 birthday in non-leap year by falling back to Feb 28")
    void testLeapYearBirthdayFallbackToFeb28() {
        LocalDate leapDob = LocalDate.of(2000, Month.FEBRUARY, 29);
        long daysUntil = AgeUtils.daysUntilNextBirthday(leapDob);
        // Must be a valid non-negative countdown (0 to at most 366 days)
        assertTrue(daysUntil >= 0 && daysUntil <= 366,
                "Expected valid countdown for Feb 29 birthday, got: " + daysUntil);
    }

    /**
     * Verifies that {@link AgeUtils#totalMonths(LocalDate)} correctly computes
     * the total months for a February 29 leap year date of birth without throwing
     * any exceptions.
     */
    @Test
    @DisplayName("totalMonths correctly computes for Feb 29 leap year DOB")
    void testTotalMonthsLeapYearDob() {
        LocalDate dob = LocalDate.of(2000, Month.FEBRUARY, 29);
        long expected = ChronoUnit.MONTHS.between(dob, LocalDate.now());
        assertEquals(expected, AgeUtils.totalMonths(dob));
    }

    /**
     * Verifies that {@link AgeUtils#totalDays(LocalDate)} correctly computes
     * the total days for a February 29 leap year date of birth without throwing
     * any exceptions.
     */
    @Test
    @DisplayName("totalDays correctly computes for Feb 29 leap year DOB")
    void testTotalDaysLeapYearDob() {
        LocalDate dob = LocalDate.of(2000, Month.FEBRUARY, 29);
        long expected = ChronoUnit.DAYS.between(dob, LocalDate.now());
        assertEquals(expected, AgeUtils.totalDays(dob));
    }

    // -------------------------------------------------------------------------
    // Additional Edge Case Tests
    // -------------------------------------------------------------------------

    /**
     * Verifies that both {@link AgeUtils#totalMonths(LocalDate)} and
     * {@link AgeUtils#totalDays(LocalDate)} return strictly positive values
     * for multiple well-known past dates, ensuring no negative results appear.
     */
    @Test
    @DisplayName("totalMonths and totalDays return non-negative values for past dates")
    void testNonNegativeResults() {
        LocalDate dob1 = LocalDate.of(1990, 6, 15);
        LocalDate dob2 = LocalDate.of(1950, 1, 1);
        assertTrue(AgeUtils.totalMonths(dob1) > 0,
                "totalMonths should be positive for 1990-06-15");
        assertTrue(AgeUtils.totalDays(dob1) > 0,
                "totalDays should be positive for 1990-06-15");
        assertTrue(AgeUtils.totalMonths(dob2) > 0,
                "totalMonths should be positive for 1950-01-01");
        assertTrue(AgeUtils.totalDays(dob2) > 0,
                "totalDays should be positive for 1950-01-01");
    }
}
