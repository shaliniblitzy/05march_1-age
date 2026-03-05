import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

/**
 * Validation utility for date-of-birth input parsing.
 *
 * <p>This stateless utility class is responsible for parsing and validating
 * date-of-birth strings in {@code DD/MM/YYYY} format using strict date resolution.
 * It follows the Separation of Concerns principle — this class ONLY handles
 * validation and parsing, delegating age computation to {@code AgeCalculator}.</p>
 *
 * <p>Uses {@link ResolverStyle#STRICT} with the {@code uuuu} proleptic-year pattern
 * to ensure that logically invalid calendar dates (e.g., 31/02/2020, 29/02/2019)
 * are rejected with a {@link DateTimeParseException} rather than silently adjusted.</p>
 */
public class DateValidator {

    // Uses ResolverStyle.STRICT with 'uuuu' (proleptic year) to reject invalid dates.
    // 'yyyy' means year-of-era and requires an era context (AD/BC); it fails with
    // STRICT mode on valid modern dates because no era field is present in the input.
    // 'uuuu' means proleptic year (signed integer, no era needed) and works correctly
    // with STRICT mode for all valid dates.
    //
    // ResolverStyle.STRICT is mandatory because the default ResolverStyle.SMART
    // silently adjusts impossible dates (e.g., February 30 becomes February 28).
    // STRICT mode throws DateTimeParseException for invalid calendar dates, which
    // is required to satisfy the user's requirement of handling invalid dates like
    // 31/02/2020.
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/uuuu")
            .withResolverStyle(ResolverStyle.STRICT);

    /**
     * Parses and validates a date-of-birth string in {@code DD/MM/YYYY} format.
     *
     * <p>This method performs two levels of validation:</p>
     * <ol>
     *   <li><strong>Format and calendar validation:</strong> The input string is parsed
     *       using a {@link DateTimeFormatter} configured with {@link ResolverStyle#STRICT}.
     *       Malformed strings (e.g., {@code "abc"}, {@code ""}, {@code "15-06-1990"}) and
     *       logically invalid calendar dates (e.g., {@code "31/02/2020"}, {@code "29/02/2019"},
     *       {@code "00/06/1990"}, {@code "15/13/1990"}) will cause a
     *       {@link DateTimeParseException} to be thrown.</li>
     *   <li><strong>Future date validation:</strong> If the parsed date is after today's date,
     *       an {@link IllegalArgumentException} is thrown with a descriptive message.</li>
     * </ol>
     *
     * @param dobString the date-of-birth string to parse, expected in {@code DD/MM/YYYY} format
     *                  (e.g., {@code "15/06/1990"}, {@code "29/02/2000"})
     * @return a valid {@link LocalDate} representing the parsed date of birth
     * @throws DateTimeParseException   if the input string is malformed or represents an
     *                                   invalid calendar date
     * @throws IllegalArgumentException if the parsed date is in the future
     */
    public static LocalDate parseAndValidate(String dobString) {
        // Parse the input string using the strict formatter.
        // DateTimeParseException is thrown automatically for:
        //   - Malformed strings (wrong format, wrong separators, non-numeric content)
        //   - Invalid calendar dates (day/month out of range, Feb 29 in non-leap years)
        // This exception is NOT caught here — it propagates to the caller (Main.java).
        LocalDate dob = LocalDate.parse(dobString, FORMATTER);

        // Validate that the parsed date is not in the future.
        // A date of birth cannot logically be after the current date.
        if (dob.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be a future date.");
        }

        return dob;
    }
}
