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
export interface DateCalculator {
    /**
     * Creates a date instance from ISO 8061 date string. String must be in YYYY-MM-DD format.
     * Time portion, compact and reduced precision formats are not supported.
     * @param date string in YYYY-MM-DD format
     * @return created date instance
     * @throws error if date does not match YYYY-MM-DD pattern or if the date cannot be created with given values
     */
    createDate(date: string): Date

    /**
     * Creates a date instance from year, month and day triplet. Year, month and day start from 1.
     *
     * @param year a year
     * @param month a month, starts from 1 (January) and ends with 12 (December)
     * @param day a day of month - value must be a valid day for the month or error will be thrown
     * @return created date instance
     * @throws error if the date cannot be created with given values
     */
    createDate(year: number, month: number, day: number): Date

    /**
     * Creates a date instance which is date in specified timezone at this moment in time.
     * @param zoneId that indicates specific timezone
     * @return created date instance
     */
    today(zoneId: string): Date

    /**
     * Returns a specific field value of date. Year, month and day start from 1.
     * @param date initial date instance
     * @param field indicates which field value to return
     * @return value of the requested field
     */
    getDateField(date: Date, field: DateField): number

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
    withDateField(date: Date, field: DateField, value: number): Date

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
    addDateField(date: Date, field: DateField, value: number): Date

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
    differenceBetweenDates(date1: Date, date2: Date, field: DateField): number

    /**
     * Creates a datetime instance from ISO 8061 datetime string.
     * String must be in YYYY-MM-DDThh:mm:ddZ format.
     * Compact, reduced precision and milliseconds are not supported.
     * @param dateTime string in YYYY-MM-DDThh:mm:ddZ format
     * @return created date instance
     * @throws error if date time does not match YYYY-MM-DDThh:mm:ddZ pattern or if the datetime cannot be created
     * with given values
     */
    createDateTime(dateTime: string): Date

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
    createDateTime(dateTime: string, zoneId: string): Date

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
    ): Date

    /**
     * @return a current moment in time
     */
    now(): Date

    /**
     * Returns a specific field value of datetime. Year, month and day start from 1. Hours, minutes and seconds start from 0.
     * @param dateTime initial datetime instance
     * @param field indicates which field value to return
     * @param zoneId a timezone of a clock
     * @return value of the requested field
     */
    getDateTimeField(dateTime: Date, field: DateTimeField, zoneId: string): number

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
    withDateTimeField(dateTime: Date, field: DateTimeField, value: number, zoneId: string): Date

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
    addDateTimeField(dateTime: Date, field: DateTimeField, value: number, zoneId: string): Date

    /**
     * Returns a moment in time which represents a start of the day at the provided date in specified timezone.
     *
     * @param date initial date instance
     * @param zoneId a timezone of a clock
     * @return datetime instance that represents a start of the day at the provided date in specified timezone
     */
    toDateTime(date: Date, zoneId: string): Date

    /**
     * Returns a current calendar date of the moment in time at the specified timezone.
     *
     * @param dateTime initial datetime instance that indicates a moment in time
     * @param zoneId a timezone of a clock
     * @return date instance that represents current date of the moment in time at the specified timezone
     */
    toDate(dateTime: Date, zoneId: string): Date

    /**
     * Differentiates date from datetime instance when using the same javascript Date container.
     *
     * @param d date or datetime
     */
    isDate(d: Date): boolean

    /**
     * Differentiates date from datetime instance when using the same javascript Date container.
     *
     * @param d date or datetime
     */
    isDateTime(d: Date): boolean
}

/**
 * Enumerates all date fields
 */
export type DateField = 'YEAR' | 'MONTH' | 'DAY_OF_MONTH'

/**
 * Enumerates all datetime fields
 */
export type DateTimeField = 'YEAR' | 'MONTH' | 'DAY_OF_MONTH' | 'HOUR' | 'MINUTE' | 'SECOND'

export class DefaultDateCalculator implements DateCalculator {
    private ISO_DATE = new RegExp('^\\d{4}-\\d{2}-\\d{2}$')
    private ISO_DATETIME_NO_OFFSET = new RegExp('^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$')
    private ISO_DATETIME_ZULU = new RegExp('^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}Z$')

    addDateField(date: Date, field: DateField, value: number): Date {
        const dateResult = new Date(date)
        switch (field) {
            case 'YEAR': {
                dateResult.setFullYear(date.getFullYear() + value)
                return this.resetToLastValidDayOfMonthIfNeeded(dateResult, date.getDate())
            }
            case 'MONTH': {
                dateResult.setMonth(date.getMonth() + value)
                return this.resetToLastValidDayOfMonthIfNeeded(dateResult, date.getDate())
            }
            case 'DAY_OF_MONTH': {
                dateResult.setDate(date.getDate() + value)
                return dateResult
            }
        }
    }

    addDateTimeField(dateTime: Date, field: DateTimeField, value: number, zoneId: string): Date {
        this.ensureValidZoneId(zoneId)
        const dateTimeResult = new Date(dateTime)
        switch (field) {
            case 'YEAR': {
                dateTimeResult.setFullYear(dateTime.getFullYear() + value)
                return this.resetToLastValidDayOfMonthIfNeeded(dateTimeResult, dateTime.getDate())
            }
            case 'MONTH': {
                dateTimeResult.setMonth(dateTime.getMonth() + value)
                return this.resetToLastValidDayOfMonthIfNeeded(dateTimeResult, dateTime.getDate())
            }
            case 'DAY_OF_MONTH': {
                dateTimeResult.setDate(dateTime.getDate() + value)
                return dateTimeResult
            }
            case 'HOUR': {
                dateTimeResult.setHours(dateTime.getHours() + value)
                return dateTimeResult
            }
            case 'MINUTE': {
                dateTimeResult.setMinutes(dateTime.getMinutes() + value)
                return dateTimeResult
            }
            case 'SECOND': {
                dateTimeResult.setSeconds(dateTime.getSeconds() + value)
                return dateTimeResult
            }
        }
    }

    createDate(date: string): Date
    createDate(year: number, month: number, day: number): Date
    createDate(dateStringOrYear: string | number, month?: number, day?: number): Date {
        if (typeof dateStringOrYear === 'string') {
            if (this.ISO_DATE.test(dateStringOrYear)) {
                return this.ensureValid(new Date(`${dateStringOrYear}T00:00:00`))
            }
            throw new Error(`Failed to create Date from pattern ${dateStringOrYear}`)
        }
        if (month !== undefined && day !== undefined) {
            this.ensureValidYear(dateStringOrYear)
            this.ensureValidMonth(month)
            this.ensureValidDay(day)
            return new Date(dateStringOrYear, month - 1, day)
        }
        throw new Error('Failed to create Date')
    }

    createDateTime(dateTime: string): Date
    createDateTime(dateTime: string, zoneId: string): Date
    createDateTime(
        year: number,
        month: number,
        day: number,
        hours: number,
        minutes: number,
        seconds: number,
        zoneId: string,
    ): Date
    createDateTime(
        dateTimeStringOrYear: string | number,
        zoneIdOrMonth?: string | number,
        day?: number,
        hours?: number,
        minutes?: number,
        seconds?: number,
        zoneId?: string,
    ): Date {
        if (typeof dateTimeStringOrYear === 'string') {
            if (typeof zoneIdOrMonth === 'string' && this.ISO_DATETIME_NO_OFFSET.test(dateTimeStringOrYear)) {
                this.ensureValidZoneId(zoneIdOrMonth)
                return this.ensureValid(new Date(dateTimeStringOrYear))
            }
            if (!zoneIdOrMonth && this.ISO_DATETIME_ZULU.test(dateTimeStringOrYear)) {
                return this.ensureValid(new Date(dateTimeStringOrYear))
            }
            throw new Error(`Failed to create Date from pattern ${dateTimeStringOrYear}`)
        }
        if (
            typeof zoneIdOrMonth === 'number' &&
            day !== undefined &&
            hours !== undefined &&
            minutes !== undefined &&
            seconds !== undefined &&
            zoneId
        ) {
            this.ensureValidYear(dateTimeStringOrYear)
            this.ensureValidMonth(zoneIdOrMonth)
            this.ensureValidDay(day)
            this.ensureValidHours(hours)
            this.ensureValidMinutes(minutes)
            this.ensureValidSeconds(seconds)
            this.ensureValidZoneId(zoneId)
            return new Date(dateTimeStringOrYear, zoneIdOrMonth - 1, day, hours, minutes, seconds)
        }
        throw new Error('Failed to create Date')
    }

    differenceBetweenDates(date1: Date, date2: Date, field: DateField): number {
        const from = date1 < date2 ? date1 : date2
        const to = date2 > date1 ? date2 : date1

        switch (field) {
            case 'YEAR':
                return Math.floor(this.differenceBetweenDates(date1, date2, 'MONTH') / 12)
            case 'MONTH': {
                let fullMonths = (to.getFullYear() - from.getFullYear()) * 12 + (to.getMonth() - from.getMonth())
                if (fullMonths > 0) {
                    fullMonths -= 1
                }
                let partialMonth = 0
                if (
                    (from.getFullYear() < to.getFullYear() || from.getMonth() < to.getMonth()) &&
                    to.getDate() >= from.getDate()
                ) {
                    partialMonth = 1
                }
                return fullMonths + partialMonth
            }
            case 'DAY_OF_MONTH': {
                const offsetInMilliseconds = (to.getTimezoneOffset() - from.getTimezoneOffset()) * 60 * 1000
                const day = 24 * 60 * 60 * 1000
                const diffTime = Math.abs(from.getTime() - to.getTime() + offsetInMilliseconds)
                return Math.floor(diffTime / day)
            }
        }
    }

    getDateField(date: Date, field: DateField): number {
        switch (field) {
            case 'YEAR':
                return date.getFullYear()
            case 'MONTH':
                return date.getMonth() + 1
            case 'DAY_OF_MONTH':
                return date.getDate()
        }
    }

    getDateTimeField(dateTime: Date, field: DateTimeField, zoneId: string): number {
        this.ensureValidZoneId(zoneId)
        switch (field) {
            case 'YEAR':
                return dateTime.getFullYear()
            case 'MONTH':
                return dateTime.getMonth() + 1
            case 'DAY_OF_MONTH':
                return dateTime.getDate()
            case 'HOUR':
                return dateTime.getHours()
            case 'MINUTE':
                return dateTime.getMinutes()
            case 'SECOND':
                return dateTime.getSeconds()
        }
    }

    isDate(_d: Date): boolean {
        // default implementation does not support differentiation between DATE and DATETIME
        return true
    }

    isDateTime(_d: Date): boolean {
        // default implementation does not support differentiation between DATE and DATETIME
        return true
    }

    now(): Date {
        return new Date()
    }

    withDateField(date: Date, field: DateField, value: number): Date {
        const dateResult = new Date(date)
        switch (field) {
            case 'YEAR': {
                this.ensureValidYear(value)
                dateResult.setFullYear(value)
                return this.resetToLastValidDayOfMonthIfNeeded(dateResult, date.getDate())
            }
            case 'MONTH': {
                this.ensureValidMonth(value)
                dateResult.setMonth(value - 1)
                return this.resetToLastValidDayOfMonthIfNeeded(dateResult, date.getDate())
            }
            case 'DAY_OF_MONTH': {
                this.ensureValidDay(value)
                dateResult.setDate(value)
                if (dateResult.getDate() != value) {
                    throw new Error(
                        `Cannot set day '${value}' in date '${date}' because month '${
                            date.getMonth() + 1
                        }' of year '${date.getFullYear()}' does not have this day.`,
                    )
                }
                return dateResult
            }
        }
    }

    withDateTimeField(dateTime: Date, field: DateTimeField, value: number, zoneId: string): Date {
        this.ensureValidZoneId(zoneId)
        const dateTimeResult = new Date(dateTime)
        switch (field) {
            case 'YEAR': {
                this.ensureValidYear(value)
                dateTimeResult.setFullYear(value)
                return this.resetToLastValidDayOfMonthIfNeeded(dateTimeResult, dateTime.getDate())
            }
            case 'MONTH': {
                this.ensureValidMonth(value)
                dateTimeResult.setMonth(value - 1)
                return this.resetToLastValidDayOfMonthIfNeeded(dateTimeResult, dateTime.getDate())
            }
            case 'DAY_OF_MONTH': {
                this.ensureValidDay(value)
                dateTimeResult.setDate(value)
                if (dateTimeResult.getDate() != value) {
                    throw new Error(
                        `Cannot set day '${value}' in date '${dateTime}' because month '${
                            dateTime.getMonth() + 1
                        }' of year '${dateTime.getFullYear()}' does not have this day.`,
                    )
                }
                return dateTimeResult
            }
            case 'HOUR': {
                this.ensureValidHours(value)
                dateTimeResult.setHours(value)
                return dateTimeResult
            }
            case 'MINUTE': {
                this.ensureValidMinutes(value)
                dateTimeResult.setMinutes(value)
                return dateTimeResult
            }
            case 'SECOND': {
                this.ensureValidSeconds(value)
                dateTimeResult.setSeconds(value)
                return dateTimeResult
            }
        }
    }

    toDate(dateTime: Date, zoneId: string): Date {
        this.ensureValidZoneId(zoneId)
        return new Date(dateTime.getFullYear(), dateTime.getMonth(), dateTime.getDate())
    }

    toDateTime(date: Date, zoneId: string): Date {
        this.ensureValidZoneId(zoneId)
        return new Date(date.getFullYear(), date.getMonth(), date.getDate(), 0, 0, 0, 0)
    }

    today(zoneId: string): Date {
        this.ensureValidZoneId(zoneId)
        const now = new Date()
        return new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0, 0)
    }

    /**
     *  When changing date to some month-of-year it may shift day-of-month to the first day of the next month when requested
     *  month-of-year does not have such a day.
     *  In this case we need to do a correction so that it works equivalently to Java implementation.
     *  A correction is to rewind to last valid day-of-month.
     *  By setting day to 0 the date is reset to last day of the previous month which is the last valid day-of-month.
     *
     * @param date after modification
     * @param previousDay of date before modification
     */
    private resetToLastValidDayOfMonthIfNeeded(date: Date, previousDay: number): Date {
        if (date.getDate() != previousDay) {
            date.setDate(0)
        }
        return date
    }

    private ensureValidZoneId(zoneId: string): void {
        const systemTimezoneId = Intl.DateTimeFormat().resolvedOptions().timeZone
        if (zoneId !== systemTimezoneId) {
            throw new Error(
                `Default implementation of DateCalculator does not support timezone specific calculations. System timezone is ${systemTimezoneId}, but function was invoked for timezone ${zoneId}.`,
            )
        }
    }

    private ensureValid(date: Date): Date {
        if (date.toString() === 'Invalid Date') {
            throw new Error(`Error while creating Date`)
        }
        return date
    }

    private ensureValidYear(year: number): void {
        if (year < 1 || year > 9999) {
            throw new Error(`Error while creating Date. Year must be between 1 and 9999`)
        }
    }

    private ensureValidMonth(month: number): void {
        if (month < 1 || month > 12) {
            throw new Error(`Error while creating Date. Month must be between 1 and 12`)
        }
    }

    private ensureValidDay(day: number): void {
        if (day < 1 || day > 31) {
            throw new Error(`Error while creating Date. Day must be between 1 and 31`)
        }
    }

    private ensureValidHours(hours: number): void {
        if (hours < 0 || hours > 23) {
            throw new Error(`Error while creating Date. Hours must be between 0 and 23`)
        }
    }

    private ensureValidMinutes(minutes: number): void {
        if (minutes < 0 || minutes > 59) {
            throw new Error(`Error while creating Date. Minutes must be between 0 and 59`)
        }
    }

    private ensureValidSeconds(seconds: number): void {
        if (seconds < 0 || seconds > 59) {
            throw new Error(`Error while creating Date. Seconds must be between 0 and 59`)
        }
    }
}
