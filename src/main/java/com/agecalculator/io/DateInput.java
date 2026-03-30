package com.agecalculator.io;

import java.util.Scanner;

/**
 * Utility class for handling console input for the Age Calculator application.
 *
 * <p>This class is responsible for reading the Date of Birth (DOB) string from the user
 * via standard console input. It provides a single static method that prompts the user
 * and captures their input using a {@link Scanner} instance passed by the caller
 * (dependency injection pattern).</p>
 *
 * <p><strong>Important:</strong> This class does NOT perform any input validation.
 * All validation — including format checking, calendar validity, and future date rejection —
 * is handled by {@link com.agecalculator.service.DateValidator} in the service package.
 * The raw input string is returned exactly as entered by the user.</p>
 *
 * <p>This class cannot be instantiated — it is designed as a utility class with
 * static methods only.</p>
 *
 * @see com.agecalculator.service.DateValidator
 * @see java.util.Scanner
 */
public class DateInput {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * All methods in this class are static and should be accessed directly
     * via the class name.
     */
    private DateInput() {
        // Utility class — prevent instantiation
    }

    /**
     * Prompts the user to enter their Date of Birth and reads the input from the console.
     *
     * <p>This method displays the prompt {@code "Enter your Date of Birth (DD/MM/YYYY): "}
     * on the standard output (using {@link System#out} with {@code print()} so the cursor
     * remains on the same line) and then reads the full line of input from the provided
     * {@link Scanner} instance.</p>
     *
     * <p>The returned string is the raw, unmodified input exactly as typed by the user.
     * No trimming, validation, or transformation is applied. It is the caller's
     * responsibility to pass this string to the appropriate validation service.</p>
     *
     * @param scanner the {@link Scanner} instance connected to the input stream
     *                (typically {@link System#in} for console applications)
     * @return the raw input string entered by the user, without any validation
     *         or transformation
     */
    public static String readDateOfBirth(Scanner scanner) {
        System.out.print("Enter your Date of Birth (DD/MM/YYYY): ");
        return scanner.nextLine();
    }
}
