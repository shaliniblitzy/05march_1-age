package com.agecalculator.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the {@link AgeResult} immutable data class.
 *
 * <p>Tests cover construction, accessor methods ({@code getYears()}, {@code getMonths()},
 * {@code getDays()}), the {@code toString()} output format, zero-value edge cases,
 * centenarian (large-value) edge cases, and immutability guarantees.</p>
 *
 * <p>Each test method verifies a specific behavioral contract of the {@code AgeResult}
 * class to ensure that the immutable data transfer object faithfully preserves age
 * components and produces the exact output format required by the application
 * specification: {@code "Your age is X years, Y months, and Z days."}</p>
 *
 * @see AgeResult
 */
class AgeResultTest {

    // -------------------------------------------------------------------------
    // Construction Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("AgeResult can be constructed with typical age values")
    void testConstructionWithTypicalValues() {
        AgeResult result = new AgeResult(27, 6, 15);
        assertNotNull(result);
    }

    @Test
    @DisplayName("AgeResult can be constructed with all zero values")
    void testConstructionWithZeroValues() {
        AgeResult result = new AgeResult(0, 0, 0);
        assertNotNull(result);
    }

    // -------------------------------------------------------------------------
    // Accessor Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("getYears() returns the correct years value")
    void testGetYears() {
        AgeResult result = new AgeResult(27, 6, 15);
        assertEquals(27, result.getYears());
    }

    @Test
    @DisplayName("getMonths() returns the correct months value")
    void testGetMonths() {
        AgeResult result = new AgeResult(27, 6, 15);
        assertEquals(6, result.getMonths());
    }

    @Test
    @DisplayName("getDays() returns the correct days value")
    void testGetDays() {
        AgeResult result = new AgeResult(27, 6, 15);
        assertEquals(15, result.getDays());
    }

    @Test
    @DisplayName("Accessors return zero for all-zero AgeResult")
    void testZeroValueAccessors() {
        AgeResult result = new AgeResult(0, 0, 0);
        assertEquals(0, result.getYears());
        assertEquals(0, result.getMonths());
        assertEquals(0, result.getDays());
    }

    // -------------------------------------------------------------------------
    // toString() Tests — Exact Format Verification
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("toString() produces the exact required output format")
    void testToStringFormat() {
        AgeResult result = new AgeResult(27, 6, 15);
        assertEquals("Your age is 27 years, 6 months, and 15 days.", result.toString());
    }

    @Test
    @DisplayName("toString() correctly formats all-zero age")
    void testToStringWithZeroValues() {
        AgeResult result = new AgeResult(0, 0, 0);
        assertEquals("Your age is 0 years, 0 months, and 0 days.", result.toString());
    }

    // -------------------------------------------------------------------------
    // Edge Case Tests — Centenarian
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("AgeResult correctly handles centenarian age (100+ years)")
    void testCentenarianAge() {
        AgeResult result = new AgeResult(105, 3, 22);
        assertEquals(105, result.getYears());
        assertEquals(3, result.getMonths());
        assertEquals(22, result.getDays());
        assertEquals("Your age is 105 years, 3 months, and 22 days.", result.toString());
    }

    // -------------------------------------------------------------------------
    // Immutability Guarantee Tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("Multiple calls to getters return the same values, verifying immutability")
    void testImmutabilityViaRepeatedAccess() {
        AgeResult result = new AgeResult(30, 11, 28);
        assertEquals(result.getYears(), result.getYears());
        assertEquals(result.getMonths(), result.getMonths());
        assertEquals(result.getDays(), result.getDays());
        assertEquals(30, result.getYears());
        assertEquals(11, result.getMonths());
        assertEquals(28, result.getDays());
    }
}
