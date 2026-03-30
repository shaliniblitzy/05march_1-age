package com.agecalculator.exception;

/**
 * Custom checked exception for the Age Calculator application.
 *
 * <p>Thrown by {@link com.agecalculator.service.DateValidator} when Date of Birth (DOB)
 * input fails validation. This exception carries a descriptive, user-friendly error
 * message that can be displayed directly to the user.</p>
 *
 * <p>Validation failure scenarios that trigger this exception:</p>
 * <ul>
 *     <li>Invalid date format — input does not match DD/MM/YYYY pattern</li>
 *     <li>Impossible calendar date — e.g., 31/02/2020 (February 31 does not exist)</li>
 *     <li>Future date — DOB is after the current system date</li>
 * </ul>
 *
 * @author Age Calculator Application
 * @version 1.0.0
 */
public class InvalidDateException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new InvalidDateException with the specified detail message.
     *
     * <p>The message should be a user-friendly, actionable description of the validation
     * failure. It is retrieved by {@code getMessage()} and displayed to the user by the
     * main application class.</p>
     *
     * @param message the detail message describing the validation failure
     */
    public InvalidDateException(String message) {
        super(message);
    }
}
