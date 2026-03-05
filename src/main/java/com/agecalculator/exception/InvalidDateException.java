package com.agecalculator.exception;

/**
 * Custom checked exception thrown when a user-provided date string is either
 * malformed (wrong format) or represents a logically impossible calendar date.
 *
 * <p>This exception is used to signal two categories of invalid date input:</p>
 * <ul>
 *   <li><strong>Format errors</strong> — the input string does not conform to
 *       the expected {@code DD/MM/YYYY} pattern (e.g., alphabetic characters,
 *       missing separators, empty strings).</li>
 *   <li><strong>Impossible dates</strong> — the input parses structurally but
 *       represents a date that does not exist on the Gregorian calendar
 *       (e.g., February 30, month 13, day 32, February 29 on a non-leap year).</li>
 * </ul>
 *
 * <p>As a checked exception extending {@link Exception}, callers are required
 * to handle this exception explicitly via {@code try-catch} blocks or by
 * declaring it in their method's {@code throws} clause. This design enforces
 * robust error handling at every layer of the application.</p>
 *
 * <p><strong>Exception chaining:</strong> The {@link #InvalidDateException(String, Throwable)}
 * constructor supports wrapping lower-level exceptions (such as
 * {@link java.time.format.DateTimeParseException}) while preserving the
 * original stack trace for diagnostic purposes.</p>
 *
 * @see com.agecalculator.util.DateParserUtil
 * @see com.agecalculator.exception.FutureDateException
 */
public class InvalidDateException extends Exception {

    /**
     * Constructs a new {@code InvalidDateException} with the specified
     * detail message.
     *
     * <p>This constructor is used when the error condition can be fully
     * described by a message string alone, without an underlying cause
     * exception.</p>
     *
     * @param message the detail message describing why the date is invalid;
     *                retained for later retrieval by {@link #getMessage()}
     */
    public InvalidDateException(String message) {
        super(message);
    }

    /**
     * Constructs a new {@code InvalidDateException} with the specified
     * detail message and cause.
     *
     * <p>This constructor enables exception chaining, allowing lower-level
     * parsing exceptions (e.g., {@link java.time.format.DateTimeParseException})
     * to be wrapped in this application-specific exception while preserving
     * the original failure's stack trace and context.</p>
     *
     * @param message the detail message describing why the date is invalid;
     *                retained for later retrieval by {@link #getMessage()}
     * @param cause   the underlying exception that triggered this invalid date
     *                error; retained for later retrieval by {@link #getCause()}.
     *                A {@code null} value is permitted and indicates that the
     *                cause is nonexistent or unknown.
     */
    public InvalidDateException(String message, Throwable cause) {
        super(message, cause);
    }
}
