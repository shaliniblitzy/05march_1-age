package com.agecalculator.service;

import com.agecalculator.exception.InvalidDateException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Validation service for Date of Birth (DOB) input in the Age Calculator application.
 *
 * <p>Provides a stateless, static method to parse and validate user-provided DOB strings
 * in the {@code DD/MM/YYYY} format. Enforces three layers of validation:</p>
 * <ol>
 *     <li><strong>Format validation</strong> — input must match the {@code dd/MM/uuuu} pattern</li>
 *     <li><strong>Calendar validity</strong> — the date must be a real calendar date
 *         (e.g., February 31 is rejected)</li>
 *     <li><strong>Temporal validity</strong> — the date must not be after the current system date</li>
 * </ol>
 *
 * <p>Uses {@link DateTimeFormatter} with {@link ResolverStyle#STRICT} to enforce
 * strict calendar date parsing. The pattern {@code "dd/MM/uuuu"} is used instead of
 * {@code "dd/MM/yyyy"} because strict mode requires the proleptic year ({@code uuuu})
 * rather than the year-of-era ({@code yyyy}).</p>
 *
 * @author Age Calculator Application
 * @version 1.0.0
 * @see com.agecalculator.exception.InvalidDateException
 * @see java.time.format.DateTimeFormatter
 * @see java.time.format.ResolverStyle#STRICT
 */
public class DateValidator {

    /**
     * Strict date formatter for parsing DOB input in DD/MM/YYYY format.
     *
     * <p>Uses the pattern {@code "dd/MM/uuuu"} with {@link ResolverStyle#STRICT}
     * to reject invalid calendar dates such as February 31 or April 31.
     * The {@code uuuu} year pattern is mandatory for strict resolver mode.</p>
     */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private DateValidator() {
        // Utility class — not meant to be instantiated
    }

    /**
     * Parses and validates a Date of Birth string in DD/MM/YYYY format.
     *
     * <p>This method performs the following validation steps in order:</p>
     * <ol>
     *     <li>Parses the input string using a strict {@link DateTimeFormatter} configured
     *         with {@link ResolverStyle#STRICT} — this automatically rejects malformed
     *         input and impossible calendar dates (e.g., 31/02/2020)</li>
     *     <li>Checks that the parsed date is not after the current system date —
     *         future dates are rejected as invalid Dates of Birth</li>
     * </ol>
     *
     * @param dobString the Date of Birth string in DD/MM/YYYY format (e.g., "15/08/1998")
     * @return the parsed and validated {@link LocalDate} representing the Date of Birth
     * @throws InvalidDateException if the input fails any validation check:
     *         <ul>
     *             <li>Format error: "Invalid date format. Please use DD/MM/YYYY."</li>
     *             <li>Impossible date: "Invalid date. Please enter a real calendar date."</li>
     *             <li>Future date: "Date of Birth cannot be a future date."</li>
     *         </ul>
     */
    public static LocalDate parseAndValidate(String dobString) throws InvalidDateException {
        LocalDate dob;
        try {
            dob = LocalDate.parse(dobString, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidDateException("Invalid date format. Please use DD/MM/YYYY.");
        }

        if (dob.isAfter(LocalDate.now())) {
            throw new InvalidDateException("Date of Birth cannot be a future date.");
        }

        return dob;
    }
}
