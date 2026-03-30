package com.agecalculator.io;

import com.agecalculator.model.AgeResult;

/**
 * A utility class responsible for formatting age calculation results into
 * human-readable strings. This class accepts an {@link AgeResult} instance
 * containing the computed age components (years, months, and days) and
 * produces a formatted output string matching the application's required
 * display format.
 *
 * <p>This class follows the Single Responsibility Principle — it is concerned
 * <strong>only</strong> with formatting the result. It does <strong>not</strong>
 * handle printing to the console, calculating ages, validating input, or reading
 * user input. The responsibility of printing the formatted string to
 * {@code System.out} belongs to the caller (typically
 * {@code AgeCalculatorApp}).</p>
 *
 * <p>All methods in this class are {@code static}, and the class cannot be
 * instantiated. This design enables easy consumption of the formatting logic
 * from any caller without requiring object creation.</p>
 *
 * <p><strong>Output format:</strong></p>
 * <pre>{@code
 * "Your age is X years, Y months, and Z days."
 * }</pre>
 * <p>where {@code X}, {@code Y}, and {@code Z} are the integer values obtained
 * from {@link AgeResult#getYears()}, {@link AgeResult#getMonths()}, and
 * {@link AgeResult#getDays()} respectively.</p>
 *
 * <p><strong>Usage example:</strong></p>
 * <pre>{@code
 * AgeResult result = new AgeResult(27, 6, 15);
 * String formatted = ResultFormatter.format(result);
 * // formatted == "Your age is 27 years, 6 months, and 15 days."
 * }</pre>
 *
 * @see com.agecalculator.model.AgeResult
 * @see com.agecalculator.service.AgeCalculator
 */
public class ResultFormatter {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * All functionality is provided through static methods.
     */
    private ResultFormatter() {
        // Utility class — prevent instantiation
    }

    /**
     * Formats the given {@link AgeResult} into a human-readable string showing
     * the computed age in years, months, and days.
     *
     * <p>The returned string follows the exact format required by the application
     * output specification:</p>
     * <pre>{@code
     * "Your age is X years, Y months, and Z days."
     * }</pre>
     *
     * <p><strong>Format details:</strong></p>
     * <ul>
     *   <li>Begins with the phrase {@code "Your age is "}</li>
     *   <li>Years component followed by {@code " years, "}</li>
     *   <li>Months component followed by {@code " months, and "}</li>
     *   <li>Days component followed by {@code " days."}</li>
     *   <li>Ends with a period ({@code .})</li>
     * </ul>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * AgeResult result = new AgeResult(27, 6, 15);
     * String output = ResultFormatter.format(result);
     * // output == "Your age is 27 years, 6 months, and 15 days."
     *
     * AgeResult zero = new AgeResult(0, 0, 0);
     * String zeroOutput = ResultFormatter.format(zero);
     * // zeroOutput == "Your age is 0 years, 0 months, and 0 days."
     * }</pre>
     *
     * @param result the {@link AgeResult} containing the computed age components
     *               (years, months, days); must not be {@code null}
     * @return a formatted string in the exact format:
     *         {@code "Your age is X years, Y months, and Z days."}
     * @throws IllegalArgumentException if {@code result} is {@code null}
     */
    public static String format(AgeResult result) {
        if (result == null) {
            throw new IllegalArgumentException("AgeResult must not be null.");
        }
        return String.format("Your age is %d years, %d months, and %d days.",
                result.getYears(), result.getMonths(), result.getDays());
    }
}
