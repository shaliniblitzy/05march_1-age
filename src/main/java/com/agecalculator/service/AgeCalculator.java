package com.agecalculator.service;

import com.agecalculator.model.AgeResult;
import java.time.LocalDate;
import java.time.Period;

/**
 * Core age calculation engine for the Age Calculator application.
 *
 * <p>Provides a stateless, pure-function method to compute a person's exact age
 * from their Date of Birth (DOB) to the current system date. The age is broken
 * down into three discrete components: years, months, and days.</p>
 *
 * <p>This class uses {@link java.time.Period#between(LocalDate, LocalDate)} for
 * calendar-aware date arithmetic that correctly handles varying month lengths,
 * leap years, and all edge cases in the Gregorian calendar.</p>
 *
 * <p>Design: Stateless utility class with static methods — no instance creation needed.
 * All methods are deterministic given the same DOB and current date.</p>
 *
 * @author Age Calculator Application
 * @version 1.0.0
 * @see com.agecalculator.model.AgeResult
 * @see java.time.Period
 */
public class AgeCalculator {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AgeCalculator() {
        // Utility class — not meant to be instantiated
    }

    /**
     * Calculates the exact age from the given Date of Birth to the current system date.
     *
     * <p>Uses {@link Period#between(LocalDate, LocalDate)} to compute the difference
     * between the DOB and {@link LocalDate#now()}, yielding an exact breakdown into
     * years, months, and days. This method correctly handles:</p>
     * <ul>
     *     <li>Leap year birthdays (e.g., February 29)</li>
     *     <li>Varying month lengths (28, 29, 30, 31 days)</li>
     *     <li>Year boundaries</li>
     *     <li>Same-day DOB (returns 0 years, 0 months, 0 days)</li>
     * </ul>
     *
     * @param dob the validated Date of Birth as a {@link LocalDate} — must not be null,
     *            must not be after the current date (validation is performed by
     *            {@link DateValidator#parseAndValidate(String)} before this method is called)
     * @return an {@link AgeResult} containing the computed years, months, and days components
     */
    public static AgeResult calculateAge(LocalDate dob) {
        LocalDate today = LocalDate.now();
        Period period = Period.between(dob, today);
        return new AgeResult(period.getYears(), period.getMonths(), period.getDays());
    }
}
