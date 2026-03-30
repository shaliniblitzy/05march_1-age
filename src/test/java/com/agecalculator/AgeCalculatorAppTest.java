package com.agecalculator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.time.LocalDate;

/**
 * Integration test for the Age Calculator application.
 *
 * <p>Tests the complete end-to-end workflow by simulating console input via
 * {@link System#setIn(InputStream)} and capturing console output via
 * {@link System#setOut(PrintStream)}. Verifies the entire pipeline:
 * input &rarr; validation &rarr; calculation &rarr; formatting &rarr; output.</p>
 *
 * <p>Each test method follows the same pattern:</p>
 * <ol>
 *     <li>Simulate user input by wrapping a string in a {@link ByteArrayInputStream}
 *         and assigning it to {@code System.in}</li>
 *     <li>Invoke {@link AgeCalculatorApp#main(String[])} to execute the full application
 *         workflow</li>
 *     <li>Capture the console output from a {@link ByteArrayOutputStream} that was
 *         assigned to {@code System.out}</li>
 *     <li>Assert expected patterns in the captured output using
 *         {@code assertTrue(output.contains(...))} to avoid hardcoding date-dependent
 *         values</li>
 * </ol>
 *
 * <p><strong>Important design decisions:</strong></p>
 * <ul>
 *     <li>Age values are NOT hardcoded in assertions (except for the "today as DOB"
 *         boundary case) because {@code LocalDate.now()} changes daily</li>
 *     <li>{@code System.in} and {@code System.out} are restored after each test via
 *         {@code @AfterEach} to prevent test pollution</li>
 *     <li>Error message strings must exactly match the production code in
 *         {@code DateValidator} and {@code AgeCalculatorApp}</li>
 * </ul>
 *
 * @author Age Calculator Application
 * @version 1.0.0
 * @see AgeCalculatorApp
 */
class AgeCalculatorAppTest {

    /**
     * Stores the original {@code System.in} stream to restore after each test.
     * This prevents one test's input simulation from affecting subsequent tests.
     */
    private final InputStream originalIn = System.in;

    /**
     * Stores the original {@code System.out} stream to restore after each test.
     * This ensures console output is properly redirected back after test completion.
     */
    private final PrintStream originalOut = System.out;

    /**
     * Buffer that captures all output written to {@code System.out} during a test.
     * Initialized fresh for each test in {@link #setUp()} to prevent output leakage
     * between tests.
     */
    private ByteArrayOutputStream capturedOutput;

    /**
     * Sets up the output capture stream before each test method.
     *
     * <p>Creates a fresh {@link ByteArrayOutputStream} and redirects {@code System.out}
     * to write into it, so all console output during the test can be inspected
     * via {@link #getCapturedOutput()}.</p>
     */
    @BeforeEach
    void setUp() {
        capturedOutput = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capturedOutput));
    }

    /**
     * Restores the original {@code System.in} and {@code System.out} streams
     * after each test method.
     *
     * <p>This is critical to prevent test pollution — without this teardown,
     * a test that sets {@code System.in} to a {@link ByteArrayInputStream}
     * could cause subsequent tests to read from an exhausted stream.</p>
     */
    @AfterEach
    void tearDown() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    /**
     * Simulates console input by wrapping the given string in a
     * {@link ByteArrayInputStream} and assigning it to {@code System.in}.
     *
     * <p>The input string should include a trailing newline ({@code \n})
     * to simulate the user pressing Enter after typing their input.</p>
     *
     * @param input the simulated user input string (e.g., {@code "15/08/1998\n"})
     */
    private void provideInput(String input) {
        ByteArrayInputStream testIn = new ByteArrayInputStream(input.getBytes());
        System.setIn(testIn);
    }

    /**
     * Returns the console output captured during the current test, trimmed
     * of leading and trailing whitespace.
     *
     * @return the captured output string from {@code System.out}
     */
    private String getCapturedOutput() {
        return capturedOutput.toString().trim();
    }

    /**
     * Tests the complete end-to-end workflow with a valid Date of Birth.
     *
     * <p>Simulates the input {@code "15/08/1998"} and verifies that the application
     * produces output containing the expected prompt, age format pattern, and
     * optional enhancement output (total months, total days, next birthday countdown).</p>
     *
     * <p>Age values are NOT asserted because they change daily as
     * {@code LocalDate.now()} advances. Instead, the output FORMAT is validated
     * using substring containment checks.</p>
     */
    @Test
    @DisplayName("End-to-end test with valid DOB produces age output")
    void testValidDobEndToEnd() {
        provideInput("15/08/1998\n");

        AgeCalculatorApp.main(new String[]{});

        String output = getCapturedOutput();

        // Verify the prompt was displayed
        assertTrue(output.contains("Enter your Date of Birth (DD/MM/YYYY):"),
                "Output should contain the input prompt");

        // Verify the age output format pattern (not exact values)
        assertTrue(output.contains("Your age is"),
                "Output should contain 'Your age is' prefix");
        assertTrue(output.contains("years,"),
                "Output should contain 'years,' component");
        assertTrue(output.contains("months, and"),
                "Output should contain 'months, and' component");
        assertTrue(output.contains("days."),
                "Output should contain 'days.' component");

        // Verify optional enhancement outputs are present
        assertTrue(output.contains("Total months:"),
                "Output should contain total months enhancement");
        assertTrue(output.contains("Total days:"),
                "Output should contain total days enhancement");
        assertTrue(output.contains("Days until next birthday:"),
                "Output should contain next birthday countdown enhancement");

        // Verify no error messages appear in successful output
        assertFalse(output.contains("Error:"),
                "Successful output should not contain error messages");
    }

    /**
     * Tests that an invalid format input (non-date string) produces an error message.
     *
     * <p>Simulates the input {@code "abc"} which does not match the DD/MM/YYYY pattern.
     * The {@code DateValidator} will throw an {@code InvalidDateException} with
     * the message "Invalid date format. Please use DD/MM/YYYY." which is displayed
     * by the application as "Error: Invalid date format. Please use DD/MM/YYYY."</p>
     */
    @Test
    @DisplayName("Invalid format input displays error message")
    void testInvalidFormatInput() {
        provideInput("abc\n");

        AgeCalculatorApp.main(new String[]{});

        String output = getCapturedOutput();

        // Verify the prompt was still displayed before error
        assertTrue(output.contains("Enter your Date of Birth (DD/MM/YYYY):"),
                "Output should contain the input prompt even on error");

        // Verify the exact error message for format errors
        assertTrue(output.contains("Error: Invalid date format. Please use DD/MM/YYYY."),
                "Output should contain format error message for non-date input");

        // Verify no age output appears on error
        assertFalse(output.contains("Your age is"),
                "Error output should not contain age calculation result");
    }

    /**
     * Tests that a future date input produces an error message.
     *
     * <p>Simulates the input {@code "01/01/2999"} which is a valid calendar date
     * but is far in the future. The {@code DateValidator} will reject it as a
     * future date and throw an {@code InvalidDateException} with the message
     * "Date of Birth cannot be a future date." which is displayed by the application
     * as "Error: Date of Birth cannot be a future date."</p>
     */
    @Test
    @DisplayName("Future date input displays error message")
    void testFutureDateInput() {
        provideInput("01/01/2999\n");

        AgeCalculatorApp.main(new String[]{});

        String output = getCapturedOutput();

        // Verify the prompt was still displayed before error
        assertTrue(output.contains("Enter your Date of Birth (DD/MM/YYYY):"),
                "Output should contain the input prompt even on error");

        // Verify the exact error message for future dates
        assertTrue(output.contains("Error: Date of Birth cannot be a future date."),
                "Output should contain future date error message");

        // Verify no age output appears on error
        assertFalse(output.contains("Your age is"),
                "Error output should not contain age calculation result");
    }

    /**
     * Tests that an impossible calendar date produces an error message.
     *
     * <p>Simulates the input {@code "31/02/2020"} which structurally matches
     * the DD/MM/YYYY pattern but represents an impossible date (February 31).
     * The {@code DateValidator} uses a regex pre-check to distinguish this from
     * a format error — since the input structurally matches {@code \d{2}/\d{2}/\d{4}},
     * it throws an {@code InvalidDateException} with the message
     * "Invalid date. Please enter a real calendar date."</p>
     */
    @Test
    @DisplayName("Impossible calendar date displays error message")
    void testImpossibleCalendarDate() {
        provideInput("31/02/2020\n");

        AgeCalculatorApp.main(new String[]{});

        String output = getCapturedOutput();

        // Verify the prompt was still displayed before error
        assertTrue(output.contains("Enter your Date of Birth (DD/MM/YYYY):"),
                "Output should contain the input prompt even on error");

        // Verify error message is displayed — impossible dates are detected by the
        // regex pre-check in DateValidator and produce a calendar-specific error
        assertTrue(output.contains("Error:"),
                "Output should contain an error prefix for impossible calendar date");
        assertTrue(output.contains("Invalid date. Please enter a real calendar date."),
                "Output should contain impossible calendar date error message");

        // Verify no age output appears on error
        assertFalse(output.contains("Your age is"),
                "Error output should not contain age calculation result");
    }

    /**
     * Tests the boundary case where today's date is provided as the Date of Birth.
     *
     * <p>When DOB equals today, the age should be exactly 0 years, 0 months, and 0 days.
     * This is the only test case where exact numeric values can be asserted because
     * the result is deterministic regardless of when the test runs.</p>
     *
     * <p>The test dynamically generates today's date in DD/MM/YYYY format using
     * {@link LocalDate#now()} to ensure the test remains valid on any execution date.</p>
     */
    @Test
    @DisplayName("Today's date as DOB produces zero age")
    void testTodayAsDob() {
        LocalDate today = LocalDate.now();
        String todayStr = String.format("%02d/%02d/%04d",
                today.getDayOfMonth(), today.getMonthValue(), today.getYear());

        provideInput(todayStr + "\n");

        AgeCalculatorApp.main(new String[]{});

        String output = getCapturedOutput();

        // Verify the prompt was displayed
        assertTrue(output.contains("Enter your Date of Birth (DD/MM/YYYY):"),
                "Output should contain the input prompt");

        // Verify the exact zero-age output — this is deterministic since DOB == today
        assertTrue(output.contains("Your age is 0 years, 0 months, and 0 days."),
                "Today's DOB should produce exactly 0 years, 0 months, and 0 days");

        // Verify optional enhancement outputs for zero age
        assertTrue(output.contains("Total months: 0"),
                "Total months for today's DOB should be 0");
        assertTrue(output.contains("Total days: 0"),
                "Total days for today's DOB should be 0");

        // Verify no error messages appear
        assertFalse(output.contains("Error:"),
                "Successful output should not contain error messages");
    }

    /**
     * Tests the end-to-end workflow with a leap year Date of Birth (February 29, 2000).
     *
     * <p>February 29, 2000 is a valid leap year date. The application must accept it
     * and produce a valid age output. This verifies that the entire pipeline handles
     * leap year birthdays correctly — from parsing through calculation to formatting.</p>
     *
     * <p>Age values are NOT asserted because they change daily. The test validates
     * that the output format is correct and no error messages appear.</p>
     */
    @Test
    @DisplayName("Leap year DOB (29/02/2000) produces valid age output")
    void testLeapYearDobEndToEnd() {
        provideInput("29/02/2000\n");

        AgeCalculatorApp.main(new String[]{});

        String output = getCapturedOutput();

        // Verify the prompt was displayed
        assertTrue(output.contains("Enter your Date of Birth (DD/MM/YYYY):"),
                "Output should contain the input prompt");

        // Verify the age output format pattern (not exact values)
        assertTrue(output.contains("Your age is"),
                "Output should contain 'Your age is' prefix for leap year DOB");
        assertTrue(output.contains("years,"),
                "Output should contain 'years,' component for leap year DOB");
        assertTrue(output.contains("months, and"),
                "Output should contain 'months, and' component for leap year DOB");
        assertTrue(output.contains("days."),
                "Output should contain 'days.' component for leap year DOB");

        // Verify optional enhancement outputs are present
        assertTrue(output.contains("Total months:"),
                "Output should contain total months enhancement for leap year DOB");
        assertTrue(output.contains("Total days:"),
                "Output should contain total days enhancement for leap year DOB");
        assertTrue(output.contains("Days until next birthday:"),
                "Output should contain next birthday countdown for leap year DOB");

        // Verify no error messages appear — leap year dates are valid
        assertFalse(output.contains("Error:"),
                "Leap year DOB should not produce any error messages");
    }
}
