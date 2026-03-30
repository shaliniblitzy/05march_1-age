package com.agecalculator;

import com.agecalculator.exception.InvalidDateException;
import com.agecalculator.io.DateInput;
import com.agecalculator.io.ResultFormatter;
import com.agecalculator.model.AgeResult;
import com.agecalculator.service.AgeCalculator;
import com.agecalculator.service.DateValidator;
import com.agecalculator.util.AgeUtils;

import java.time.LocalDate;
import java.util.Scanner;

/**
 * Main entry point for the Age Calculator console application.
 *
 * <p>This class orchestrates the complete workflow for computing a user's
 * exact age from their Date of Birth (DOB). It follows the Single Responsibility
 * Principle by delegating all business logic to dedicated service, I/O, model,
 * and utility classes:</p>
 *
 * <ol>
 *     <li><strong>Input</strong> — {@link DateInput} reads the DOB string from the console</li>
 *     <li><strong>Validation</strong> — {@link DateValidator} parses and validates the input
 *         against format, calendar, and temporal rules</li>
 *     <li><strong>Calculation</strong> — {@link AgeCalculator} computes the exact age as
 *         years, months, and days using {@code java.time.Period}</li>
 *     <li><strong>Formatting</strong> — {@link ResultFormatter} produces the display string
 *         in the required format</li>
 *     <li><strong>Enhancements</strong> — {@link AgeUtils} provides additional calculations
 *         (total months, total days, days until next birthday)</li>
 * </ol>
 *
 * <p>All exceptions are handled gracefully with user-friendly messages — no stack
 * traces are ever displayed to the user. The {@link Scanner} is managed via
 * try-with-resources to ensure proper resource cleanup.</p>
 *
 * <p><strong>Usage:</strong></p>
 * <pre>{@code
 * $ java -jar age-calculator-1.0.0-SNAPSHOT.jar
 * Enter your Date of Birth (DD/MM/YYYY): 15/08/1998
 * Your age is 27 years, 6 months, and 15 days.
 *
 * Total months: 330
 * Total days: 10060
 * Days until next birthday: 138
 * }</pre>
 *
 * @author Age Calculator Application
 * @version 1.0.0
 * @see com.agecalculator.io.DateInput
 * @see com.agecalculator.service.DateValidator
 * @see com.agecalculator.service.AgeCalculator
 * @see com.agecalculator.io.ResultFormatter
 * @see com.agecalculator.util.AgeUtils
 */
public class AgeCalculatorApp {

    /**
     * Application entry point that orchestrates the age calculation workflow.
     *
     * <p>Executes the following linear workflow:</p>
     * <ol>
     *     <li>Opens a {@link Scanner} on {@code System.in} via try-with-resources</li>
     *     <li>Reads the DOB string from the user via {@link DateInput#readDateOfBirth(Scanner)}</li>
     *     <li>Parses and validates the input via {@link DateValidator#parseAndValidate(String)}</li>
     *     <li>Computes the exact age via {@link AgeCalculator#calculateAge(LocalDate)}</li>
     *     <li>Formats and prints the result via {@link ResultFormatter#format(AgeResult)}</li>
     *     <li>Prints optional enhancement data (total months, total days, next birthday countdown)
     *         via {@link AgeUtils}</li>
     * </ol>
     *
     * <p>Exception handling strategy:</p>
     * <ul>
     *     <li>{@link InvalidDateException} — caught and displayed as
     *         {@code "Error: <message>"} for validation failures</li>
     *     <li>{@link Exception} — caught as a safety net and displayed as
     *         a generic error message for unexpected failures</li>
     * </ul>
     *
     * @param args command-line arguments (not used by this application)
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {

            // Step 1 — Read the Date of Birth input from the console
            String dobString = DateInput.readDateOfBirth(scanner);

            // Step 2 — Parse and validate the input (throws InvalidDateException on failure)
            LocalDate dob = DateValidator.parseAndValidate(dobString);

            // Step 3 — Calculate the exact age from the validated DOB
            AgeResult result = AgeCalculator.calculateAge(dob);

            // Step 4 — Format and display the main result
            System.out.println(ResultFormatter.format(result));

            // Step 5 — Display optional enhancement output
            System.out.println();
            System.out.println("Total months: " + AgeUtils.totalMonths(dob));
            System.out.println("Total days: " + AgeUtils.totalDays(dob));
            System.out.println("Days until next birthday: " + AgeUtils.daysUntilNextBirthday(dob));

        } catch (InvalidDateException e) {
            // Validation failure — display the specific error message to the user
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            // Safety net — catch any unexpected runtime errors gracefully
            System.out.println("An unexpected error occurred. Please try again.");
        }
    }
}
