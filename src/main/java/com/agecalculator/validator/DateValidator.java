package com.agecalculator.validator;

import java.time.LocalDate;

import com.agecalculator.util.DateParserUtil;
import com.agecalculator.exception.InvalidDateException;
import com.agecalculator.exception.FutureDateException;

/**
 * Validation orchestrator for Date of Birth input in the Age Calculator application.
 *
 * <p>This class coordinates two layers of input validation:</p>
 * <ol>
 *   <li><strong>Format and calendar validation</strong> — delegated to
 *       {@link DateParserUtil#parse(String)}, which enforces strict {@code DD/MM/YYYY}
 *       format compliance and rejects logically impossible calendar dates (e.g.,
 *       February 30, February 29 on non-leap years).</li>
 *   <li><strong>Temporal constraint validation</strong> — performed directly by this
 *       class, ensuring that the parsed date of birth is not after the current
 *       system date.</li>
 * </ol>
 *
 * <h3>Design Decisions</h3>
 * <ul>
 *   <li><strong>Stateless instance class</strong> — this class is instantiated by
 *       {@code AgeCalculatorApp} and holds no mutable state. The default no-arg
 *       constructor is sufficient.</li>
 *   <li><strong>Single Responsibility</strong> — this class handles ONLY validation
 *       orchestration. It does not parse dates (delegated to {@code DateParserUtil}),
 *       compute age (delegated to {@code AgeCalculatorService}), or perform I/O.</li>
 *   <li><strong>Transparent exception propagation</strong> — {@link InvalidDateException}
 *       thrown by {@code DateParserUtil.parse()} propagates directly to the caller
 *       without being caught or wrapped. {@link FutureDateException} is thrown
 *       explicitly when the temporal constraint is violated.</li>
 * </ul>
 *
 * <h3>Usage Example</h3>
 * <pre>{@code
 * DateValidator validator = new DateValidator();
 * try {
 *     LocalDate dob = validator.validate("15/03/1990");
 *     // dob is a validated LocalDate ready for age calculation
 * } catch (InvalidDateException e) {
 *     // Handle malformed or impossible date
 * } catch (FutureDateException e) {
 *     // Handle DOB in the future
 * }
 * }</pre>
 *
 * @see com.agecalculator.util.DateParserUtil
 * @see com.agecalculator.exception.InvalidDateException
 * @see com.agecalculator.exception.FutureDateException
 */
public class DateValidator {

    /**
     * Validates a date-of-birth string by parsing it into a {@link LocalDate}
     * and verifying that it does not represent a future date.
     *
     * <p>The validation process follows two sequential steps:</p>
     * <ol>
     *   <li><strong>Parsing</strong> — the raw string is parsed via
     *       {@link DateParserUtil#parse(String)}, which enforces the {@code DD/MM/YYYY}
     *       format with strict calendar resolution. If the string is {@code null},
     *       empty, malformed, or represents an impossible calendar date, an
     *       {@link InvalidDateException} is thrown and allowed to propagate
     *       directly to the caller.</li>
     *   <li><strong>Temporal check</strong> — the successfully parsed date is
     *       compared against the current system date via
     *       {@link LocalDate#isAfter(java.time.chrono.ChronoLocalDate)}. If the
     *       date of birth is strictly after today, a {@link FutureDateException}
     *       is thrown. Today's date itself is considered valid (age of 0 years,
     *       0 months, 0 days).</li>
     * </ol>
     *
     * @param dateStr the date-of-birth string to validate, expected in
     *                {@code DD/MM/YYYY} format (e.g., {@code "29/02/2000"})
     * @return a validated {@link LocalDate} representing the date of birth,
     *         guaranteed to be on or before the current system date
     * @throws InvalidDateException  if {@code dateStr} is {@code null}, empty,
     *                               does not match the expected format, or
     *                               represents a logically impossible calendar date
     * @throws FutureDateException   if the parsed date is strictly after the
     *                               current system date
     */
    public LocalDate validate(String dateStr) throws InvalidDateException, FutureDateException {
        LocalDate dob = DateParserUtil.parse(dateStr);

        if (dob.isAfter(LocalDate.now())) {
            throw new FutureDateException("Date of birth cannot be a future date.");
        }

        return dob;
    }
}
