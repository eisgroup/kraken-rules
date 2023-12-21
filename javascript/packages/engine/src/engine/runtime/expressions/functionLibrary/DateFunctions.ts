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
import { DateCalculator, DateField, ISO_DATE, ISO_DATETIME_NO_OFFSET, ISO_DATETIME_ZULU } from '../date/DateCalculator'
import { ensureValidDay, ensureValidMonth, ensureValidYear } from '../math/Temporal'

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

function Today(this: InternalFunctionScope): unknown {
    return this.dateCalculator.today(this.functionContext.zoneId)
}

function Now(this: InternalFunctionScope): unknown {
    return this.dateCalculator.now()
}

function FxDate(dateString?: string): unknown
function FxDate(year?: number, month?: number, day?: number): unknown

function FxDate(
    this: InternalFunctionScope,
    dateStringOrYear?: string | number,
    month?: number,
    day?: number,
): unknown {
    const dc = this.dateCalculator
    const zoneId = this.functionContext.zoneId
    if (typeof dateStringOrYear === 'string') {
        if (ISO_DATETIME_NO_OFFSET.test(dateStringOrYear)) {
            return dc.toDate(dc.createDateTime(dateStringOrYear, zoneId), zoneId)
        } else if (ISO_DATE.test(dateStringOrYear)) {
            return dc.createDate(dateStringOrYear)
        } else if (ISO_DATETIME_ZULU.test(dateStringOrYear)) {
            return dc.toDate(dc.createDateTime(dateStringOrYear), zoneId)
        }
    }
    if (typeof dateStringOrYear === 'number' && month != undefined && day != undefined) {
        validateYear('Date', dateStringOrYear)
        validateMonth('Date', month)
        validateDay('Date', day)
        return dc.createDate(dateStringOrYear, month, day)
    }
    throw new Error("Failed to execute function 'Date' with parameters: " + [...arguments].join())
}

function FxDateTime(this: InternalFunctionScope, dateTimeString: string): unknown {
    const dc = this.dateCalculator
    const zoneId = this.functionContext.zoneId

    if (ISO_DATETIME_NO_OFFSET.test(dateTimeString)) {
        return dc.createDateTime(dateTimeString, zoneId)
    } else if (ISO_DATE.test(dateTimeString)) {
        return dc.toDateTime(dc.createDate(dateTimeString), zoneId)
    } else if (ISO_DATETIME_ZULU.test(dateTimeString)) {
        return dc.createDateTime(dateTimeString)
    }

    throw new Error("Failed to execute function 'DateTime' with parameters: " + [...arguments].join())
}

function AsDate(this: InternalFunctionScope, dateTimeArg?: unknown): unknown {
    if (!dateTimeArg) {
        throw new Error(message('AsDate', message.reason.firstParam))
    }
    if (this.dateCalculator.isDateTime(dateTimeArg)) {
        return this.dateCalculator.toDate(dateTimeArg, this.functionContext.zoneId)
    }
    throw new Error(message('AsDate', 'First parameter must be DATETIME value.'))
}

function AsTime(this: InternalFunctionScope, dateArg?: unknown): unknown {
    if (!dateArg) {
        throw new Error(message('AsTime', message.reason.firstParam))
    }
    if (this.dateCalculator.isDate(dateArg)) {
        return this.dateCalculator.toDateTime(dateArg, this.functionContext.zoneId)
    }
    throw new Error(message('AsTime', 'First parameter must be DATE value.'))
}

function IsDateBetween(this: InternalFunctionScope, dateToCheck?: unknown, start?: unknown, end?: unknown): boolean {
    if (!dateToCheck) {
        throw new Error(message('IsDateBetween', message.reason.firstParam))
    }
    if (!start) {
        throw new Error(message('IsDateBetween', message.reason.secondParam))
    }
    if (!end) {
        throw new Error(message('IsDateBetween', message.reason.thirdParam))
    }
    if (!this.dateCalculator.isDate(dateToCheck)) {
        throw new Error(message('IsDateBetween', 'First parameter must be a DATE value.'))
    }
    if (!this.dateCalculator.isDate(start)) {
        throw new Error(message('IsDateBetween', 'Second parameter must be a DATE value.'))
    }
    if (!this.dateCalculator.isDate(end)) {
        throw new Error(message('IsDateBetween', 'Third parameter must be a DATE value.'))
    }

    const dateToCheckNormalized = this.dateCalculator.convertDateToJavascriptDate(dateToCheck)
    const startNormalized = this.dateCalculator.convertDateToJavascriptDate(start)
    const endNormalized = this.dateCalculator.convertDateToJavascriptDate(end)

    return (
        dateToCheckNormalized.getTime() >= startNormalized.getTime() &&
        dateToCheckNormalized.getTime() <= endNormalized.getTime()
    )
}

function PlusYears(this: InternalFunctionScope, dateArg?: unknown, num?: number): unknown {
    return plusField('PlusYears', dateArg, num, 'YEAR', this.dateCalculator, this.functionContext.zoneId)
}

function PlusMonths(this: InternalFunctionScope, dateArg?: unknown, num?: number): unknown {
    return plusField('PlusMonths', dateArg, num, 'MONTH', this.dateCalculator, this.functionContext.zoneId)
}

function PlusDays(this: InternalFunctionScope, dateArg?: unknown, num?: number): unknown {
    return plusField('PlusDays', dateArg, num, 'DAY_OF_MONTH', this.dateCalculator, this.functionContext.zoneId)
}

function GetYear(this: InternalFunctionScope, date?: unknown): number {
    return getField('GetYear', date, 'YEAR', this)
}

function GetMonth(this: InternalFunctionScope, date?: unknown): number {
    return getField('GetMonth', date, 'MONTH', this)
}

function GetDay(this: InternalFunctionScope, date?: unknown): number {
    return getField('GetDay', date, 'DAY_OF_MONTH', this)
}

function WithYear(this: InternalFunctionScope, date: unknown, year: number | undefined): unknown {
    return withField('WithYear', date, year, 'YEAR', this)
}

function WithMonth(this: InternalFunctionScope, date: unknown, month: number | undefined): unknown {
    return withField('WithMonth', date, month, 'MONTH', this)
}

function WithDay(this: InternalFunctionScope, date: unknown, day: number | undefined): unknown {
    return withField('WithDay', date, day, 'DAY_OF_MONTH', this)
}

function getField(functionName: string, date: unknown, field: DateField, fc: InternalFunctionScope): number {
    if (!date) {
        throw new Error(message(functionName, message.reason.firstParam))
    }

    const dc = fc.dateCalculator
    const zoneId = fc.functionContext.zoneId

    if (dc.isDate(date)) {
        return dc.getDateField(date, field)
    }
    if (dc.isDateTime(date)) {
        return dc.getDateTimeField(date, field, zoneId)
    }
    throw new Error(`Failed to execute function '${functionName}' with parameters: ${[...arguments].join()}`)
}

function withField(
    functionName: string,
    date: unknown,
    value: number | undefined,
    field: DateField,
    fc: InternalFunctionScope,
): unknown {
    if (!date) {
        throw new Error(message(functionName, message.reason.firstParam))
    }
    if (value == undefined) {
        throw new Error(message(functionName, message.reason.firstParam))
    }

    if (field === 'YEAR') {
        validateYear(functionName, value)
    } else if (field === 'MONTH') {
        validateMonth(functionName, value)
    } else if (field === 'DAY_OF_MONTH') {
        validateDay(functionName, value)
    }

    const dc = fc.dateCalculator
    const zoneId = fc.functionContext.zoneId

    if (dc.isDate(date)) {
        return dc.withDateField(date, field, value)
    }
    if (dc.isDateTime(date)) {
        return dc.withDateTimeField(date, field, value, zoneId)
    }
    throw new Error(`Failed to execute function '${functionName}' with parameters: ${[...arguments].join()}`)
}

function plusField(
    functionName: string,
    date: unknown,
    value: number | undefined,
    field: DateField,
    dc: DateCalculator<unknown, unknown>,
    zoneId: string,
): unknown {
    if (!date) {
        throw new Error(message(functionName, message.reason.firstParam))
    }
    if (value == undefined) {
        throw new Error(message(functionName, message.reason.secondParam))
    }

    if (dc.isDate(date)) {
        return dc.addDateField(date, field, value)
    }
    if (dc.isDateTime(date)) {
        return dc.addDateTimeField(date, field, value, zoneId)
    }
    throw new Error(`Failed to execute function '${functionName}' with parameters: ${[...arguments].join()}`)
}

function Format(date?: Date, format = 'YYYY-MM-DD'): string {
    if (!date) {
        throw new Error(message('Format', message.reason.firstParam))
    }
    return moment(date).format(format)
}

function NumberOfDaysBetween(this: InternalFunctionScope, d1?: unknown, d2?: unknown): number {
    return calculateDifference('NumberOfDaysBetween', 'DAY_OF_MONTH', this.dateCalculator, d1, d2)
}

function NumberOfMonthsBetween(this: InternalFunctionScope, d1?: unknown, d2?: unknown): number {
    return calculateDifference('NumberOfDaysBetween', 'MONTH', this.dateCalculator, d1, d2)
}

function NumberOfYearsBetween(this: InternalFunctionScope, d1?: unknown, d2?: unknown): number {
    return calculateDifference('NumberOfDaysBetween', 'YEAR', this.dateCalculator, d1, d2)
}

function calculateDifference(
    functionName: string,
    field: DateField,
    dc: DateCalculator<unknown, unknown>,
    d1: unknown,
    d2: unknown,
): number {
    if (!d1) {
        throw new Error(message(functionName, message.reason.firstParam))
    }
    if (!d2) {
        throw new Error(message(functionName, message.reason.secondParam))
    }

    if (dc.isDate(d1) && dc.isDate(d2)) {
        return dc.differenceBetweenDates(d1, d2, field)
    }

    throw new Error(`Failed to execute function '${functionName}' with parameters: ${[...arguments].join()}`)
}

function validateYear(functionName: string, year: number): void {
    try {
        ensureValidYear(year)
    } catch (e) {
        const m = e instanceof Error ? e.message : e
        throw new Error(`Failed to execute function '${functionName}'. ${m}`)
    }
}

function validateMonth(functionName: string, month: number): void {
    try {
        ensureValidMonth(month)
    } catch (e) {
        const m = e instanceof Error ? e.message : e
        throw new Error(`Failed to execute function '${functionName}'. ${m}`)
    }
}

function validateDay(functionName: string, day: number): void {
    try {
        ensureValidDay(day)
    } catch (e) {
        const m = e instanceof Error ? e.message : e
        throw new Error(`Failed to execute function '${functionName}'. ${m}`)
    }
}
