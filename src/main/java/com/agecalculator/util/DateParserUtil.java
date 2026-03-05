package com.agecalculator.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.regex.Pattern;

import com.agecalculator.exception.InvalidDateException;

/**
 * Utility class for parsing user-input date strings into {@link LocalDate} objects
 * using strict date resolution.
 *
 * <p>This class provides a single static method {@link #parse(String)} that converts
 * date strings in the {@code DD/MM/YYYY} format into {@link LocalDate} instances.
 * It enforces strict date resolution via {@link ResolverStyle#STRICT}, which ensures
 * that logically impossible dates (e.g., February 30, February 29 on non-leap years,
 * month 13, day 32) are rejected during parsing rather than silently adjusted.</p>
 *
 * <h3>Design Decisions</h3>
 * <ul>
 *   <li><strong>Stateless utility</strong> — all methods are {@code static}; no instance
 *       state is maintained. A private constructor prevents instantiation.</li>
 *   <li><strong>Single Responsibility</strong> — this class handles ONLY string-to-date
 *       conversion. Temporal validation (e.g., future date rejection) is the responsibility
 *       of {@code DateValidator} in the validator package.</li>
 *   <li><strong>Clean exception API</strong> — JDK {@link DateTimeParseException} is caught
 *       internally and wrapped in the application-specific {@link InvalidDateException},
 *       preserving the original cause via exception chaining.</li>
 *   <li><strong>Strict resolver with proleptic year</strong> — the formatter uses the
 *       pattern {@code "dd/MM/uuuu"} (proleptic year) instead of {@code "dd/MM/yyyy"}
 *       (year-of-era) because {@link ResolverStyle#STRICT} requires proleptic year
 *       fields to resolve correctly.</li>
 * </ul>
 *
 * <h3>Leap Year Handling</h3>
 * <p>Leap year correctness is handled natively by {@link LocalDate}'s built-in
 * Gregorian calendar arithmetic combined with {@link ResolverStyle#STRICT}:</p>
 * <ul>
 *   <li>{@code "29/02/2024"} — valid (2024 is a leap year)</li>
 *   <li>{@code "29/02/2023"} — rejected (2023 is not a leap year)</li>
 *   <li>{@code "29/02/1900"} — rejected (century year not divisible by 400)</li>
 *   <li>{@code "29/02/2000"} — valid (century year divisible by 400)</li>
 * </ul>
 *
 * @see com.agecalculator.validator.DateValidator
 * @see com.agecalculator.exception.InvalidDateException
 */
public class DateParserUtil {

    /**
     * Reusable, thread-safe date formatter configured for strict DD/MM/YYYY parsing.
     *
     * <p>Uses the proleptic year pattern {@code "dd/MM/uuuu"} combined with
     * {@link ResolverStyle#STRICT} to enforce exact calendar validity. The
     * {@code uuuu} specifier is required (rather than {@code yyyy}) because
     * strict resolution operates on proleptic year fields, not year-of-era.</p>
     *
     * <p>{@link DateTimeFormatter} instances are immutable and thread-safe,
     * so this constant can be safely shared across concurrent invocations.</p>
     */
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Compiled regex pattern that matches the structural DD/MM/YYYY format:
     * exactly two digits, a forward slash, two digits, a forward slash, and four digits.
     *
     * <p>This pattern validates ONLY the syntactic structure of the input string —
     * it does NOT validate whether the day/month/year values represent a real calendar
     * date. For example, {@code "31/02/2020"} matches this pattern (correct format)
     * but fails strict calendar resolution (impossible date). This two-phase approach
     * enables distinct error messages for format errors vs. impossible-date errors,
     * as required by the application's error message specification.</p>
     */
    private static final Pattern DD_MM_YYYY_PATTERN = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$");

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * <p>All functionality is provided through the static {@link #parse(String)}
     * method. There is no need to create instances of {@code DateParserUtil}.</p>
     */
    private DateParserUtil() {
        // Utility class — instantiation not permitted
    }

    /**
     * Parses a date string in {@code DD/MM/YYYY} format into a {@link LocalDate}.
     *
     * <p>This method performs three layers of validation:</p>
     * <ol>
     *   <li><strong>Null/empty guard</strong> — rejects {@code null} references and
     *       blank strings immediately with a descriptive error message.</li>
     *   <li><strong>Format structure check</strong> — validates that the input matches
     *       the syntactic {@code DD/MM/YYYY} pattern (two-digit day, slash, two-digit
     *       month, slash, four-digit year) using a compiled regex. Inputs that fail
     *       this check receive a format-specific error message.</li>
     *   <li><strong>Strict calendar parsing</strong> — delegates to
     *       {@link LocalDate#parse(CharSequence, DateTimeFormatter)} with the strict
     *       formatter, which rejects logically impossible calendar dates (e.g.,
     *       February 30, April 31, February 29 on non-leap years). Inputs that pass
     *       the format check but fail calendar resolution receive a distinct
     *       calendar-validity error message.</li>
     * </ol>
     *
     * <p>This two-phase parse approach enables differentiated error messaging:
     * format errors (e.g., "abc", "2020-03-15") produce one message, while
     * impossible calendar dates (e.g., "31/02/2020") produce a distinct message.</p>
     *
     * <p><strong>Note:</strong> This method does NOT validate temporal constraints
     * (e.g., whether the date is in the future). Such validation is the responsibility
     * of {@code DateValidator} in the validator package.</p>
     *
     * @param dateStr the date string to parse, expected in {@code DD/MM/YYYY} format
     *                (e.g., {@code "15/03/1990"}, {@code "29/02/2000"})
     * @return a {@link LocalDate} representing the parsed date
     * @throws InvalidDateException if {@code dateStr} is {@code null}, empty/blank,
     *                              does not conform to the {@code DD/MM/YYYY} format,
     *                              or represents a logically impossible calendar date
     */
    public static LocalDate parse(String dateStr) throws InvalidDateException {
        // Guard clause: reject null or blank input before attempting parse
        if (dateStr == null || dateStr.trim().isEmpty()) {
            throw new InvalidDateException("Date input cannot be null or empty.");
        }

        // Phase 1: Validate syntactic format (DD/MM/YYYY structural pattern)
        if (!DD_MM_YYYY_PATTERN.matcher(dateStr).matches()) {
            throw new InvalidDateException(
                    "Invalid date format. Please use DD/MM/YYYY format.");
        }

        // Phase 2: Strict calendar resolution — format is correct, validate calendar validity
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidDateException(
                    "Invalid date. Please enter a valid calendar date.", e);
        }
    }
}
