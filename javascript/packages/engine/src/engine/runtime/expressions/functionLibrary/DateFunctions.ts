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
import { InternalFunctionScope } from './Registry'

export const dateFunctions = {
    Format,
    GetYear,
    GetMonth,
    GetDay,
    Today,
    Now,
    Date: FxDate,
    DateTime: FxDateTime,
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

const ISO_DATE = new RegExp('^\\d{4}-\\d{2}-\\d{2}$')
const ISO_DATETIME_NO_OFFSET = new RegExp('^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}$')

function Today(this: InternalFunctionScope): Date {
    return this.dateCalculator.today(this.functionContext.zoneId)
}

function Now(this: InternalFunctionScope): Date {
    return this.dateCalculator.now()
}

function FxDate(dateString?: string): Date
function FxDate(year?: number, month?: number, day?: number): Date

function FxDate(this: InternalFunctionScope, dateStringOrYear?: string | number, month?: number, day?: number): Date {
    if (typeof dateStringOrYear === 'string') {
        let date
        if (ISO_DATETIME_NO_OFFSET.test(dateStringOrYear)) {
            const dateTime = this.dateCalculator.createDateTime(dateStringOrYear, this.functionContext.zoneId)
            date = this.dateCalculator.toDate(dateTime, this.functionContext.zoneId)
        } else if (ISO_DATE.test(dateStringOrYear)) {
            date = this.dateCalculator.createDate(dateStringOrYear)
        } else {
            const dateTime = this.dateCalculator.createDateTime(dateStringOrYear)
            date = this.dateCalculator.toDate(dateTime, this.functionContext.zoneId)
        }
        if (date.toString() === 'Invalid Date') {
            throw new Error(`Failed to execute function 'Date' with parameters: ${[...arguments].join()}`)
        }
        return date
    }
    if (typeof dateStringOrYear === 'number' && month && day) {
        ensureValidYear('Date', dateStringOrYear)
        ensureValidMonth('Date', month)
        ensureValidDay('Date', day)
        return this.dateCalculator.createDate(dateStringOrYear, month, day)
    }
    throw new Error("Failed to execute function 'Date' with parameters: " + [...arguments].join())
}

function FxDateTime(this: InternalFunctionScope, dateTimeString: string): Date {
    let dateTime
    if (ISO_DATETIME_NO_OFFSET.test(dateTimeString)) {
        dateTime = this.dateCalculator.createDateTime(dateTimeString, this.functionContext.zoneId)
    } else if (ISO_DATE.test(dateTimeString)) {
        const date = this.dateCalculator.createDate(dateTimeString)
        dateTime = this.dateCalculator.toDateTime(date, this.functionContext.zoneId)
    } else {
        dateTime = this.dateCalculator.createDateTime(dateTimeString)
    }
    if (dateTime.toString() === 'Invalid Date') {
        throw new Error(`Failed to execute function 'DateTime' with parameters: ${[...arguments].join()}`)
    }
    return dateTime
}

function PlusYears(this: InternalFunctionScope, dateArg?: Date, num?: number): Date {
    if (!dateArg) {
        throw new Error(message('PlusYears', message.reason.firstParam))
    }
    if (num == undefined) {
        throw new Error(message('PlusYears', message.reason.secondParam))
    }
    if (this.dateCalculator.isDate(dateArg)) {
        return this.dateCalculator.addDateField(dateArg, 'YEAR', num)
    }
    if (this.dateCalculator.isDateTime(dateArg)) {
        return this.dateCalculator.addDateTimeField(dateArg, 'YEAR', num, this.functionContext.zoneId)
    }
    throw new Error(`Failed to execute function 'PlusYears' with parameters: ${[...arguments].join()}`)
}

function PlusMonths(this: InternalFunctionScope, dateArg?: Date, num?: number): Date {
    if (!dateArg) {
        throw new Error(message('PlusMonths', message.reason.firstParam))
    }
    if (num == undefined) {
        throw new Error(message('PlusMonths', message.reason.secondParam))
    }
    if (this.dateCalculator.isDate(dateArg)) {
        return this.dateCalculator.addDateField(dateArg, 'MONTH', num)
    }
    if (this.dateCalculator.isDateTime(dateArg)) {
        return this.dateCalculator.addDateTimeField(dateArg, 'MONTH', num, this.functionContext.zoneId)
    }
    throw new Error(`Failed to execute function 'PlusMonths' with parameters: ${[...arguments].join()}`)
}

function PlusDays(this: InternalFunctionScope, dateArg?: Date, num?: number): Date {
    if (!dateArg) {
        throw new Error(message('PlusDays', message.reason.firstParam))
    }
    if (num == undefined) {
        throw new Error(message('PlusDays', message.reason.secondParam))
    }
    if (this.dateCalculator.isDate(dateArg)) {
        return this.dateCalculator.addDateField(dateArg, 'DAY_OF_MONTH', num)
    }
    if (this.dateCalculator.isDateTime(dateArg)) {
        return this.dateCalculator.addDateTimeField(dateArg, 'DAY_OF_MONTH', num, this.functionContext.zoneId)
    }
    throw new Error(`Failed to execute function 'PlusDays' with parameters: ${[...arguments].join()}`)
}

function AsDate(this: InternalFunctionScope, dateArg?: Date): Date {
    if (!dateArg) {
        throw new Error(message('AsDate', message.reason.firstParam))
    }
    return this.dateCalculator.toDate(dateArg, this.functionContext.zoneId)
}

function AsTime(this: InternalFunctionScope, dateArg?: Date): Date {
    if (!dateArg) {
        throw new Error(message('AsTime', message.reason.firstParam))
    }
    return this.dateCalculator.toDateTime(dateArg, this.functionContext.zoneId)
}

function IsDateBetween(this: InternalFunctionScope, dateToCheck?: Date, start?: Date, end?: Date): boolean {
    if (!dateToCheck) {
        throw new Error(message('IsDateBetween', message.reason.firstParam))
    }
    if (!start) {
        throw new Error(message('IsDateBetween', message.reason.secondParam))
    }
    if (!end) {
        throw new Error(message('IsDateBetween', message.reason.thirdParam))
    }
    return dateToCheck >= start && dateToCheck <= end
}

function GetDay(this: InternalFunctionScope, date?: Date): number {
    if (!date) {
        throw new Error(message('GetDay', message.reason.firstParam))
    }
    if (this.dateCalculator.isDate(date)) {
        return this.dateCalculator.getDateField(date, 'DAY_OF_MONTH')
    }
    if (this.dateCalculator.isDateTime(date)) {
        return this.dateCalculator.getDateTimeField(date, 'DAY_OF_MONTH', this.functionContext.zoneId)
    }
    throw new Error(`Failed to execute function 'GetDay' with parameters: ${[...arguments].join()}`)
}

function GetYear(this: InternalFunctionScope, date?: Date): number {
    if (!date) {
        throw new Error(message('GetYear', message.reason.firstParam))
    }
    if (this.dateCalculator.isDate(date)) {
        return this.dateCalculator.getDateField(date, 'YEAR')
    }
    if (this.dateCalculator.isDateTime(date)) {
        return this.dateCalculator.getDateTimeField(date, 'YEAR', this.functionContext.zoneId)
    }
    throw new Error(`Failed to execute function 'GetYear' with parameters: ${[...arguments].join()}`)
}

function GetMonth(this: InternalFunctionScope, date?: Date): number {
    if (!date) {
        throw new Error(message('GetMonth', message.reason.firstParam))
    }
    if (this.dateCalculator.isDate(date)) {
        return this.dateCalculator.getDateField(date, 'MONTH')
    }
    if (this.dateCalculator.isDateTime(date)) {
        return this.dateCalculator.getDateTimeField(date, 'MONTH', this.functionContext.zoneId)
    }
    throw new Error(`Failed to execute function 'GetMonth' with parameters: ${[...arguments].join()}`)
}

function WithYear(this: InternalFunctionScope, date: Date | undefined, year: number | undefined): Date {
    if (!date) {
        throw new Error(message('WithYear', message.reason.firstParam))
    }
    if (!year) {
        throw new Error(message('WithYear', message.reason.firstParam))
    }
    ensureValidYear('WithYear', year)
    if (this.dateCalculator.isDate(date)) {
        return this.dateCalculator.withDateField(date, 'YEAR', year)
    }
    if (this.dateCalculator.isDateTime(date)) {
        return this.dateCalculator.withDateTimeField(date, 'YEAR', year, this.functionContext.zoneId)
    }
    throw new Error(`Failed to execute function 'WithYear' with parameters: ${[...arguments].join()}`)
}

function WithMonth(this: InternalFunctionScope, date: Date | undefined, month: number | undefined): Date {
    if (!date) {
        throw new Error(message('WithMonth', message.reason.firstParam))
    }
    if (!month) {
        throw new Error(message('WithMonth', message.reason.firstParam))
    }
    ensureValidMonth('WithMonth', month)
    if (this.dateCalculator.isDate(date)) {
        return this.dateCalculator.withDateField(date, 'MONTH', month)
    }
    if (this.dateCalculator.isDateTime(date)) {
        return this.dateCalculator.withDateTimeField(date, 'MONTH', month, this.functionContext.zoneId)
    }
    throw new Error(`Failed to execute function 'WithMonth' with parameters: ${[...arguments].join()}`)
}

function WithDay(this: InternalFunctionScope, date: Date | undefined, day: number | undefined): Date {
    if (!date) {
        throw new Error(message('WithDay', message.reason.firstParam))
    }
    if (!day) {
        throw new Error(message('WithDay', message.reason.firstParam))
    }
    ensureValidMonth('WithDay', day)
    if (this.dateCalculator.isDate(date)) {
        return this.dateCalculator.withDateField(date, 'DAY_OF_MONTH', day)
    }
    if (this.dateCalculator.isDateTime(date)) {
        return this.dateCalculator.withDateTimeField(date, 'DAY_OF_MONTH', day, this.functionContext.zoneId)
    }
    throw new Error(`Failed to execute function 'WithDay' with parameters: ${[...arguments].join()}`)
}

function NumberOfDaysBetween(this: InternalFunctionScope, d1?: Date, d2?: Date): number {
    if (!d1) {
        throw new Error(message('NumberOfDaysBetween', message.reason.firstParam))
    }
    if (!d2) {
        throw new Error(message('NumberOfDaysBetween', message.reason.secondParam))
    }
    return this.dateCalculator.differenceBetweenDates(d1, d2, 'DAY_OF_MONTH')
}

function NumberOfMonthsBetween(this: InternalFunctionScope, d1?: Date, d2?: Date): number {
    if (!d1) {
        throw new Error(message('NumberOfMonthsBetween', message.reason.firstParam))
    }
    if (!d2) {
        throw new Error(message('NumberOfMonthsBetween', message.reason.secondParam))
    }
    return this.dateCalculator.differenceBetweenDates(d1, d2, 'MONTH')
}

function NumberOfYearsBetween(this: InternalFunctionScope, d1?: Date, d2?: Date): number {
    if (!d1) {
        throw new Error(message('NumberOfYearsBetween', message.reason.firstParam))
    }
    if (!d2) {
        throw new Error(message('NumberOfYearsBetween', message.reason.secondParam))
    }
    return this.dateCalculator.differenceBetweenDates(d1, d2, 'YEAR')
}

function Format(date?: Date, format = 'YYYY-MM-DD'): string {
    if (!date) {
        throw new Error(message('Format', message.reason.firstParam))
    }
    return moment(date).format(format)
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
