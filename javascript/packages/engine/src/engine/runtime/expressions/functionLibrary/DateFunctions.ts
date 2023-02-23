/* eslint-disable prefer-rest-params */
/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

import moment from 'moment'
import { message } from './function.utils'

function Today(): Date {
    const now = new Date()
    return new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0)
}

function Now(): Date {
    return new Date()
}

function FxDate(dateString?: string): Date
function FxDate(year?: number, month?: number, day?: number): Date

function FxDate(dateString?: string | number, month?: number, day?: number): Date {
    if (!dateString) {
        throw new Error("Failed to execute function 'Date' or 'DateTime' with parameters: " + [...arguments].join())
    }
    if (typeof dateString === 'string') {
        return tryParseDateString('Date', dateString)
    }
    const year = dateString
    if (month && day) {
        ensureValidYear('Date', year)
        ensureValidMonth('Date', month)
        ensureValidDay('Date', day)
        return new Date(year, month - 1, day)
    }
    throw new Error("Failed to execute function 'Date' or 'DateTime' with parameters: " + [...arguments].join())
}

function PlusYears(dateArg?: Date, num?: number): Date {
    if (!dateArg) {
        throw new Error(message('PlusYears', message.reason.firstParam))
    }
    if (num == undefined) {
        throw new Error(message('PlusYears', message.reason.secondParam))
    }
    const date = new Date(dateArg)
    date.setUTCFullYear(date.getUTCFullYear() + num)

    return resetToLastValidDayOfMonthIfNeeded(date, dateArg.getDate())
}

function PlusMonths(dateArg?: Date, num?: number): Date {
    if (!dateArg) {
        throw new Error(message('PlusMonths', message.reason.firstParam))
    }
    if (num == undefined) {
        throw new Error(message('PlusMonths', message.reason.secondParam))
    }
    const date = new Date(dateArg)
    date.setUTCMonth(date.getUTCMonth() + num)

    return resetToLastValidDayOfMonthIfNeeded(date, dateArg.getDate())
}

function PlusDays(dateArg?: Date, num?: number): Date {
    if (!dateArg) {
        throw new Error(message('PlusDays', message.reason.firstParam))
    }
    if (num == undefined) {
        throw new Error(message('PlusDays', message.reason.secondParam))
    }
    const date = new Date(dateArg)
    date.setUTCDate(date.getUTCDate() + num)
    return date
}

function AsDate(dateArg?: Date): Date {
    if (!dateArg) {
        throw new Error(message('AsDate', message.reason.firstParam))
    }
    return new Date(dateArg.getFullYear(), dateArg.getMonth(), dateArg.getDate())
}

function AsTime(dateArg?: Date): Date {
    if (!dateArg) {
        throw new Error(message('AsTime', message.reason.firstParam))
    }
    return new Date(dateArg.getFullYear(), dateArg.getMonth(), dateArg.getDate(), 0, 0, 0, 0)
}

function IsDateBetween(dateToCheck?: Date, start?: Date, end?: Date): boolean {
    if (!dateToCheck) {
        throw new Error(message('NumberOfDaysBetween', message.reason.firstParam))
    }
    if (!start) {
        throw new Error(message('NumberOfDaysBetween', message.reason.secondParam))
    }
    if (!end) {
        throw new Error(message('NumberOfDaysBetween', message.reason.thirdParam))
    }
    return dateAfter(dateToCheck, start) && dateBefore(dateToCheck, end)
}

function GetDay(date?: Date): number {
    if (!date) {
        throw new Error("Failed to execute function 'GetDay'. Parameter is absent")
    }
    try {
        return date.getDate()
    } catch (error) {
        throw new Error(`Failed to execute function 'GetDay'. Parameter '${date}' is invalid`)
    }
}

function GetYear(date?: Date): number {
    if (!date) {
        throw new Error("Failed to execute function 'GetYear'. Parameter is absent")
    }
    try {
        return date.getFullYear()
    } catch (error) {
        throw new Error(`Failed to execute function 'GetYear'. Parameter '${date}' is invalid`)
    }
}

function GetMonth(date?: Date): number {
    if (!date) {
        throw new Error("Failed to execute function 'GetMonth'. Parameter is absent")
    }
    try {
        return date.getMonth() + 1
    } catch (error) {
        throw new Error(`Failed to execute function 'GetMonth'. Parameter '${date}' is invalid`)
    }
}

function WithYear(date: Date | undefined, year: number | undefined): Date {
    if (!date) {
        throw new Error(`Failed to execute function 'WithYear', because date value is null or undefined`)
    }
    if (!year) {
        throw new Error(`Failed to execute function 'WithYear', because year value is null or undefined`)
    }
    ensureValidYear('WithYear', year)

    const dateCopy = new Date(date)
    dateCopy.setUTCFullYear(year)

    return resetToLastValidDayOfMonthIfNeeded(dateCopy, date.getDate())
}

function WithMonth(date: Date | undefined, month: number | undefined): Date {
    if (!date) {
        throw new Error(`Failed to execute function 'WithMonth', because date value is null or undefined`)
    }
    if (!month) {
        throw new Error(`Failed to execute function 'WithMonth', because month value is null or undefined`)
    }
    ensureValidMonth('WithMonth', month)

    const dateCopy = new Date(date)
    dateCopy.setUTCMonth(month - 1)

    return resetToLastValidDayOfMonthIfNeeded(dateCopy, date.getDate())
}

function WithDay(date: Date | undefined, day: number | undefined): Date {
    if (!date) {
        throw new Error(`Failed to execute function 'WithDay', because date value is null or undefined`)
    }
    if (!day) {
        throw new Error(`Failed to execute function 'WithDay', because day value is null or undefined`)
    }
    ensureValidDay('WithDay', day)

    const dateCopy = new Date(date)
    dateCopy.setUTCDate(day)

    if (dateCopy.getUTCDate() != day) {
        throw new Error(
            `Cannot set day '${day}' in date '${date}' because month '${
                date.getUTCMonth() + 1
            }' of year '${date.getUTCFullYear()}' does not have this day.`,
        )
    }

    return dateCopy
}

function NumberOfDaysBetween(start?: Date, end?: Date): number {
    if (!start) {
        throw new Error(message('NumberOfDaysBetween', message.reason.firstParam))
    }
    if (!end) {
        throw new Error(message('NumberOfDaysBetween', message.reason.secondParam))
    }
    const offsetInMilliseconds = (end.getTimezoneOffset() - start.getTimezoneOffset()) * 60 * 1000
    const day = 24 * 60 * 60 * 1000
    const diffTime = Math.abs(start.getTime() - end.getTime() + offsetInMilliseconds)
    return Math.floor(diffTime / day)
}

function NumberOfMonthsBetween(d1?: Date, d2?: Date): number {
    if (!d1) {
        throw new Error(message('NumberOfMonthsBetween', message.reason.firstParam))
    }
    if (!d2) {
        throw new Error(message('NumberOfMonthsBetween', message.reason.secondParam))
    }
    try {
        const date1 = AsDate(d1)
        const date2 = AsDate(d2)

        const from = date1 < date2 ? date1 : date2
        const to = date2 > date1 ? date2 : date1

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
    } catch (error) {
        throw new Error(message('NumberOfMonthsBetween', 'Parameters are invalid: ' + [...arguments]))
    }
}

function NumberOfYearsBetween(start?: Date, end?: Date): number {
    return Math.floor(NumberOfMonthsBetween(start, end) / 12)
}

function Format(date?: Date, format = 'YYYY-MM-DD'): string {
    if (!date) {
        throw new Error(message('Format', message.reason.firstParam))
    }
    return moment(date).format(format)
}

function tryParseDateString(functionName: string, dateString: string): Date {
    const date = new Date(dateString)
    if (date.toString() === 'Invalid Date') {
        throw new Error("Failed to execute function 'Date' or 'DateTime' with parameters: " + [...arguments].join())
    }
    ensureValidYear(functionName, date.getFullYear())
    ensureValidMonth(functionName, date.getMonth() + 1)
    ensureValidDay(functionName, date.getDate())
    return date
}

function ensureValidYear(functionName: string, year: number): void {
    if (year < 1 || year > 9999) {
        throw new Error(
            `Failed to execute function '${functionName}'. Year value must be from 1 to 9999, but was: '${year}'`,
        )
    }
}

function ensureValidMonth(functionName: string, month: number): void {
    if (month < 1 || month > 12) {
        throw new Error(
            `Failed to execute function '${functionName}'. Month value must be from 1 (January) to 12 (December), but was: '${month}'`,
        )
    }
}

function ensureValidDay(functionName: string, day: number): void {
    if (day < 1 || day > 31) {
        throw new Error(
            `Failed to execute function '${functionName}'. Day value must be from 1 to 31, but was: '${day}'`,
        )
    }
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
function resetToLastValidDayOfMonthIfNeeded(date: Date, previousDay: number): Date {
    if (date.getDate() != previousDay) {
        date.setUTCDate(0)
    }
    return date
}

export const dateFunctions = {
    Format,
    GetYear,
    GetMonth,
    GetDay,
    Today,
    Now,
    Date: FxDate,
    DateTime: FxDate,
    PlusYears,
    PlusMonths,
    PlusDays,
    AsDate,
    AsTime,
    NumberOfDaysBetween,
    NumberOfMonthsBetween,
    NumberOfYearsBetween,
    IsDateBetween,
    WithYear,
    WithMonth,
    WithDay,
}

const dateAfter = (date: Date, start: Date) => date >= start
const dateBefore = (date: Date, end: Date) => date <= end
