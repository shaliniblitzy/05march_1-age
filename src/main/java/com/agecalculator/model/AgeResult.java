package com.agecalculator.model;

/**
 * An immutable data class that holds the computed age components resulting from
 * an age calculation. This class stores the result of computing the difference
 * between a Date of Birth and the current date, broken down into discrete
 * years, months, and days components.
 *
 * <p>{@code AgeResult} serves as the central data transfer object (DTO) shared
 * across all modules of the Age Calculator application:</p>
 * <ul>
 *   <li>{@code AgeCalculator} (service) — creates instances of this class</li>
 *   <li>{@code ResultFormatter} (io) — reads its properties for output formatting</li>
 *   <li>{@code AgeCalculatorApp} (main) — receives it from the calculator and passes it to the formatter</li>
 *   <li>{@code AgeUtils} (util) — may consume it for enhancement calculations</li>
 * </ul>
 *
 * <p>This class enforces immutability through {@code private final} fields and
 * the absence of setter methods. Once constructed, the age components cannot be
 * modified, ensuring thread safety and predictable behavior across the application.</p>
 *
 * <p><strong>Usage example:</strong></p>
 * <pre>{@code
 * AgeResult result = new AgeResult(27, 6, 15);
 * System.out.println(result.getYears());  // 27
 * System.out.println(result.getMonths()); // 6
 * System.out.println(result.getDays());   // 15
 * System.out.println(result);             // Your age is 27 years, 6 months, and 15 days.
 * }</pre>
 *
 * @see com.agecalculator.service.AgeCalculator
 */
public class AgeResult {

    /**
     * The years component of the computed age.
     * Represents the number of complete years between the date of birth and the
     * current date. This value is always non-negative.
     */
    private final int years;

    /**
     * The months component of the computed age (0-11).
     * Represents the number of complete months remaining after the full years
     * have been accounted for. This value ranges from 0 to 11 inclusive.
     */
    private final int months;

    /**
     * The days component of the computed age (0-30).
     * Represents the number of remaining days after the full years and months
     * have been accounted for. This value ranges from 0 to 30 inclusive,
     * depending on the specific months involved.
     */
    private final int days;

    /**
     * Constructs a new {@code AgeResult} with the specified age components.
     *
     * <p>Each parameter corresponds to a discrete component of the computed age
     * derived from the difference between a date of birth and the current date
     * using calendar-aware arithmetic (e.g., {@code java.time.Period}).</p>
     *
     * @param years  the years component of the age (non-negative integer
     *               representing complete years)
     * @param months the months component of the age (integer in the range 0-11
     *               representing remaining complete months after full years)
     * @param days   the days component of the age (integer in the range 0-30
     *               representing remaining days after full years and months)
     */
    public AgeResult(int years, int months, int days) {
        this.years = years;
        this.months = months;
        this.days = days;
    }

    /**
     * Returns the years component of the computed age.
     *
     * @return the years component of the age, representing the number of
     *         complete years between the date of birth and the current date
     */
    public int getYears() {
        return years;
    }

    /**
     * Returns the months component of the computed age.
     *
     * @return the months component of the age, representing the number of
     *         remaining complete months (0-11) after full years have been
     *         accounted for
     */
    public int getMonths() {
        return months;
    }

    /**
     * Returns the days component of the computed age.
     *
     * @return the days component of the age, representing the number of
     *         remaining days (0-30) after full years and months have been
     *         accounted for
     */
    public int getDays() {
        return days;
    }

    /**
     * Returns a human-readable string representation of the age in the format
     * required by the application output specification.
     *
     * <p>The returned string follows the exact format:
     * {@code "Your age is X years, Y months, and Z days."} where X, Y, and Z
     * are the years, months, and days components respectively.</p>
     *
     * <p><strong>Example:</strong> For an {@code AgeResult} constructed with
     * {@code new AgeResult(27, 6, 15)}, this method returns:
     * {@code "Your age is 27 years, 6 months, and 15 days."}</p>
     *
     * @return a formatted string representing the age in the format
     *         "Your age is X years, Y months, and Z days."
     */
    @Override
    public String toString() {
        return String.format("Your age is %d years, %d months, and %d days.", years, months, days);
    }
}
