package com.agecalculator.util;

import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Utility class providing static helper methods for age-related calculations.
 *
 * <p>This class extends the core age computation with additional data points
 * including total age in months, total age in days, and countdown to the next
 * birthday. All methods are static and stateless, designed for independent
 * reuse beyond the console application (e.g., by a future GUI or REST API).</p>
 *
 * <p>All date calculations use the {@code java.time} API with {@link LocalDate}
 * and {@link ChronoUnit} for calendar-aware arithmetic.</p>
 *
 * @author Age Calculator Application
 * @version 1.0.0
 * @see java.time.LocalDate
 * @see java.time.temporal.ChronoUnit
 */
public class AgeUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AgeUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Computes the total number of months between the given date of birth and today.
     *
     * <p>Uses {@link ChronoUnit#MONTHS} for calendar-aware month counting that
     * correctly handles varying month lengths and leap years.</p>
     *
     * @param dob the date of birth, must not be null and must not be after today
     * @return the total number of months between the DOB and the current date
     */
    public static long totalMonths(LocalDate dob) {
        Objects.requireNonNull(dob, "Date of birth must not be null");
        return ChronoUnit.MONTHS.between(dob, LocalDate.now());
    }

    /**
     * Computes the total number of days between the given date of birth and today.
     *
     * <p>Uses {@link ChronoUnit#DAYS} for exact day counting that correctly
     * handles leap years and varying month lengths.</p>
     *
     * @param dob the date of birth, must not be null and must not be after today
     * @return the total number of days between the DOB and the current date
     */
    public static long totalDays(LocalDate dob) {
        Objects.requireNonNull(dob, "Date of birth must not be null");
        return ChronoUnit.DAYS.between(dob, LocalDate.now());
    }

    /**
     * Computes the number of days remaining until the user's next birthday.
     *
     * <p>Determines the next occurrence of the user's birth month and day relative
     * to today's date. If the birthday has already occurred this year, it calculates
     * the countdown to next year's birthday.</p>
     *
     * <p><strong>Leap year handling:</strong> For users born on February 29, if the
     * current (or next) year is NOT a leap year, the birthday reference falls back
     * to February 28 of that year.</p>
     *
     * @param dob the date of birth, must not be null and must not be after today
     * @return the number of days until the next birthday occurrence
     */
    public static long daysUntilNextBirthday(LocalDate dob) {
        Objects.requireNonNull(dob, "Date of birth must not be null");
        LocalDate today = LocalDate.now();

        // Determine this year's birthday with leap year fallback for Feb 29 births
        LocalDate birthdayThisYear;
        if (dob.getMonth() == Month.FEBRUARY && dob.getDayOfMonth() == 29) {
            // Leap year birthday: fall back to Feb 28 if current year is not a leap year
            if (today.isLeapYear()) {
                birthdayThisYear = LocalDate.of(today.getYear(), Month.FEBRUARY, 29);
            } else {
                birthdayThisYear = LocalDate.of(today.getYear(), Month.FEBRUARY, 28);
            }
        } else {
            birthdayThisYear = LocalDate.of(today.getYear(), dob.getMonth(), dob.getDayOfMonth());
        }

        // If birthday hasn't passed yet this year (including today), use it
        if (!birthdayThisYear.isBefore(today)) {
            return ChronoUnit.DAYS.between(today, birthdayThisYear);
        } else {
            // Birthday already passed this year, compute next year's birthday
            int nextYear = today.getYear() + 1;
            LocalDate birthdayNextYear;
            if (dob.getMonth() == Month.FEBRUARY && dob.getDayOfMonth() == 29) {
                // Leap year birthday: fall back to Feb 28 if next year is not a leap year
                if (LocalDate.of(nextYear, 1, 1).isLeapYear()) {
                    birthdayNextYear = LocalDate.of(nextYear, Month.FEBRUARY, 29);
                } else {
                    birthdayNextYear = LocalDate.of(nextYear, Month.FEBRUARY, 28);
                }
            } else {
                birthdayNextYear = LocalDate.of(nextYear, dob.getMonth(), dob.getDayOfMonth());
            }
            return ChronoUnit.DAYS.between(today, birthdayNextYear);
        }
    }
}
