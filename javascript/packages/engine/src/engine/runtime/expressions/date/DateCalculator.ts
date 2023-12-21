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

/**
 * SPI to be implemented when a custom handling of date calculations are required in the system.
 * Implementation can be provided during rule evaluation to {@link SyncEngine}.
 *
 * @author Mindaugas Ulevicius
 */
export interface DateCalculator<DATE, DATETIME> {
    /**
     * Creates a date instance from ISO 8061 date string. String must be in YYYY-MM-DD format.
     * Time portion, compact and reduced precision formats are not supported.
     * @param date string in YYYY-MM-DD format
     * @return created date instance
     * @throws error if date does not match YYYY-MM-DD pattern or if the date cannot be created with given values
     */
    createDate(date: string): DATE

    /**
     * Creates a date instance from year, month and day triplet. Year, month and day start from 1.
     *
     * @param year a year
     * @param month a month, starts from 1 (January) and ends with 12 (December)
     * @param day a day of month - value must be a valid day for the month or error will be thrown
     * @return created date instance
     * @throws error if the date cannot be created with given values
     */
    createDate(year: number, month: number, day: number): DATE

    /**
     * Creates a date instance which is date in specified timezone at this moment in time.
     * @param zoneId that indicates specific timezone
     * @return created date instance
     */
    today(zoneId: string): DATE

    /**
     * Returns a specific field value of date. Year, month and day start from 1.
     * @param date initial date instance
     * @param field indicates which field value to return
     * @return value of the requested field
     */
    getDateField(date: DATE, field: DateField): number

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
     * @throws error if the date cannot be created with given value
     */
    withDateField(date: DATE, field: DateField, value: number): DATE

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
    addDateField(date: DATE, field: DateField, value: number): DATE

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
    differenceBetweenDates(date1: DATE, date2: DATE, field: DateField): number

    /**
     * Creates a datetime instance from ISO 8061 datetime string.
     * String must be in YYYY-MM-DDThh:mm:ddZ format.
     * Compact, reduced precision and milliseconds are not supported.
     * @param dateTime string in YYYY-MM-DDThh:mm:ddZ format
     * @return created date instance
     * @throws error if date time does not match YYYY-MM-DDThh:mm:ddZ pattern or if the datetime cannot be created
     * with given values
     */
    createDateTime(dateTime: string): DATETIME

    /**
     * Creates a datetime instance from ISO 8061 datetime string in specified timezone.
     * String must be in YYYY-MM-DDThh:mm:ss format.
     * It must not contain timezone portion.
     *
     * @param dateTime string in YYYY-MM-DDThh:mm:ss format
     * @param zoneId a timezone of a clock
     * @return created datetime instance
     * @throws error if date time does not match YYYY-MM-DDThh:mm:dd pattern or if the datetime cannot be created
     * with given values
     */
    createDateTime(dateTime: string, zoneId: string): DATETIME

    /**
     * Creates a datetime instance from year, month, day, hours, minutes and seconds in specified timezone.
     * Year, month, and day start from 1. Hours, minutes and seconds start from 0.
     *
     * @param year a year
     * @param month a month, starts from 1 (January) and ends with 12 (December)
     * @param day a day of month - value must be a valid day for the month or exception will be thrown
     * @param hours hour of the day, starting from 0 and ending with 23h
     * @param minutes minute of the hour, starting from 0 and ending with 59
     * @param seconds second of a minute, starting from 0 and ending with 59
     * @param zoneId a timezone of a clock
     * @return created datetime instance
     * @throws error if datetime cannot be created with given values
     */
    createDateTime(
        year: number,
        month: number,
        day: number,
        hours: number,
        minutes: number,
        seconds: number,
        zoneId: string,
    ): DATETIME

    /**
     * @return a current moment in time
     */
    now(): DATETIME

    /**
     * Returns a specific field value of datetime. Year, month and day start from 1. Hours, minutes and seconds start from 0.
     * @param dateTime initial datetime instance
     * @param field indicates which field value to return
     * @param zoneId a timezone of a clock
     * @return value of the requested field
     */
    getDateTimeField(dateTime: DATETIME, field: DateTimeField, zoneId: string): number

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
     * @param zoneId a timezone of a clock. All operations are performed as if the clock is located in specified timezone.
     * @return new instance of datetime with changed field value
     * @throws error if datetime cannot be created with given values
     */
    withDateTimeField(dateTime: DATETIME, field: DateTimeField, value: number, zoneId: string): DATETIME

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
     * @param zoneId a timezone of a clock. All operations are performed as if the clock is located in specified timezone.
     * @return new instance of datetime with changed field value
     */
    addDateTimeField(dateTime: DATETIME, field: DateTimeField, value: number, zoneId: string): DATETIME

    /**
     * Returns a moment in time which represents a start of the day at the provided date in specified timezone.
     *
     * @param date initial date instance
     * @param zoneId a timezone of a clock
     * @return datetime instance that represents a start of the day at the provided date in specified timezone
     */
    toDateTime(date: DATE, zoneId: string): DATETIME

    /**
     * Returns a current calendar date of the moment in time at the specified timezone.
     *
     * @param dateTime initial datetime instance that indicates a moment in time
     * @param zoneId a timezone of a clock
     * @return date instance that represents current date of the moment in time at the specified timezone
     */
    toDate(dateTime: DATETIME, zoneId: string): DATE

    /**
     * Differentiates date from datetime.
     *
     * @param d date or datetime
     */
    isDate(d: unknown): boolean

    /**
     * Differentiates date from datetime.
     *
     * @param d date or datetime
     */
    isDateTime(d: unknown): boolean

    convertDateToJavascriptDate(date: DATE): Date

    convertDateTimeToJavascriptDate(dateTime: DATETIME): Date
}

/**
 * Enumerates all date fields
 */
export type DateField = 'YEAR' | 'MONTH' | 'DAY_OF_MONTH'

/**
 * Enumerates all datetime fields
 */
export type DateTimeField = 'YEAR' | 'MONTH' | 'DAY_OF_MONTH' | 'HOUR' | 'MINUTE' | 'SECOND'

export const ISO_DATE = new RegExp('^\\d{4}-\\d{2}-\\d{2}$')
export const ISO_DATETIME_NO_OFFSET = new RegExp('^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$')
export const ISO_DATETIME_ZULU = new RegExp('^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$')
