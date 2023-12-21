/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.el.date;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;

/**
 * SPI to be implemented when a custom handling of date calculations are required in the system.
 * Implementation must be registered by using {@link java.util.ServiceLoader} mechanism.
 * All instances of LocalDateTime must represent time on a default system locale clock.
 *
 * @author Mindaugas Ulevicius
 */
public interface DateCalculator {

    /**
     * Creates a date instance from ISO 8061 date string. String must be in YYYY-MM-DD format.
     * Time portion, compact and reduced precision formats are not supported.
     * @param date string in YYYY-MM-DD format
     * @return created date instance
     * @throws DateTimeException if date cannot be created with the given values
     * @throws DateTimeParseException if date string does not match YYYY-MM-DD pattern
     */
    LocalDate createDate(String date);

    /**
     * Creates a date instance from year, month and day triplet. Year, month and day start from 1.
     *
     * @param year a year
     * @param month a month, starts from 1 (January) and ends with 12 (December)
     * @param day a day of month - value must be a valid day for the month or exception will be thrown
     * @return created date instance
     * @throws DateTimeException if date cannot be created with the given values
     */
    LocalDate createDate(int year, int month, int day);

    /**
     * Creates a date instance which is date in specified time zone at this moment in time.
     * @param zoneId that indicates specific time zone
     * @return created date instance
     */
    LocalDate today(ZoneId zoneId);

    /**
     * Returns a specific field value of date. Year, month and day start from 1.
     * @param date initial date instance
     * @param field indicates which field value to return
     * @return value of the requested field
     */
    int getDateField(LocalDate date, DateField field);

    /**
     * Returns a new date instance with changed specific field value of date. Year, month and day start from 1.
     * Value cannot exceed valid range of value for a specific field.
     * When day of month is set, then date field value should not overflow.
     * For example, if day of month is set to be 32, then exception will be thrown.
     * However, if month value is set, but new month does not have such a day in initial date instance,
     * then the day should be adjusted to the last valid day for that month.
     *
     * @param date initial date instance
     * @param field indicates which field to change in initial date instance
     * @param value field value to set
     * @return new instance of date with changed field value
     * @throws DateTimeException if date cannot be created with the given values
     */
    LocalDate withDateField(LocalDate date, DateField field, int value);

    /**
     * Returns a new date instance with modified specific field value of date.
     * Field is modified by adding a specified amount. Year, month and day start from 1.
     * Adding large values is allowed and date value should overflow. For example, if 32 is added to the day of month,
     * then a month should also be increased by one.
     *
     * @param date initial date instance
     * @param field indicates which field to modify in initial date instance
     * @param value amount to add to field value
     * @return new instance of date with changed field value
     */
    LocalDate addDateField(LocalDate date, DateField field, long value);

    /**
     * Calculates a difference between two date instances by some date field.
     * Difference in months should depend on the last day of month.
     * For example, difference between 2022-01-31 and 2020-02-28 is 0 months, but difference between 2020-02-28 and 2020-03-31 is 1 month.
     * Also, result depends on whether first or second date is later. If first date is later, then result should be negative.
     *
     * @param date1 first date instance. If it is later than second date instance then result should be negative.
     * @param date2 second date instance
     * @param field indicates date field to calculate a difference of
     * @return difference of some date field
     */
    long difference(LocalDate date1, LocalDate date2, DateField field);

    /**
     * Creates a datetime instance from ISO 8061 date or datetime string.
     * String must be in YYYY-MM-DDThh:mm:ddZ format.
     * Compact, reduced precision and milliseconds are not supported.
     * @param dateTime string in YYYY-MM-DDThh:mm:ddZ format
     * @return created date instance
     * @throws DateTimeException if datetime cannot be created with the given values
     * @throws DateTimeParseException if datetime string does not match YYYY-MM-DDThh:mm:ddZ pattern
     */
    LocalDateTime createDateTime(String dateTime);

    /**
     * Creates a datetime instance from ISO 8061 datetime string in specified time zone.
     * String must be in YYYY-MM-DDThh:mm:ss format.
     * It must not contain time zone portion.
     *
     * @param dateTime string in YYYY-MM-DDThh:mm:ss format
     * @param zoneId a time zone of a clock
     * @return created datetime instance
     * @throws DateTimeException if datetime cannot be created with the given values
     * @throws DateTimeParseException if datetime string does not match YYYY-MM-DDThh:mm:dd pattern
     */
    LocalDateTime createDateTime(String dateTime, ZoneId zoneId);

    /**
     * Creates a datetime instance from year, month, day, hours, minutes and seconds in specified time zone.
     * Year, month, and day start from 1. Hours, minutes and seconds start from 0.
     *
     * @param year a year
     * @param month a month, starts from 1 (January) and ends with 12 (December)
     * @param day a day of month - value must be a valid day for the month or exception will be thrown
     * @param hours hour of the day, starting from 0 and ending with 23h
     * @param minutes minute of the hour, starting from 0 and ending with 59
     * @param seconds second of a minute, starting from 0 and ending with 59
     * @param zoneId a time zone of a clock
     * @return created datetime instance
     * @throws DateTimeException if datetime cannot be created with the given values
     */
    LocalDateTime createDateTime(int year, int month, int day, int hours, int minutes, int seconds, ZoneId zoneId);

    /**
     * @return a current moment in time
     */
    LocalDateTime now();

    /**
     * Returns a specific field value of datetime. Year, month and day start from 1. Hours, minutes and seconds start from 0.
     * @param dateTime initial datetime instance
     * @param field indicates which field value to return
     * @param zoneId a time zone of a clock
     * @return value of the requested field
     */
    int getDateTimeField(LocalDateTime dateTime, DateTimeField field, ZoneId zoneId);

    /**
     * Returns a new datetime instance with changed specific field value of datetime. Year, month and day start from 1.
     * Hours, minutes and seconds start from 0.
     * Value cannot exceed valid range of value for a specific field.
     * When day of month, hours, minutes or seconds is set, then the value should not overflow.
     * For example, if day of month is set to be 32, then exception will be thrown.
     * For example, if second is set to be 60, then exception will be thrown.
     * However, if month value is set, but new month does not have such a day in initial date instance,
     * then the day should be adjusted to the last valid day for that month.
     *
     * @param dateTime initial datetime instance
     * @param field indicates which field to change in initial datetime instance
     * @param value field value to set
     * @param zoneId a time zone of a clock. All operations are performed as if the clock is located in specified time zone.
     * @return new instance of datetime with changed field value
     * @throws DateTimeException if datetime cannot be created with the given values
     */
    LocalDateTime withDateTimeField(LocalDateTime dateTime, DateTimeField field, int value, ZoneId zoneId);

    /**
     * Returns a new datetime instance with modified specific field value of date.
     * Field is modified by adding a specified amount. Year, month and day start from 1.
     * Hours, minutes and seconds start from 0.
     * Adding large values is allowed and datetime value should overflow. For example, if 32 is added to the day of month,
     * then a month should also be increased by one. For example, if 60 is added to the seconds,
     * then a minute should also be increased by one.
     *
     * @param dateTime initial datetime instance
     * @param field indicates which field to change in initial datetime instance
     * @param value amount to add to field value
     * @param zoneId a time zone of a clock. All operations are performed as if the clock is located in specified time zone.
     * @return new instance of datetime with changed field value
     */
    LocalDateTime addDateTimeField(LocalDateTime dateTime, DateTimeField field, long value, ZoneId zoneId);

    /**
     * Returns a moment in time which represents a start of the day at the provided date in specified time zone.
     *
     * @param date initial date instance
     * @param zoneId a time zone of a clock
     * @return datetime instance that represents a start of the day at the provided date in specified time zone
     */
    LocalDateTime toDateTime(LocalDate date, ZoneId zoneId);

    /**
     * Returns a current calendar date of the moment in time at the specified time zone.
     *
     * @param dateTime initial datetime instance that indicates a moment in time
     * @param zoneId a time zone of a clock
     * @return date instance that represents current date of the moment in time at the specified time zone
     */
    LocalDate toDate(LocalDateTime dateTime, ZoneId zoneId);

    /**
     * Enumerates all date fields
     */
    enum DateField {

        YEAR(ChronoField.YEAR),
        MONTH(ChronoField.MONTH_OF_YEAR),
        DAY_OF_MONTH(ChronoField.DAY_OF_MONTH);

        DateField(ChronoField chronoField) {
            this.chronoField = chronoField;
        }

        final ChronoField chronoField;
    }

    /**
     * Enumerates all datetime fields
     */
    enum DateTimeField {
        YEAR(ChronoField.YEAR),
        MONTH(ChronoField.MONTH_OF_YEAR),
        DAY_OF_MONTH(ChronoField.DAY_OF_MONTH),
        HOUR(ChronoField.HOUR_OF_DAY),
        MINUTE(ChronoField.MINUTE_OF_HOUR),
        SECOND(ChronoField.SECOND_OF_MINUTE);

        DateTimeField(ChronoField chronoField) {
            this.chronoField = chronoField;
        }

        final ChronoField chronoField;
    }

    /**
     * @return a current registered instance of DateCalculator
     */
    static DateCalculator getInstance() {
        return DateCalculatorProvider.getInstance();
    }
}
