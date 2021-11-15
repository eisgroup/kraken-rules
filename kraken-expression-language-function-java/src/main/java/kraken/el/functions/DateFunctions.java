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
package kraken.el.functions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import kraken.el.ast.builder.Literals;
import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.Native;
import kraken.el.functionregistry.NotNull;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.MONTHS;

/**
 * Library that contains functions for working with Dates in MVEL expressions
 *
 * @author mulevicius
 */
@SuppressWarnings("squid:S1118")
@Native
public class DateFunctions implements FunctionLibrary {

    @ExpressionFunction("Date")
    public static LocalDate date(@NotNull String date) {
        try {
            return getDate(date);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Cannot parse date from pattern: " + date, e);
        }
    }

    @ExpressionFunction("DateTime")
    public static LocalDateTime dateTime(@NotNull String dateTime) {
        try {
            return getDateTime(dateTime);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Cannot parse dateTime from pattern: " + dateTime, e);
        }
    }

    @ExpressionFunction("AsDate")
    public static LocalDate asDate(@NotNull LocalDateTime time) {
        return time.toLocalDate();
    }

    @ExpressionFunction("AsTime")
    public static LocalDateTime asTime(@NotNull LocalDate date) {
        return date.atStartOfDay();
    }

    @ExpressionFunction("Today")
    public static LocalDate today() {
        return LocalDate.now();
    }

    @ExpressionFunction("Now")
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    @ExpressionFunction("PlusYears")
    public static <T> T plusYears(@NotNull T date, @NotNull Number days) {
        if (date instanceof LocalDate) {
            return (T) ((LocalDate) date).plusYears(days.longValue());
        }
        if (date instanceof LocalDateTime) {
            return (T) ((LocalDateTime) date).plusYears(days.longValue());
        }
        throw new IllegalArgumentException(
                "Function 'PlusYears' accepts as a first parameter instance of 'LocalDate' or 'LocalDateTime'"
        );
    }

    @ExpressionFunction("PlusDays")
    public static <T> T plusDays(@NotNull T date, @NotNull Number days) {
        if (date instanceof LocalDate) {
            return (T) ((LocalDate) date).plusDays(days.longValue());
        }
        if (date instanceof LocalDateTime) {
            return (T) ((LocalDateTime) date).plusDays(days.longValue());
        }
        throw new IllegalArgumentException(
                "Function 'PlusDays' accepts as a first parameter instance of 'LocalDate' or 'LocalDateTime'"
        );
    }

    @ExpressionFunction("PlusMonths")
    public static <T> T plusMonths(@NotNull T date, @NotNull Number days) {
        if (date instanceof LocalDate) {
            return (T) ((LocalDate) date).plusMonths(days.longValue());
        }
        if (date instanceof LocalDateTime) {
            return (T) ((LocalDateTime) date).plusMonths(days.longValue());
        }
        throw new IllegalArgumentException(
                "Function 'PlusMonths' accepts as a first parameter instance of 'LocalDate' or 'LocalDateTime'"
        );
    }

    @SuppressWarnings("squid:S1166")
    private static LocalDate getDate(String date) {
        try {
            return Literals.getDate(date);
        } catch (DateTimeParseException ex) {
            return getDateTime(date).toLocalDate();
        }
    }

    @SuppressWarnings("squid:S1166")
    private static LocalDateTime getDateTime(String dateTime) {
        try {
            return Literals.getDateTime(dateTime);
        } catch (DateTimeParseException ex) {
            return parseNoOffset(dateTime);
        }
    }

    @SuppressWarnings("squid:S1166")
    private static LocalDateTime parseNoOffset(String dateTime) {
        try {
            return LocalDateTime.parse(dateTime);
        } catch (DateTimeParseException ex) {
            return LocalDate.parse(dateTime).atStartOfDay();
        }
    }

    @ExpressionFunction("IsDateBetween")
    public static Boolean isDateBetween(@NotNull LocalDate of, @NotNull LocalDate start, @NotNull LocalDate end) {
        return of.isEqual(start) || of.isEqual(end) || isDateBetweenNotInclusive(of, start, end);
    }

    /**
     * Creates date from year, month and day.
     *
     * @param year  starts from 1
     * @param month starts from 1
     * @param day   starts from 1
     * @return date
     */
    @ExpressionFunction("Date")
    public static LocalDate date(@NotNull Number year, @NotNull Number month, @NotNull Number day) {
        return LocalDate.of(year.intValue(), month.intValue(), day.intValue());
    }

    private static Boolean isDateBetweenNotInclusive(LocalDate of, LocalDate start, LocalDate end) {
        return of.isBefore(end) && of.isAfter(start);
    }

    /**
     * Gets day of month from date or date time
     *
     * @param date
     * @return number of day in month starting from 1
     * @throws IllegalStateException if parameter is not LocalDate or LocalDateTime
     */
    @ExpressionFunction("GetDay")
    public static Integer getDay(@NotNull Object date) {
        if (date instanceof LocalDate) {
            return ((LocalDate) date).getDayOfMonth();
        }
        if (date instanceof LocalDateTime) {
            return ((LocalDateTime) date).getDayOfMonth();
        }
        throw new IllegalStateException("Day can be extracted only from date or date time");
    }

    /**
     * Gets month number from date or date time
     *
     * @param date
     * @return number of month starting from 1
     * @throws IllegalStateException if parameter is not LocalDate or LocalDateTime
     */
    @ExpressionFunction("GetMonth")
    public static Integer getMonth(@NotNull Object date) {
        if (date instanceof LocalDate) {
            return ((LocalDate) date).getMonthValue();
        }
        if (date instanceof LocalDateTime) {
            return ((LocalDateTime) date).getMonthValue();
        }
        throw new IllegalStateException("Month can be extracted only from date or date time");
    }

    /**
     * Gets year from date or date time
     *
     * @param date
     * @return number of month starting from 1
     * @throws IllegalStateException if parameter is not LocalDate or LocalDateTime
     */
    @ExpressionFunction("GetYear")
    public static Integer getYear(@NotNull Object date) {
        if (date instanceof LocalDate) {
            return ((LocalDate) date).getYear();
        }
        if (date instanceof LocalDateTime) {
            return ((LocalDateTime) date).getYear();
        }
        throw new IllegalStateException("Year can be extracted only from date or date time");
    }

    /**
     * Formats date according format passed as a parameter.
     * ("YMD") characters cannot be used as delimiters, format cannot contain words with ("YMD" characters)
     * Year can be used in format as - Y
     * Month can be used in format as - M
     * Day can be used in format as - D
     *
     * @param date   to parse to string
     * @param format to parse date to string. Default is "YYYY-MM-DD"
     * @return string representation of date formatted by format from parameters.
     */
    @ExpressionFunction("Format")
    public static String format(@NotNull LocalDate date, String format) {
        String fxFormat = format == null
                ? "yyyy-MM-dd"
                : format.replaceAll("D", "d").replaceAll("Y", "y");
        return date.format(DateTimeFormatter.ofPattern(fxFormat));
    }

    /**
     * Calculates number of days between two dates.
     * The output of this function will always be positive. There is no difference
     * first or second parameter will be start date.
     *
     * @param d1 date
     * @param d2 date
     * @return
     */
    @ExpressionFunction("NumberOfDaysBetween")
    public static Long numberOfDaysBetween(@NotNull LocalDate d1, @NotNull LocalDate d2) {
        return Math.abs(DAYS.between(d1, d2));
    }

    /**
     * Calculates months between two dates.
     * The output of this function will always be positive. There is no difference
     * first or second parameter will be start date.
     * example:
     *     <p>
     *         NumberOfMonthsBetween(Date(2011,11,30), Date(2011,12,1)) // 0
     *         NumberOfMonthsBetween(Date(2012,11,16), Date(2012,12,16)) // 1
     *         NumberOfMonthsBetween(Date(2011,12,30), Date(2012,12,29)) // 11
     *     <p/>
     *
     * @param d1 date
     * @param d2 date
     * @return number of months between
     */
    @ExpressionFunction("NumberOfMonthsBetween")
    public static Long numberOfMonthsBetween(@NotNull LocalDate d1, @NotNull LocalDate d2) {
        return Math.abs(MONTHS.between(d1, d2));
    }

    /**
     * Calculates years between two dates.
     * The output of this function will always be positive. There is no difference
     * first or second parameter will be start date.
     * example:
     *     <p>
     *         NumberOfYearsBetween(Date(2011,12,31), Date(2012,1,1)) // 0
     *         NumberOfYearsBetween(Date(2012,12,31), Date(2013,12,31)) // 1
     *         NumberOfYearsBetween(Date(2011,1,1), Date(2013,12,31)) // 2
     *     <p/>
     *
     * @param d1 date
     * @param d2 date
     * @return number of years between
     */
    @ExpressionFunction("NumberOfYearsBetween")
    public static Long numberOfYearsBetween(@NotNull LocalDate d1, @NotNull LocalDate d2) {
        return numberOfMonthsBetween(d1, d2) / 12L;
    }

}
