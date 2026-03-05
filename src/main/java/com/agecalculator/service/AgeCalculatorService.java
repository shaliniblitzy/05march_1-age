package com.agecalculator.service;

import java.time.LocalDate;
import java.time.Period;

import com.agecalculator.model.AgeResult;

/**
 * Stateless service class responsible for computing the exact age of a person
 * given their date of birth. The age is decomposed into three components:
 * years, months, and days.
 *
 * <p>This class follows the Single Responsibility Principle — its sole job is
 * computing the chronological difference between a validated date of birth and
 * the current system date. It performs no input validation (handled by
 * {@code DateValidator}), no date parsing (handled by {@code DateParserUtil}),
 * and no I/O operations (handled by {@code AgeCalculatorApp}).</p>
 *
 * <p>Age computation relies exclusively on the {@code java.time} API as mandated
 * by the project requirements:</p>
 * <ul>
 *     <li>{@link java.time.LocalDate} — date representation without timezone context</li>
 *     <li>{@link java.time.Period} — ISO-chronology-based date difference computation</li>
 * </ul>
 *
 * <p>Leap year handling is automatic — {@code Period.between()} uses the proleptic
 * Gregorian calendar and correctly handles all leap year boundaries, including
 * persons born on February 29.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 *     AgeCalculatorService service = new AgeCalculatorService();
 *     LocalDate dob = LocalDate.of(1990, 5, 15);
 *     AgeResult result = service.calculateAge(dob);
 *     System.out.println(result);
 *     // Output: Your age is 35 years, 9 months, and 18 days.
 * }</pre>
 */
public class AgeCalculatorService {

    /**
     * Computes the exact age in years, months, and days for the given date of birth.
     *
     * <p>The computation uses {@link Period#between(LocalDate, LocalDate)} to calculate
     * the chronological difference between the provided date of birth and the current
     * system date obtained via {@link LocalDate#now()}. The resulting {@link Period}
     * is decomposed into its year, month, and day components, which are encapsulated
     * in an {@link AgeResult} instance.</p>
     *
     * <p>This method assumes the date of birth has already been validated by the
     * caller (typically {@code DateValidator}). It performs no validation checks
     * and lets any runtime exceptions propagate naturally.</p>
     *
     * @param dob the validated date of birth, must not be {@code null}
     * @return an {@link AgeResult} containing the computed years, months, and days
     */
    public AgeResult calculateAge(LocalDate dob) {
        Period period = Period.between(dob, LocalDate.now());

        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays();

        return new AgeResult(years, months, days);
    }
}
