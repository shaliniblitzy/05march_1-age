import java.time.LocalDate;
import java.time.Period;

// Core computation class for age calculation using java.time.Period.
// This is a stateless utility class — all methods are static and no instance state is maintained.
// Follows Single Responsibility Principle: this class ONLY computes age.
// Validation is handled by DateValidator; display formatting is handled by AgeResult.toString().
public class AgeCalculator {

    /**
     * Calculates the exact age from the given date of birth to the current system date.
     * This is a convenience method that delegates to the overloaded version
     * using {@code LocalDate.now()} as the reference date.
     *
     * @param dob the date of birth as a {@code LocalDate}; must not be null
     * @return an {@code AgeResult} containing the age broken down into years, months, and days
     */
    public static AgeResult calculateAge(LocalDate dob) {
        // Delegate to the overloaded method with the current system date as reference
        return calculateAge(dob, LocalDate.now());
    }

    /**
     * Calculates the exact age from the given date of birth to the specified reference date.
     * Uses {@code Period.between()} which natively handles leap years and calendar edge cases.
     * The overloaded method with an explicit {@code referenceDate} parameter exists for testability,
     * allowing deterministic unit tests without depending on the current system clock.
     *
     * <p>Examples of edge cases handled correctly by {@code Period.between()}:</p>
     * <ul>
     *   <li>Leap year DOB (Feb 29) evaluated in a non-leap year — computed as Feb 28</li>
     *   <li>End-of-month transitions (e.g., born Jan 31, reference date Feb 28)</li>
     *   <li>Same-day birth returns 0 years, 0 months, 0 days</li>
     *   <li>Century-spanning periods (e.g., born in 1900, reference in 2026)</li>
     * </ul>
     *
     * @param dob           the date of birth as a {@code LocalDate}; must not be null
     * @param referenceDate the reference date to calculate age against; must not be null
     * @return an {@code AgeResult} containing the age broken down into years, months, and days
     */
    public static AgeResult calculateAge(LocalDate dob, LocalDate referenceDate) {
        // Period.between() natively handles leap years and calendar edge cases
        Period period = Period.between(dob, referenceDate);
        return new AgeResult(period.getYears(), period.getMonths(), period.getDays());
    }
}
