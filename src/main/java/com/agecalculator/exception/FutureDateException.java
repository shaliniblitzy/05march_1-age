package com.agecalculator.exception;

/**
 * Custom checked exception thrown when a user-provided Date of Birth
 * is strictly after the current system date.
 *
 * <p>This exception enforces the temporal constraint that a person's
 * date of birth cannot be in the future. Callers must handle this
 * exception via try-catch or a throws declaration, as it extends
 * {@link Exception} (checked exception).</p>
 *
 * <p>Typical usage context:</p>
 * <ul>
 *   <li>Thrown by {@code DateValidator.validate()} when
 *       {@code dob.isAfter(LocalDate.now())}</li>
 *   <li>Caught by {@code AgeCalculatorApp.main()} to display a
 *       user-friendly error message</li>
 * </ul>
 *
 * @see java.lang.Exception
 */
public class FutureDateException extends Exception {

    /**
     * Constructs a new {@code FutureDateException} with the specified
     * detail message describing why the date was rejected.
     *
     * @param message the detail message explaining the validation failure,
     *                for example: {@code "Date of birth cannot be a future date."}
     */
    public FutureDateException(String message) {
        super(message);
    }
}
