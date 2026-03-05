package com.agecalculator.model;

/**
 * Immutable data transfer object that encapsulates a computed age
 * decomposed into three components: years, months, and days.
 *
 * <p>This class is constructed by {@code AgeCalculatorService} after
 * computing the difference between a date of birth and the current
 * system date using {@code java.time.Period}. The formatted string
 * representation is consumed by {@code AgeCalculatorApp} for display.</p>
 *
 * <p>Instances of this class are immutable — all fields are {@code final}
 * and set exclusively through the constructor. No setter methods are
 * provided.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 *     AgeResult result = new AgeResult(25, 3, 10);
 *     System.out.println(result);
 *     // Output: Your age is 25 years, 3 months, and 10 days.
 * }</pre>
 */
public class AgeResult {

    /** The number of complete years in the computed age. */
    private final int years;

    /** The number of complete months beyond the years in the computed age. */
    private final int months;

    /** The number of remaining days beyond the months in the computed age. */
    private final int days;

    /**
     * Constructs an {@code AgeResult} with the specified years, months, and days.
     *
     * @param years  the number of complete years in the age
     * @param months the number of complete months beyond the years
     * @param days   the number of remaining days beyond the months
     */
    public AgeResult(int years, int months, int days) {
        this.years = years;
        this.months = months;
        this.days = days;
    }

    /**
     * Returns the number of complete years in the computed age.
     *
     * @return the years component of the age
     */
    public int getYears() {
        return years;
    }

    /**
     * Returns the number of complete months beyond the years in the computed age.
     *
     * @return the months component of the age
     */
    public int getMonths() {
        return months;
    }

    /**
     * Returns the number of remaining days beyond the months in the computed age.
     *
     * @return the days component of the age
     */
    public int getDays() {
        return days;
    }

    /**
     * Returns a formatted string representation of the age in the exact format:
     * {@code "Your age is X years, Y months, and Z days."}
     *
     * <p>All three components are always displayed, including when their values
     * are zero. The word "and" precedes the days component, commas separate
     * the years and months components, and the sentence ends with a period.</p>
     *
     * @return the formatted age string
     */
    @Override
    public String toString() {
        return "Your age is " + years + " years, " + months + " months, and " + days + " days.";
    }
}
