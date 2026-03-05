package com.agecalculator.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Comprehensive JUnit Jupiter unit tests for the {@link AgeResult} immutable model class.
 *
 * <p>This test class validates two critical aspects of {@code AgeResult}:</p>
 * <ol>
 *     <li><strong>Constructor and Getter Correctness</strong> — verifies that values passed
 *         to the constructor are faithfully stored and returned by the corresponding getter
 *         methods ({@code getYears()}, {@code getMonths()}, {@code getDays()}).</li>
 *     <li><strong>toString() Format Compliance</strong> — verifies that the formatted string
 *         output matches the exact specification:
 *         {@code "Your age is X years, Y months, and Z days."} including proper punctuation,
 *         zero-value display, and large-value formatting.</li>
 * </ol>
 *
 * <p>Each test method is self-contained with no shared mutable state between tests,
 * following the Arrange-Act-Assert pattern.</p>
 */
public class AgeResultTest {

    // =========================================================================
    // Getter Method Tests — Constructor + Accessor Validation
    // =========================================================================

    @Test
    @DisplayName("getYears() returns the years value set via constructor")
    void testGetYearsReturnsCorrectValue() {
        // Arrange
        AgeResult result = new AgeResult(25, 3, 10);

        // Act & Assert
        assertEquals(25, result.getYears());
    }

    @Test
    @DisplayName("getMonths() returns the months value set via constructor")
    void testGetMonthsReturnsCorrectValue() {
        // Arrange
        AgeResult result = new AgeResult(25, 3, 10);

        // Act & Assert
        assertEquals(3, result.getMonths());
    }

    @Test
    @DisplayName("getDays() returns the days value set via constructor")
    void testGetDaysReturnsCorrectValue() {
        // Arrange
        AgeResult result = new AgeResult(25, 3, 10);

        // Act & Assert
        assertEquals(10, result.getDays());
    }

    @Test
    @DisplayName("All getter methods return correct values for a different age")
    void testAllGettersReturnCorrectValues() {
        // Arrange
        AgeResult result = new AgeResult(50, 11, 28);

        // Act & Assert
        assertEquals(50, result.getYears());
        assertEquals(11, result.getMonths());
        assertEquals(28, result.getDays());
    }

    // =========================================================================
    // toString() Format Tests — Exact Format Verification
    // Format: "Your age is X years, Y months, and Z days."
    // =========================================================================

    @Test
    @DisplayName("toString() returns correctly formatted string for standard age")
    void testToStringWithStandardAge() {
        // Arrange
        AgeResult result = new AgeResult(25, 3, 10);

        // Act
        String actual = result.toString();

        // Assert
        assertEquals("Your age is 25 years, 3 months, and 10 days.", actual);
    }

    @Test
    @DisplayName("toString() displays zero values correctly for all components")
    void testToStringWithAllZeroValues() {
        // Arrange
        AgeResult result = new AgeResult(0, 0, 0);

        // Act
        String actual = result.toString();

        // Assert
        assertEquals("Your age is 0 years, 0 months, and 0 days.", actual);
    }

    @Test
    @DisplayName("toString() displays correctly when months is zero")
    void testToStringWithZeroMonths() {
        // Arrange
        AgeResult result = new AgeResult(1, 0, 15);

        // Act
        String actual = result.toString();

        // Assert
        assertEquals("Your age is 1 years, 0 months, and 15 days.", actual);
    }

    @Test
    @DisplayName("toString() displays correctly when days is zero")
    void testToStringWithZeroDays() {
        // Arrange
        AgeResult result = new AgeResult(30, 6, 0);

        // Act
        String actual = result.toString();

        // Assert
        assertEquals("Your age is 30 years, 6 months, and 0 days.", actual);
    }

    @Test
    @DisplayName("toString() displays correctly when years is zero")
    void testToStringWithZeroYears() {
        // Arrange
        AgeResult result = new AgeResult(0, 5, 20);

        // Act
        String actual = result.toString();

        // Assert
        assertEquals("Your age is 0 years, 5 months, and 20 days.", actual);
    }

    @Test
    @DisplayName("toString() formats correctly for large age values")
    void testToStringWithLargeValues() {
        // Arrange
        AgeResult result = new AgeResult(125, 11, 29);

        // Act
        String actual = result.toString();

        // Assert
        assertEquals("Your age is 125 years, 11 months, and 29 days.", actual);
    }

    @Test
    @DisplayName("toString() formats correctly for single unit values")
    void testToStringWithSingleUnitValues() {
        // Arrange
        AgeResult result = new AgeResult(1, 1, 1);

        // Act
        String actual = result.toString();

        // Assert
        assertEquals("Your age is 1 years, 1 months, and 1 days.", actual);
    }
}
