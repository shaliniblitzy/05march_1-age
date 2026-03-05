package com.agecalculator;

import java.util.Scanner;

import com.agecalculator.service.AgeCalculatorService;
import com.agecalculator.validator.DateValidator;
import com.agecalculator.model.AgeResult;
import com.agecalculator.exception.InvalidDateException;
import com.agecalculator.exception.FutureDateException;

/**
 * Main entry point for the Age Calculator console application.
 *
 * <p>This class is the sole I/O orchestration layer — it manages console input
 * from the user, delegates input validation to {@link DateValidator}, delegates
 * age computation to {@link AgeCalculatorService}, and displays the formatted
 * result or user-friendly error messages.</p>
 *
 * <p><strong>No business logic exists in this class.</strong> All date parsing,
 * validation, and age calculation are handled by dedicated service, validator,
 * and utility classes in their respective packages. This design adheres to the
 * Single Responsibility Principle: this class's sole job is orchestrating
 * console I/O.</p>
 *
 * <h3>Application Flow</h3>
 * <ol>
 *   <li>Prompt the user for their Date of Birth in {@code DD/MM/YYYY} format</li>
 *   <li>Read the input string from the console</li>
 *   <li>Delegate validation to {@link DateValidator#validate(String)}</li>
 *   <li>Delegate age computation to {@link AgeCalculatorService#calculateAge(java.time.LocalDate)}</li>
 *   <li>Display the formatted age via {@link AgeResult#toString()}</li>
 * </ol>
 *
 * <h3>Error Handling</h3>
 * <ul>
 *   <li>{@link InvalidDateException} — caught when the input is malformed or
 *       represents an impossible calendar date (e.g., February 30)</li>
 *   <li>{@link FutureDateException} — caught when the input date is after the
 *       current system date</li>
 *   <li>{@link Exception} — general fallback for any unexpected runtime errors</li>
 * </ul>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * $ java -jar target/age-calculator-1.0.0.jar
 * Enter your Date of Birth (DD/MM/YYYY): 15/03/1990
 * Your age is 35 years, 11 months, and 20 days.
 * }</pre>
 *
 * @see com.agecalculator.validator.DateValidator
 * @see com.agecalculator.service.AgeCalculatorService
 * @see com.agecalculator.model.AgeResult
 */
public class AgeCalculatorApp {

    /**
     * Application entry point — orchestrates the console-based age calculation workflow.
     *
     * <p>This method performs the following steps in sequence:</p>
     * <ol>
     *   <li>Opens a {@link Scanner} on {@code System.in} via try-with-resources</li>
     *   <li>Displays the input prompt on the same line as the cursor</li>
     *   <li>Reads the user's date-of-birth string</li>
     *   <li>Validates the input via {@link DateValidator#validate(String)}</li>
     *   <li>Computes the age via {@link AgeCalculatorService#calculateAge(java.time.LocalDate)}</li>
     *   <li>Prints the formatted result via {@link AgeResult#toString()}</li>
     * </ol>
     *
     * <p>All exceptions are caught and translated into user-friendly console messages.
     * Error output is directed to {@code System.out} (not {@code System.err}) for
     * clean, unified console output.</p>
     *
     * @param args command-line arguments (not used by this application)
     */
    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Enter your Date of Birth (DD/MM/YYYY): ");
            String dateOfBirth = scanner.nextLine();

            DateValidator dateValidator = new DateValidator();
            var validatedDate = dateValidator.validate(dateOfBirth);

            AgeCalculatorService ageCalculatorService = new AgeCalculatorService();
            AgeResult ageResult = ageCalculatorService.calculateAge(validatedDate);

            System.out.println(ageResult.toString());
        } catch (InvalidDateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (FutureDateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Error: An unexpected error occurred. " + e.getMessage());
        }
    }
}
