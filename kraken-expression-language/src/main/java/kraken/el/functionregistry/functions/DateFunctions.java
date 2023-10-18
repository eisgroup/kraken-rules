/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package kraken.el.functionregistry.functions;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

import kraken.el.ExpressionEvaluationException;
import kraken.el.FunctionContextHolder;
import kraken.el.date.DateCalculator;
import kraken.el.date.DateCalculator.DateField;
import kraken.el.date.DateCalculator.DateTimeField;
import kraken.el.functionregistry.Example;
import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionDocumentation;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.GenericType;
import kraken.el.functionregistry.LibraryDocumentation;
import kraken.el.functionregistry.Native;
import kraken.el.functionregistry.NotNull;
import kraken.el.functionregistry.ParameterDocumentation;
import kraken.el.functionregistry.ParameterType;

/**
 * Library that contains functions for working with Dates
 *
 * @author mulevicius
 */
@SuppressWarnings("squid:S1118")
@LibraryDocumentation(
    name = "Date",
    description = "Functions for operating with Date and DateTime values.",
    since = "1.0.28"
)
@Native
public class DateFunctions implements FunctionLibrary {

    private static final Pattern ISO_DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    private static final Pattern ISO_DATETIME_NO_OFFSET = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$");

    private static final DateCalculator dateCalculator = DateCalculator.getInstance();

    @FunctionDocumentation(
        description = "Create a date from ISO String.",
        throwsError = "if string does not match date pattern 'YYYY-MM-DD'",
        example = {
            @Example("Date('2011-11-11')"),
            @Example(value = "Date('abc')", validCall = false),
            @Example(value = "Date(null)", validCall = false),
        }
    )
    @ExpressionFunction("Date")
    public static LocalDate date(
        @NotNull
        @ParameterDocumentation(
            name = "dateString",
            description = "ISO string"
        ) String date
    ) {
        try {
            if(ISO_DATETIME_NO_OFFSET.matcher(date).matches()) {
                return dateCalculator.toDate(dateCalculator.createDateTime(date, zoneId()), zoneId());
            }
            if(ISO_DATE.matcher(date).matches()) {
                return dateCalculator.createDate(date);
            }
            return dateCalculator.toDate(dateCalculator.createDateTime(date), zoneId());
        } catch (DateTimeParseException e) {
            throw new ExpressionEvaluationException("Cannot parse date from: " + date, e);
        }
    }

    @FunctionDocumentation(
        description = "Create a datetime from ISO String.",
        throwsError = "if string does not match datetime pattern 'YYYY-MM-DDThh:mm:ssZ'",
        example = {
            @Example("DateTime('2011-11-11T10:10:10Z')"),
            @Example(value = "DateTime('abc')", validCall = false),
            @Example(value = "DateTime(null)", validCall = false),
        }
    )
    @ExpressionFunction("DateTime")
    public static LocalDateTime dateTime(
        @NotNull
        @ParameterDocumentation(
            name = "dateTimeString",
            description = "ISO string"
        ) String dateTime
    ) {
        try {
            if(ISO_DATETIME_NO_OFFSET.matcher(dateTime).matches()) {
                return dateCalculator.createDateTime(dateTime, zoneId());
            }
            if(ISO_DATE.matcher(dateTime).matches()) {
                return dateCalculator.toDateTime(dateCalculator.createDate(dateTime), zoneId());
            }
            return dateCalculator.createDateTime(dateTime);
        } catch (DateTimeParseException e) {
            throw new ExpressionEvaluationException("Cannot parse date time from: " + dateTime, e);
        }
    }

    @FunctionDocumentation(
        description = "Convert datetime to date by discarding time.",
        example = {
            @Example("AsDate(2011-11-11T10:10:10Z)"),
            @Example("AsDate(txEffectiveDate)"),
        }
    )
    @ExpressionFunction("AsDate")
    public static LocalDate asDate(@NotNull @ParameterDocumentation(name = "time") LocalDateTime time) {
        return dateCalculator.toDate(time, zoneId());
    }

    @FunctionDocumentation(
        description = "Convert date to datetime by adding zero time.",
        example = {
            @Example("AsTime(2011-11-11)"),
            @Example("AsTime(termEffectiveDate)"),
        }
    )
    @ExpressionFunction("AsTime")
    public static LocalDateTime asTime(@NotNull @ParameterDocumentation(name = "date") LocalDate date) {
        return dateCalculator.toDateTime(date, zoneId());
    }

    @FunctionDocumentation(
        description = "Resolve to the current date"
    )
    @ExpressionFunction("Today")
    public static LocalDate today() {
        return dateCalculator.today(zoneId());
    }

    @FunctionDocumentation(
        description = "Resolve to the current time."
    )
    @ExpressionFunction("Now")
    public static LocalDateTime now() {
        return dateCalculator.now();
    }

    @FunctionDocumentation(
        description = "Add or remove years from the date or datetime. "
            + "If the day of month is invalid for the year, then it is changed to the last valid day of the month.",
        throwsError = "if first parameter is not Date",
        example = {
            @Example(value = "PlusYears(2011-11-11, 1)", result = "2012-11-11"),
            @Example(value = "PlusYears(2011-11-11, -1)", result = "2010-11-11"),
            @Example(value = "PlusYears(2011-11-11T01:01:01Z, 1)", result = "2012-11-11T01:01:01Z"),
            @Example(value = "PlusYears('abc', 1)", validCall = false),
            @Example(value = "PlusYears(1, 1)", validCall = false),
            @Example(value = "PlusYears(null, 1)", validCall = false)
        }
    )
    @ExpressionFunction(
        value = "PlusYears",
        genericTypes = @GenericType(generic = "T", bound = "Date | DateTime")
    )
    public static <T> T plusYears(
        @NotNull @ParameterDocumentation(name = "dateOrDatetime", description = "Date or DateTime typeof parameter") T date,
        @NotNull @ParameterDocumentation(name = "numberOfYears", description = "can be positive or negative number") Number years
    ) {
        if (date instanceof LocalDate) {
            return (T) dateCalculator.addDateField((LocalDate) date, DateField.YEAR, years.longValue());
        }
        if (date instanceof LocalDateTime) {
            return (T) dateCalculator.addDateTimeField((LocalDateTime) date, DateTimeField.YEAR, years.longValue(), zoneId());
        }
        throw new ExpressionEvaluationException(
            "Function 'PlusYears' accepts as a first parameter instance of 'LocalDate' or 'LocalDateTime'"
        );
    }

    @FunctionDocumentation(
        description = "Add or remove days from date or datetime.",
        example = {
            @Example(value = "PlusDays(2011-11-11, 1)", result = "2011-11-12"),
            @Example(value = "PlusDays(2011-11-11, -1)", result = "2011-11-10"),
            @Example(value = "PlusDays(2011-11-11T01:01:01Z, 1)", result = "2011-11-12T01:01:01Z"),
            @Example(value = "PlusDays('abc', 1)", validCall = false),
            @Example(value = "PlusDays(1, 1)", validCall = false),
            @Example(value = "PlusDays(null, 1)", validCall = false)
        }
    )
    @ExpressionFunction(
        value = "PlusDays",
        genericTypes = @GenericType(generic = "T", bound = "Date | DateTime")
    )
    public static <T> T plusDays(
        @NotNull @ParameterDocumentation(name = "dateOrDatetime", description = "Date or DateTime typeof parameter") T date,
        @NotNull @ParameterDocumentation(name = "numberOfDays", description = "can be positive or negative number") Number days
    ) {
        if (date instanceof LocalDate) {
            return (T) dateCalculator.addDateField((LocalDate) date, DateField.DAY_OF_MONTH, days.longValue());
        }
        if (date instanceof LocalDateTime) {
            return (T) dateCalculator.addDateTimeField((LocalDateTime) date, DateTimeField.DAY_OF_MONTH, days.longValue(), zoneId());
        }
        throw new ExpressionEvaluationException(
            "Function 'PlusDays' accepts as a first parameter instance of 'LocalDate' or 'LocalDateTime'"
        );
    }

    @FunctionDocumentation(
        description = "Add or remove months from the date or datetime. "
            + "If the day of month is invalid for the year, then it is changed to the last valid day of the month.",
        throwsError = "if parameter is not Date or DateTime",
        example = {
            @Example(value = "PlusMonths(2011-11-11, 1)", result = "2011-12-11"),
            @Example(value = "PlusMonths(2011-11-11, -1)", result = "2011-10-11"),
            @Example(value = "PlusMonths(2011-11-11T01:01:01Z, 1)", result = "2011-12-11T01:01:01Z"),
            @Example(value = "PlusMonths('abc', 1)", validCall = false),
            @Example(value = "PlusMonths(1, 1)", validCall = false),
            @Example(value = "PlusMonths(null, 1)", validCall = false)
        }
    )
    @ExpressionFunction(
        value = "PlusMonths",
        genericTypes = @GenericType(generic = "T", bound = "Date | DateTime")
    )
    public static <T> T plusMonths(
        @NotNull @ParameterDocumentation(name = "dateOrDatetime", description = "Date or DateTime typeof parameter") T date,
        @NotNull @ParameterDocumentation(name = "numberOfMonths", description = "can be positive or negative number") Number months
    ) {
        if (date instanceof LocalDate) {
            return (T) dateCalculator.addDateField((LocalDate) date, DateField.MONTH, months.longValue());
        }
        if (date instanceof LocalDateTime) {
            return (T) dateCalculator.addDateTimeField((LocalDateTime) date, DateTimeField.MONTH, months.longValue(), zoneId());
        }
        throw new ExpressionEvaluationException(
            "Function 'PlusMonths' accepts as a first parameter instance of 'LocalDate' or 'LocalDateTime'"
        );
    }

    @FunctionDocumentation(
        description = "Check if the date is between two dates",
        throwsError = "if any of parameters is not a number",
        example = {
            @Example("IsDateBetween(AsDate(TermDetails.termEffectiveDate), 2011-11-11, Today())"),
            @Example(value = "IsDateBetween(2011-10-01, 2011-12-01, 2011-11-11)", result = "true"),
            @Example(value = "IsDateBetween(null, 2011-12-01, 2011-11-11)", validCall = false),
            @Example(value = "IsDateBetween(2011-10-01, null, 2011-11-11)", validCall = false),
            @Example(value = "IsDateBetween(2011-10-01, 2011-12-01, null)", validCall = false),
        }
    )
    @ExpressionFunction("IsDateBetween")
    public static Boolean isDateBetween(
        @NotNull @ParameterDocumentation(name = "date", description = "date to be between dates") LocalDate of,
        @NotNull @ParameterDocumentation(name = "start", description = "start of the range") LocalDate start,
        @NotNull @ParameterDocumentation(name = "end", description = "end of the range") LocalDate end
    ) {
        return of.isEqual(start) || of.isEqual(end) || (of.isBefore(end) && of.isAfter(start));
    }

    @FunctionDocumentation(
        throwsError = "if any of parameters is not a number",
        description = "Create a date from the year, month, and day.",
        example = {
            @Example("Date(2011, 12, 31)")
        }
    )
    @ExpressionFunction("Date")
    public static LocalDate date(
        @NotNull @ParameterDocumentation(name = "year") Number year,
        @NotNull @ParameterDocumentation(name = "month") Number month,
        @NotNull @ParameterDocumentation(name = "day") Number day
    ) {
        validateYear(year);
        validateMonth(month);
        validateDay(day);

        return dateCalculator.createDate(year.intValue(), month.intValue(), day.intValue());
    }

    @FunctionDocumentation(
        description = "Get the day of the month from date or date time.",
        throwsError = "if parameter is not Date or DateTime",
        example = {
            @Example(value = "GetDay(2011-11-11)", result = "11"),
            @Example(value = "GetDay(2011-11-11T01:01:01Z)", result = "11"),
            @Example(value = "GetDay(null)", validCall = false),
            @Example(value = "GetDay(1)", validCall = false),
        }
    )
    @ExpressionFunction("GetDay")
    public static Integer getDay(
        @NotNull
        @ParameterType("Date | DateTime")
        @ParameterDocumentation(name = "dateOrDatetime", description = "number of day in month starting from 1")
            Object dateOrDatetime
    ) {
        if (dateOrDatetime instanceof LocalDate) {
            return dateCalculator.getDateField((LocalDate)dateOrDatetime, DateField.DAY_OF_MONTH);
        }
        if (dateOrDatetime instanceof LocalDateTime) {
            return dateCalculator.getDateTimeField((LocalDateTime)dateOrDatetime, DateTimeField.DAY_OF_MONTH, zoneId());
        }
        throw new ExpressionEvaluationException("Day can be extracted only from date or date time");
    }

    @FunctionDocumentation(
        throwsError = "if parameter is not Date or DateTime",
        description = "Get the month number from date or date time.",
        example = {
            @Example(value = "GetMonth(2011-11-11)", result = "11"),
            @Example(value = "GetMonth(null)", validCall = false),
            @Example(value = "GetMonth(1)", validCall = false),
        }
    )
    @ExpressionFunction("GetMonth")
    public static Integer getMonth(
        @NotNull
        @ParameterType("Date | DateTime")
        @ParameterDocumentation(name = "dateOrDatetime", description = "number of day in month starting from 1")
            Object dateOrDatetime
    ) {
        if (dateOrDatetime instanceof LocalDate) {
            return dateCalculator.getDateField((LocalDate)dateOrDatetime, DateField.MONTH);
        }
        if (dateOrDatetime instanceof LocalDateTime) {
            return dateCalculator.getDateTimeField((LocalDateTime)dateOrDatetime, DateTimeField.MONTH, zoneId());
        }
        throw new ExpressionEvaluationException("Month can be extracted only from date or date time");
    }

    @FunctionDocumentation(
        throwsError = "if parameter is not Date or DateTime",
        description = "Get the year number from date or datetime.",
        example = {
            @Example(value = "GetYear(2011-11-11)", result = "11"),
            @Example(value = "GetYear(null)", validCall = false),
            @Example(value = "GetYear(1)", validCall = false),
        }
    )
    @ExpressionFunction("GetYear")
    public static Integer getYear(
        @NotNull
        @ParameterType("Date | DateTime")
        @ParameterDocumentation(name = "dateOrDatetime", description = "number of day in month starting from 1")
            Object dateOrDatetime
    ) {
        if (dateOrDatetime instanceof LocalDate) {
            return dateCalculator.getDateField((LocalDate)dateOrDatetime, DateField.YEAR);
        }
        if (dateOrDatetime instanceof LocalDateTime) {
            return dateCalculator.getDateTimeField((LocalDateTime)dateOrDatetime, DateTimeField.YEAR, zoneId());
        }
        throw new ExpressionEvaluationException("Year can be extracted only from date or date time");
    }

    @FunctionDocumentation(
        throwsError = "if pattern is invalid",
        description = "Formats date according to the format passed as a parameter. "
            + "Format can contain Y, M and D characters to denote year, month or day respectively.",
        example = {
            @Example(value = "Format(2012-11-10, \"YYYY/MM/DD\")", result = "2112/11/10"),
            @Example(value = "Format(2012-11-10, null)", result = "2012-11-10"),
            @Example(value = "Format(null, \"MM/YY/DD\")", validCall = false),
            @Example(value = "Format(null, \"abc\")", validCall = false),
        }
    )
    @ExpressionFunction("Format")
    public static String format(
        @NotNull
        @ParameterDocumentation(name = "date", description = "to parse to string")
            LocalDate date,
        @ParameterDocumentation(name = "format", description = "if null is passed, then default format is \"YYYY-MM-DD\"")
            String format
    ) {
        String fxFormat = format == null
            ? "yyyy-MM-dd"
            : format.replaceAll("D", "d").replaceAll("Y", "y");
        return date.format(DateTimeFormatter.ofPattern(fxFormat));
    }

    @FunctionDocumentation(
        description = "Calculate months between two dates."
            + "The output of this function is always positive, it doesn't matter which parameter is a start date.",
        since = "1.0.34",
        example = {
            @Example(value = "NumberOfMonthsBetween(2011-11-11, 2011-12-11)", result = "1"),
            @Example(value = "NumberOfMonthsBetween(2011-11-11, 2011-12-10)", result = "0"),
            @Example(value = "NumberOfMonthsBetween(2020-01-30, 2020-02-29)", result = "0")
        }
    )
    @ExpressionFunction("NumberOfMonthsBetween")
    public static Long numberOfMonthsBetween(
        @NotNull @ParameterDocumentation(name = "date1") LocalDate d1,
        @NotNull @ParameterDocumentation(name = "date2") LocalDate d2
    ) {
        return Math.abs(dateCalculator.difference(d1, d2, DateField.MONTH));
    }

    @FunctionDocumentation(
        description = "Calculate years between two dates."
            + "The output of this function is always positive, it doesn't matter which parameter is a start date.",
        example = {
            @Example(value = "NumberOfYearsBetween(2010-11-11, 2011-11-11)", result = "1"),
            @Example(value = "NumberOfYearsBetween(2010-11-11, 2011-11-10)", result = "0"),
            @Example(value = "NumberOfYearsBetween(2020-02-29, 2021-02-28)", result = "0")
        },
        since = "1.0.34"
    )
    @ExpressionFunction("NumberOfYearsBetween")
    public static Long numberOfYearsBetween(
        @NotNull @ParameterDocumentation(name = "date1") LocalDate d1,
        @NotNull @ParameterDocumentation(name = "date2") LocalDate d2
    ) {
        return Math.abs(dateCalculator.difference(d1, d2, DateField.YEAR));
    }

    @FunctionDocumentation(
        description = "Calculate days between two dates."
            + "The output of this function is always positive, it doesn't matter which parameter is a start date.",
        example = {
            @Example(value = "NumberOfDaysBetween(2011-11-11, 2011-11-12)", result = "1"),
            @Example(value = "NumberOfDaysBetween(2011-11-11, 2011-11-10)", result = "1"),
        },
        since = "1.0.34"
    )
    @ExpressionFunction("NumberOfDaysBetween")
    public static Long numberOfDaysBetween(
        @NotNull @ParameterDocumentation(name = "date1") LocalDate d1,
        @NotNull @ParameterDocumentation(name = "date2") LocalDate d2
    ) {
        return Math.abs(dateCalculator.difference(d1, d2, DateField.DAY_OF_MONTH));
    }

    @FunctionDocumentation(
        description = "Returns Date or DateTime with modified year value. "
            + "Year value must be between 1 and 9999. "
            + "If the day of month is invalid for the year, then it is changed to the last valid day of the month.",
        throwsError = "Throws error if parameter type is not Date or DateTime.",
        example = {
            @Example(value = "WithYear(2011-11-11, 2000)", result = "2000-11-11"),
            @Example(value = "WithYear(2000-02-29, 2001)", result = "2001-02-28"),
        },
        since = "1.30.0"
    )
    @ExpressionFunction(
        value = "WithYear",
        genericTypes = @GenericType(generic = "T", bound = "Date | DateTime")
    )
    public static <T> T withYear(
        @NotNull @ParameterDocumentation(name = "value") T value,
        @NotNull @ParameterDocumentation(name = "year") Number year
    ) {
        validateYear(year);

        if(value instanceof LocalDate) {
            return (T) dateCalculator.withDateField((LocalDate) value, DateField.YEAR, year.intValue());
        }
        if(value instanceof LocalDateTime) {
            return (T) dateCalculator.withDateTimeField((LocalDateTime) value, DateTimeField.YEAR, year.intValue(), zoneId());
        }
        throw new ExpressionEvaluationException("Year can only be modified for Date or DateTime, but parameter was: " + value);
    }

    @FunctionDocumentation(
        description = "Returns Date or DateTime with modified month value. "
            + "Month value must be between 1 (January) and 12 (December)." 
            + "If the day of month is invalid for the year, then it is changed to the last valid day of the month.",
        throwsError = "if parameter type is not Date or DateTime, "
            + "or if month value is smaller than 1 or larger than 12.",
        example = {
            @Example(value = "WithMonth(2011-11-11, 1)", result = "2011-12-11"),
            @Example(value = "WithMonth(2000-01-30, 2)", result = "2000-02-29"),
            @Example(value = "WithMonth(2011-11-11, 0)", validCall = false)
        },
        since = "1.30.0"
    )
    @ExpressionFunction(
        value = "WithMonth",
        genericTypes = @GenericType(generic = "T", bound = "Date | DateTime")
    )
    public static <T> T withMonth(
        @NotNull @ParameterDocumentation(name = "value") T value,
        @NotNull @ParameterDocumentation(name = "month") Number month
    ) {
        validateMonth(month);

        if(value instanceof LocalDate) {
            return (T) dateCalculator.withDateField((LocalDate) value, DateField.MONTH, month.intValue());
        }
        if(value instanceof LocalDateTime) {
            return (T) dateCalculator.withDateTimeField((LocalDateTime) value, DateTimeField.MONTH, month.intValue(), zoneId());
        }
        throw new ExpressionEvaluationException("Month can only be modified for Date or DateTime, but parameter was: " + value);
    }

    @FunctionDocumentation(
        description = "Returns Date or DateTime with modified day of month value. "
            + "Day must be a valid day of month starting from 1.",
        throwsError = "if parameter type is not Date or DateTime, or if month does not have such a day.",
        example = {
            @Example(value = "WithDay(2011-11-11, 1)", result = "2011-11-01"),
            @Example(value = "WithDay(2011-11-11, 0)", validCall = false),
            @Example(value = "WithDay(2000-02-02, 30)", validCall = false),
        },
        since = "1.30.0"
    )
    @ExpressionFunction(
        value = "WithDay",
        genericTypes = @GenericType(generic = "T", bound = "Date | DateTime")
    )
    public static <T> T withDay(
        @NotNull @ParameterDocumentation(name = "value") T value,
        @NotNull @ParameterDocumentation(name = "day") Number day
    ) {
        validateDay(day);

        try {
            if(value instanceof LocalDate) {
                return (T) dateCalculator.withDateField((LocalDate) value, DateField.DAY_OF_MONTH, day.intValue());
            }
            if(value instanceof LocalDateTime) {
                return (T) dateCalculator.withDateTimeField((LocalDateTime) value, DateTimeField.DAY_OF_MONTH, day.intValue(), zoneId());
            }
        } catch (DateTimeException e) {
            String template = "Cannot set day '%s' in date '%s' because such day does not exist.";
            String message = String.format(template, day, value);
            throw new ExpressionEvaluationException(message, e);
        }
        throw new ExpressionEvaluationException("Day can only be modified for Date or DateTime, but parameter was: " + value);
    }

    private static void validateYear(Number year) {
        int yearValue = year.intValue();
        if(yearValue < 1 || yearValue > 9999) {
            throw new ExpressionEvaluationException("Year value must be from 1 to 9999, but was: " + yearValue);
        }
    }

    private static void validateMonth(Number month) {
        int monthValue = month.intValue();
        if(monthValue < 1 || monthValue > 12) {
            throw new ExpressionEvaluationException(
                "Month value must be from 1 (January) to 12 (December), but was: " + monthValue
            );
        }
    }

    private static void validateDay(Number day) {
        int dayValue = day.intValue();
        if(dayValue < 1 || dayValue > 31) {
            throw new ExpressionEvaluationException("Day value must be from 1 to 31, but was: " + dayValue);
        }
    }

    private static ZoneId zoneId() {
        return FunctionContextHolder.getFunctionContext().getZoneId();
    }
}
