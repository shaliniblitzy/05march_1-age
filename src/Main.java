import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

// Application entry point for Age Calculator.
// Orchestrates user interaction through Scanner, delegates date validation to
// DateValidator and age computation to AgeCalculator, and handles all exceptions
// with meaningful, user-friendly error messages via try-catch-finally.
public class Main {

    /**
     * Main entry point for the Age Calculator console application.
     *
     * <p>Execution flow:</p>
     * <ol>
     *   <li>Prompts the user to enter their date of birth in DD/MM/YYYY format</li>
     *   <li>Delegates input validation to {@code DateValidator.parseAndValidate()}</li>
     *   <li>Delegates age computation to {@code AgeCalculator.calculateAge()}</li>
     *   <li>Displays the result via {@code AgeResult.toString()}</li>
     * </ol>
     *
     * <p>Exception handling catches three categories of errors:</p>
     * <ul>
     *   <li>{@code DateTimeParseException} — malformed or logically invalid dates</li>
     *   <li>{@code IllegalArgumentException} — future date-of-birth</li>
     *   <li>{@code Exception} — any other unexpected runtime error</li>
     * </ul>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.print("Enter your Date of Birth (DD/MM/YYYY): ");
            String input = scanner.nextLine();

            // Validate and parse the date-of-birth input string.
            // DateValidator.parseAndValidate() throws DateTimeParseException for
            // malformed/invalid dates and IllegalArgumentException for future dates.
            LocalDate dob = DateValidator.parseAndValidate(input);

            // Calculate the exact age in years, months, and days.
            // AgeCalculator.calculateAge() returns an AgeResult encapsulating the breakdown.
            AgeResult result = AgeCalculator.calculateAge(dob);

            // Display the formatted age result.
            // AgeResult.toString() produces: "Your age is X years, Y months, and Z days."
            System.out.println(result);
        } catch (DateTimeParseException e) {
            // Handles malformed format (e.g., "abc", "15-06-1990", empty string)
            // and logically invalid calendar dates (e.g., "31/02/2020", "29/02/2019",
            // "00/06/1990", "15/13/1990")
            System.out.println("Error: Invalid date format or impossible date. Please enter a valid date in DD/MM/YYYY format.");
        } catch (IllegalArgumentException e) {
            // Handles future date-of-birth rejection from DateValidator.
            // The exception message is: "Date of birth cannot be a future date."
            System.out.println(e.getMessage());
        } catch (Exception e) {
            // Catches any other unexpected runtime errors for graceful degradation
            System.out.println("Error: An unexpected error occurred. " + e.getMessage());
        } finally {
            // Ensure the Scanner resource is always closed to prevent resource leaks
            scanner.close();
        }
    }
}
