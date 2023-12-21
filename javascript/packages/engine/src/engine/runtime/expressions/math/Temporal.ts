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

import {
    DateCalculator,
    DateField,
    DateTimeField,
    ISO_DATE,
    ISO_DATETIME_NO_OFFSET,
    ISO_DATETIME_ZULU,
} from '../date/DateCalculator'

export function resolveBrowserTimezoneId(): string {
    return Intl.DateTimeFormat().resolvedOptions().timeZone
}

export class DateCalculatorAdapter implements DateCalculator<unknown, unknown> {
    private readonly defaultDateCalculator = new DefaultDateCalculator()

    constructor(private readonly customDateCalculator: DateCalculator<unknown, unknown>) {}

    addDateField(date: unknown, field: DateField, value: number): unknown {
        return this.resolveCalculatorForDate(date).addDateField(date, field, value)
    }

    addDateTimeField(dateTime: unknown, field: DateTimeField, value: number, zoneId: string): unknown {
        return this.resolveCalculatorForDateTime(dateTime).addDateTimeField(dateTime, field, value, zoneId)
    }

    convertDateTimeToJavascriptDate(dateTime: unknown): Date {
        return this.resolveCalculatorForDateTime(dateTime).convertDateTimeToJavascriptDate(dateTime)
    }

    convertDateToJavascriptDate(date: unknown): Date {
        return this.resolveCalculatorForDate(date).convertDateToJavascriptDate(date)
    }

    createDate(date: string): unknown
    createDate(year: number, month: number, day: number): unknown
    createDate(dateStringOrYear: string | number, month?: number, day?: number): unknown {
        if (typeof dateStringOrYear === 'string') {
            return this.customDateCalculator.createDate(dateStringOrYear)
        }
        if (month != undefined && day != undefined) {
            return this.customDateCalculator.createDate(dateStringOrYear, month, day)
        }
        throw new Error('DateCalculatorAdapter#createDate is invoked incorrectly.')
    }

    createDateTime(dateTime: string): unknown
    createDateTime(dateTime: string, zoneId: string): unknown
    createDateTime(
        year: number,
        month: number,
        day: number,
        hours: number,
        minutes: number,
        seconds: number,
        zoneId: string,
    ): unknown
    createDateTime(
        dateTimeStringOrYear: string | number,
        zoneIdOrMonth?: string | number,
        day?: number,
        hours?: number,
        minutes?: number,
        seconds?: number,
        zoneId?: string,
    ): unknown {
        if (typeof dateTimeStringOrYear === 'string') {
            if (typeof zoneIdOrMonth === 'string') {
                return this.customDateCalculator.createDateTime(dateTimeStringOrYear, zoneIdOrMonth)
            }
            return this.customDateCalculator.createDateTime(dateTimeStringOrYear)
        }
        if (
            typeof zoneIdOrMonth === 'number' &&
            day != undefined &&
            hours != undefined &&
            minutes != undefined &&
            seconds != undefined &&
            zoneId
        ) {
            return this.customDateCalculator.createDateTime(
                dateTimeStringOrYear,
                zoneIdOrMonth,
                day,
                hours,
                minutes,
                seconds,
                zoneId,
            )
        }
        throw new Error('DateCalculatorAdapter#createDateTime is invoked incorrectly.')
    }

    differenceBetweenDates(date1: unknown, date2: unknown, field: DateField): number {
        if (this.customDateCalculator.isDate(date1) && this.customDateCalculator.isDate(date2)) {
            return this.customDateCalculator.differenceBetweenDates(date1, date2, field)
        }
        const date1Normalized = this.resolveCalculatorForDate(date1).convertDateToJavascriptDate(date1)
        const date2Normalized = this.resolveCalculatorForDate(date2).convertDateToJavascriptDate(date2)
        return this.defaultDateCalculator.differenceBetweenDates(date1Normalized, date2Normalized, field)
    }

    getDateField(date: unknown, field: DateField): number {
        return this.resolveCalculatorForDate(date).getDateField(date, field)
    }

    getDateTimeField(dateTime: unknown, field: DateTimeField, zoneId: string): number {
        return this.resolveCalculatorForDateTime(dateTime).getDateTimeField(dateTime, field, zoneId)
    }

    isDate(d: unknown): boolean {
        return this.customDateCalculator.isDate(d) || this.defaultDateCalculator.isDate(d)
    }

    isDateTime(d: unknown): boolean {
        return this.customDateCalculator.isDateTime(d) || this.defaultDateCalculator.isDateTime(d)
    }

    now(): unknown {
        return this.customDateCalculator.now()
    }

    toDate(dateTime: unknown, zoneId: string): unknown {
        return this.resolveCalculatorForDateTime(dateTime).toDate(dateTime, zoneId)
    }

    toDateTime(date: unknown, zoneId: string): unknown {
        return this.resolveCalculatorForDate(date).toDateTime(date, zoneId)
    }

    today(zoneId: string): unknown {
        return this.customDateCalculator.today(zoneId)
    }

    withDateField(date: unknown, field: DateField, value: number): unknown {
        return this.resolveCalculatorForDate(date).withDateField(date, field, value)
    }

    withDateTimeField(dateTime: unknown, field: DateTimeField, value: number, zoneId: string): unknown {
        return this.resolveCalculatorForDateTime(dateTime).withDateTimeField(dateTime, field, value, zoneId)
    }

    private resolveCalculatorForDate(date: unknown): DateCalculator<unknown, unknown> {
        if (this.customDateCalculator.isDate(date)) {
            return this.customDateCalculator
        }
        if (this.defaultDateCalculator.isDate(date)) {
            return this.defaultDateCalculator
        }
        throw new Error(`Value is not compatible with date calculations: ${date}`)
    }

    private resolveCalculatorForDateTime(dateTime: unknown): DateCalculator<unknown, unknown> {
        if (this.customDateCalculator.isDateTime(dateTime)) {
            return this.customDateCalculator
        }
        if (this.defaultDateCalculator.isDateTime(dateTime)) {
            return this.defaultDateCalculator
        }
        throw new Error(`Value is not compatible with date time calculations: ${dateTime}`)
    }
}

export class DefaultDateCalculator implements DateCalculator<Date, Date> {
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
            if (ISO_DATE.test(dateStringOrYear)) {
                return this.ensureValid(new Date(`${dateStringOrYear}T00:00:00`))
            }
            throw new Error(`Failed to create Date from pattern ${dateStringOrYear}`)
        }
        if (month != undefined && day != undefined) {
            ensureValidYear(dateStringOrYear)
            ensureValidMonth(month)
            ensureValidDay(day)
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
            if (typeof zoneIdOrMonth === 'string' && ISO_DATETIME_NO_OFFSET.test(dateTimeStringOrYear)) {
                this.ensureValidZoneId(zoneIdOrMonth)
                return this.ensureValid(new Date(dateTimeStringOrYear))
            }
            if (!zoneIdOrMonth && ISO_DATETIME_ZULU.test(dateTimeStringOrYear)) {
                return this.ensureValid(new Date(dateTimeStringOrYear))
            }
            throw new Error(`Failed to create Date from pattern ${dateTimeStringOrYear}`)
        }
        if (
            typeof zoneIdOrMonth === 'number' &&
            day != undefined &&
            hours != undefined &&
            minutes != undefined &&
            seconds != undefined &&
            zoneId
        ) {
            ensureValidYear(dateTimeStringOrYear)
            ensureValidMonth(zoneIdOrMonth)
            ensureValidDay(day)
            ensureValidHours(hours)
            ensureValidMinutes(minutes)
            ensureValidSeconds(seconds)
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

    isDate(d: unknown): boolean {
        return d instanceof Date
    }

    isDateTime(d: unknown): boolean {
        return d instanceof Date
    }

    now(): Date {
        return new Date()
    }

    withDateField(date: Date, field: DateField, value: number): Date {
        const dateResult = new Date(date)
        switch (field) {
            case 'YEAR': {
                ensureValidYear(value)
                dateResult.setFullYear(value)
                return this.resetToLastValidDayOfMonthIfNeeded(dateResult, date.getDate())
            }
            case 'MONTH': {
                ensureValidMonth(value)
                dateResult.setMonth(value - 1)
                return this.resetToLastValidDayOfMonthIfNeeded(dateResult, date.getDate())
            }
            case 'DAY_OF_MONTH': {
                ensureValidDay(value)
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
                ensureValidYear(value)
                dateTimeResult.setFullYear(value)
                return this.resetToLastValidDayOfMonthIfNeeded(dateTimeResult, dateTime.getDate())
            }
            case 'MONTH': {
                ensureValidMonth(value)
                dateTimeResult.setMonth(value - 1)
                return this.resetToLastValidDayOfMonthIfNeeded(dateTimeResult, dateTime.getDate())
            }
            case 'DAY_OF_MONTH': {
                ensureValidDay(value)
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
                ensureValidHours(value)
                dateTimeResult.setHours(value)
                return dateTimeResult
            }
            case 'MINUTE': {
                ensureValidMinutes(value)
                dateTimeResult.setMinutes(value)
                return dateTimeResult
            }
            case 'SECOND': {
                ensureValidSeconds(value)
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

    convertDateTimeToJavascriptDate(dateTime: Date): Date {
        return dateTime
    }

    convertDateToJavascriptDate(date: Date): Date {
        return date
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
        const systemTimeZoneId = Intl.DateTimeFormat().resolvedOptions().timeZone
        if (zoneId !== systemTimeZoneId) {
            throw new Error(
                `Default implementation of DateCalculator does not support time zone specific calculations. System time zone is ${systemTimeZoneId}, but function was invoked for time zone ${zoneId}.`,
            )
        }
    }

    private ensureValid(date: Date): Date {
        if (date.toString() === 'Invalid Date') {
            throw new Error(`Error while creating Date`)
        }
        return date
    }
}

export function ensureValidYear(year: number): void {
    if (year < 1 || year > 9999) {
        throw new Error(`Year must be between 1 and 9999, but was: '${year}'`)
    }
}

export function ensureValidMonth(month: number): void {
    if (month < 1 || month > 12) {
        throw new Error(`Month must be between 1 and 12, but was: '${month}'`)
    }
}

export function ensureValidDay(day: number): void {
    if (day < 1 || day > 31) {
        throw new Error(`Day must be between 1 and 31, but was: '${day}'`)
    }
}

export function ensureValidHours(hours: number): void {
    if (hours < 0 || hours > 23) {
        throw new Error(`Hours must be between 0 and 23, but was: '${hours}'`)
    }
}

export function ensureValidMinutes(minutes: number): void {
    if (minutes < 0 || minutes > 59) {
        throw new Error(`Minutes must be between 0 and 59, but was: '${minutes}'`)
    }
}

export function ensureValidSeconds(seconds: number): void {
    if (seconds < 0 || seconds > 59) {
        throw new Error(`Seconds must be between 0 and 59, but was: '${seconds}'`)
    }
}
