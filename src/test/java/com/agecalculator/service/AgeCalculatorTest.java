package com.agecalculator.service;

import java.time.LocalDate;

import com.agecalculator.model.AgeResult;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link AgeCalculator} — the core age calculation engine.
 *
 * <p>Tests the {@link AgeCalculator#calculateAge(LocalDate)} method with various
 * Date of Birth inputs, including normal dates, leap year dates, boundary conditions,
 * and edge cases.</p>
 *
 * <p><strong>Testing strategy:</strong></p>
 * <ul>
 *   <li>For fixed historical DOBs (e.g., 15/08/1998): Range-based assertions are used
 *       because {@code LocalDate.now()} changes daily, making exact assertions fragile.</li>
 *   <li>For relative DOBs (today, yesterday, exactly 1 year ago): Exact assertions are
 *       used because the expected age is deterministic relative to the current date.</li>
 * </ul>
 *
 * @author Age Calculator Application
 * @version 1.0.0
 * @see AgeCalculator
 * @see AgeResult
 */
class AgeCalculatorTest {

    // ────────────────────────────────────────────────────────────────────────────
    // Test 1: Normal Date of Birth — MANDATORY per AAP §0.5.3
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Verifies that calculating age for a normal historical DOB (15/08/1998)
     * returns an {@link AgeResult} with plausible, non-negative components.
     *
     * <p>Since {@code LocalDate.now()} changes daily, we cannot hardcode the exact
     * expected values. Instead we verify:
     * <ul>
     *   <li>The result is not null</li>
     *   <li>Years is at least 26 (as of any date in 2025+)</li>
     *   <li>Months is in the valid range [0, 11]</li>
     *   <li>Days is in the valid range [0, 30]</li>
     * </ul>
     */
    @Test
    @DisplayName("Calculate age for normal DOB (15/08/1998) returns non-zero components")
    void testNormalDateOfBirth() {
        // Arrange — create a fixed historical date of birth
        LocalDate dob = LocalDate.of(1998, 8, 15);

        // Act — invoke the production calculation engine
        AgeResult result = AgeCalculator.calculateAge(dob);

        // Assert — verify result is present and within plausible ranges
        assertNotNull(result, "AgeResult should not be null");
        assertTrue(result.getYears() >= 26,
                "Years should be at least 26 for DOB 1998-08-15");
        assertTrue(result.getMonths() >= 0 && result.getMonths() <= 11,
                "Months should be between 0 and 11");
        assertTrue(result.getDays() >= 0 && result.getDays() <= 30,
                "Days should be between 0 and 30");
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Test 2: Leap Year Date of Birth — MANDATORY per AAP §0.5.3
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Verifies that calculating age for a leap-year DOB (29/02/2000) handles the
     * February 29 edge case correctly — including when the current year is not
     * a leap year.
     *
     * <p>{@code Period.between()} must produce valid, non-negative components
     * regardless of whether the current date falls in a leap year or not.</p>
     */
    @Test
    @DisplayName("Calculate age for leap year DOB (29/02/2000) handles Feb 29 correctly")
    void testLeapYearDateOfBirth() {
        // Arrange — February 29 in a leap year
        LocalDate dob = LocalDate.of(2000, 2, 29);

        // Act
        AgeResult result = AgeCalculator.calculateAge(dob);

        // Assert — verify plausible range for leap year birthday
        assertNotNull(result, "AgeResult should not be null for leap year DOB");
        assertTrue(result.getYears() >= 25,
                "Years should be at least 25 for DOB 2000-02-29");
        assertTrue(result.getMonths() >= 0 && result.getMonths() <= 11,
                "Months should be between 0 and 11");
        assertTrue(result.getDays() >= 0 && result.getDays() <= 30,
                "Days should be between 0 and 30");
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Test 3: Same-Day DOB (Today) — MANDATORY per AAP §0.5.3
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Verifies that when the DOB equals today's date, the resulting age is
     * exactly 0 years, 0 months, and 0 days.
     *
     * <p>This is a deterministic test — since DOB equals {@code LocalDate.now()},
     * the expected result is always (0, 0, 0) regardless of execution date.</p>
     */
    @Test
    @DisplayName("Calculate age for today's date returns zero years, months, and days")
    void testSameDayDateOfBirth() {
        // Arrange — DOB is today
        LocalDate dob = LocalDate.now();

        // Act
        AgeResult result = AgeCalculator.calculateAge(dob);

        // Assert — exact values since DOB = today, Period is always (0, 0, 0)
        assertNotNull(result, "AgeResult should not be null for same-day DOB");
        assertEquals(0, result.getYears(), "Years should be 0 for same-day DOB");
        assertEquals(0, result.getMonths(), "Months should be 0 for same-day DOB");
        assertEquals(0, result.getDays(), "Days should be 0 for same-day DOB");
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Test 4: Newborn (Yesterday) — Additional boundary coverage
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Verifies that when the DOB is yesterday, the resulting age is exactly
     * 0 years, 0 months, and 1 day.
     *
     * <p>This is a deterministic test — the relative difference between yesterday
     * and today is always exactly 1 day regardless of execution date.</p>
     */
    @Test
    @DisplayName("Calculate age for yesterday returns 0 years, 0 months, 1 day")
    void testYesterdayDateOfBirth() {
        // Arrange — DOB is yesterday
        LocalDate dob = LocalDate.now().minusDays(1);

        // Act
        AgeResult result = AgeCalculator.calculateAge(dob);

        // Assert — exact values since the relative offset is deterministic
        assertEquals(0, result.getYears(), "Years should be 0 for yesterday DOB");
        assertEquals(0, result.getMonths(), "Months should be 0 for yesterday DOB");
        assertEquals(1, result.getDays(), "Days should be 1 for yesterday DOB");
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Test 5: Centenarian Edge Case — MANDATORY per AAP §0.5.3
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Verifies that the calculation engine correctly handles DOBs from 100+ years
     * ago, producing an age with years >= 100.
     *
     * <p>The DOB is set to 100 years and 1 day before today to ensure the years
     * component is at least 100, validating that {@code Period.between()} handles
     * large year differences without overflow or incorrect computation.</p>
     */
    @Test
    @DisplayName("Calculate age for DOB 100+ years ago returns centenarian age")
    void testCentenarianDateOfBirth() {
        // Arrange — DOB is 100 years and 1 day ago
        LocalDate dob = LocalDate.now().minusYears(100).minusDays(1);

        // Act
        AgeResult result = AgeCalculator.calculateAge(dob);

        // Assert — years must be at least 100 for a centenarian
        assertNotNull(result, "AgeResult should not be null for centenarian");
        assertTrue(result.getYears() >= 100,
                "Years should be at least 100 for centenarian DOB");
    }

    // ────────────────────────────────────────────────────────────────────────────
    // Test 6: Exactly One Year Ago — Boundary coverage
    // ────────────────────────────────────────────────────────────────────────────

    /**
     * Verifies that when the DOB is exactly one year before today, the resulting
     * age is exactly 1 year, 0 months, and 0 days.
     *
     * <p>This is a deterministic test — the relative difference between one year
     * ago and today produces a {@code Period} of exactly (1, 0, 0) on most days.</p>
     *
     * <p><strong>Leap year edge case:</strong> If today is February 29 (a leap year),
     * {@code minusYears(1)} adjusts to February 28 (since Feb 29 does not exist in
     * the prior non-leap year). In that scenario, {@code Period.between(Feb 28, Feb 29)}
     * returns (1 year, 0 months, 1 day), so the expected days value is 1 instead of 0.
     * A guard clause handles this edge case to ensure the test is robust year-round.</p>
     */
    @Test
    @DisplayName("Calculate age for DOB exactly one year ago returns 1 year, 0 months, 0 days")
    void testExactlyOneYearAgo() {
        // Arrange — DOB is exactly one year ago
        LocalDate today = LocalDate.now();
        LocalDate dob = today.minusYears(1);

        // Act
        AgeResult result = AgeCalculator.calculateAge(dob);

        // Assert — years and months are always deterministic
        assertEquals(1, result.getYears(), "Years should be 1 for DOB exactly 1 year ago");
        assertEquals(0, result.getMonths(), "Months should be 0 for DOB exactly 1 year ago");

        // Guard: if today is Feb 29 (leap year), minusYears(1) adjusts to Feb 28,
        // so Period.between(Feb 28, Feb 29) yields 1 day instead of 0
        if (today.getMonthValue() == 2 && today.getDayOfMonth() == 29) {
            assertEquals(1, result.getDays(),
                    "Days should be 1 when today is Feb 29 (leap day edge case)");
        } else {
            assertEquals(0, result.getDays(),
                    "Days should be 0 for DOB exactly 1 year ago");
        }
    }
}
