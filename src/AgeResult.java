// Model class encapsulating the computed age breakdown
public class AgeResult {

    private final int years;
    private final int months;
    private final int days;

    /**
     * Constructs an AgeResult with the specified years, months, and days.
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
     * Returns the number of complete years in the age.
     *
     * @return the years component of the age
     */
    public int getYears() {
        return years;
    }

    /**
     * Returns the number of complete months beyond the years.
     *
     * @return the months component of the age
     */
    public int getMonths() {
        return months;
    }

    /**
     * Returns the number of remaining days beyond the months.
     *
     * @return the days component of the age
     */
    public int getDays() {
        return days;
    }

    /**
     * Returns a human-readable string representation of the age.
     * Format: "Your age is X years, Y months, and Z days."
     *
     * @return the formatted age string
     */
    @Override
    public String toString() {
        return "Your age is " + years + " years, " + months + " months, and " + days + " days.";
    }
}
