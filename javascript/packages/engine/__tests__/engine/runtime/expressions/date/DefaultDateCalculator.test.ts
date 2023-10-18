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
import { DefaultDateCalculator } from '../../../../../src/engine/runtime/expressions/date/DateCalculator'

function d(date: string): Date {
    return new Date(`${date}T00:00:00`)
}

describe('DefaultDateCalculator', () => {
    const calculator = new DefaultDateCalculator()
    const tz = Intl.DateTimeFormat().resolvedOptions().timeZone
    it('should create Date', () => {
        expect(calculator.createDate(2020, 1, 23)).toStrictEqual(new Date('2020-01-23T00:00:00'))
        expect(() => calculator.createDate(2020, 0, 1)).toThrow()

        expect(() => calculator.createDate(2020, 13, 1)).toThrow()
        expect(() => calculator.createDate(2020, 1, 0)).toThrow()

        expect(calculator.createDate('2020-01-23')).toStrictEqual(new Date('2020-01-23T00:00:00'))
        //expect(() => calculator.createDate("2020-02-30")).toThrow() // javascript allows values to overflow
        expect(() => calculator.createDate('2020-00-01')).toThrow()
        expect(() => calculator.createDate('2020-13-01')).toThrow()
        expect(() => calculator.createDate('2020-01-00')).toThrow()
        //expect(() => calculator.createDate("2020-01-23T10:00:00")).toThrow() // javascript allows values to overflow
        //expect(() => calculator.createDate("2020-01-23T10:00:00Z")).toThrow() // javascript allows values to overflow
    })
    it('should create DateTime', () => {
        expect(() => calculator.createDateTime('2020-01-23')).toThrow()
        expect(() => calculator.createDateTime('2020-01-23T10:00:00')).toThrow()
        // expect(() => calculator.createDateTime("2020-02-30T10:00:00Z")).toThrow() // javascript allows values to overflow
        expect(() => calculator.createDateTime('2020-00-01T10:00:00Z')).toThrow()
        expect(() => calculator.createDateTime('2020-13-01T10:00:00Z')).toThrow()
        expect(() => calculator.createDateTime('2020-01-00T10:00:00Z')).toThrow()
        expect(() => calculator.createDateTime('2020-01-23T25:00:00Z')).toThrow()
        expect(() => calculator.createDateTime('2020-01-23T10:60:00Z')).toThrow()
        expect(() => calculator.createDateTime('2020-01-23T10:00:60Z')).toThrow()

        expect(calculator.createDateTime('2020-01-23T10:00:00', tz)).toStrictEqual(new Date('2020-01-23T10:00:00'))
        expect(calculator.createDateTime('2020-01-23T10:00:00Z')).toStrictEqual(new Date('2020-01-23T10:00:00Z'))

        expect(calculator.createDateTime(2020, 1, 23, 10, 0, 0, tz)).toStrictEqual(new Date('2020-01-23T10:00:00'))
    })
    it('should return today', () => {
        const now = new Date()
        const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
        expect(calculator.today(tz)).toStrictEqual(today)
    })
    it('should convert Date to DateTime', () => {
        expect(calculator.toDateTime(d('2020-01-23'), tz)).toStrictEqual(new Date('2020-01-23T00:00:00'))
    })
    it('should convert DateTime to Date', () => {
        expect(calculator.toDate(d('2020-01-23'), tz)).toStrictEqual(new Date('2020-01-23T00:00:00'))
    })
    it('should get Date field', () => {
        const date = d('2020-01-30')
        expect(calculator.getDateField(date, 'YEAR')).toBe(2020)
        expect(calculator.getDateField(date, 'MONTH')).toBe(1)
        expect(calculator.getDateField(date, 'DAY_OF_MONTH')).toBe(30)
    })
    it('should set Date field', () => {
        const date = d('2020-01-30')
        expect(calculator.withDateField(date, 'YEAR', 2021)).toStrictEqual(new Date('2021-01-30T00:00:00'))
        expect(calculator.withDateField(date, 'MONTH', 3)).toStrictEqual(new Date('2020-03-30T00:00:00'))
        expect(calculator.withDateField(date, 'MONTH', 2)).toStrictEqual(new Date('2020-02-29T00:00:00'))
        expect(calculator.withDateField(date, 'DAY_OF_MONTH', 10)).toStrictEqual(new Date('2020-01-10T00:00:00'))

        expect(() => calculator.withDateField(date, 'MONTH', 13)).toThrow()
        expect(() => calculator.withDateField(date, 'DAY_OF_MONTH', 32)).toThrow()
    })
    it('should add Date field', () => {
        const date = d('2020-01-30')
        expect(calculator.addDateField(date, 'YEAR', 1)).toStrictEqual(new Date('2021-01-30T00:00:00'))
        expect(calculator.addDateField(date, 'MONTH', 1)).toStrictEqual(new Date('2020-02-29T00:00:00'))
        expect(calculator.addDateField(date, 'MONTH', 12)).toStrictEqual(new Date('2021-01-30T00:00:00'))
        expect(calculator.addDateField(date, 'DAY_OF_MONTH', 31)).toStrictEqual(new Date('2020-03-01T00:00:00'))
        expect(calculator.addDateField(date, 'DAY_OF_MONTH', 365)).toStrictEqual(new Date('2021-01-29T00:00:00'))
    })
    it('should diff Date field', () => {
        expect(calculator.differenceBetweenDates(d('2020-01-30'), d('2022-01-30'), 'YEAR')).toBe(2)
        expect(calculator.differenceBetweenDates(d('2020-01-30'), d('2022-01-29'), 'YEAR')).toBe(1)
        expect(calculator.differenceBetweenDates(d('2020-01-30'), d('2020-01-29'), 'YEAR')).toBe(0)

        expect(calculator.differenceBetweenDates(d('2020-01-29'), d('2020-02-29'), 'MONTH')).toBe(1)
        expect(calculator.differenceBetweenDates(d('2020-01-30'), d('2020-02-29'), 'MONTH')).toBe(0)
        expect(calculator.differenceBetweenDates(d('2020-01-30'), d('2020-03-01'), 'MONTH')).toBe(1)
        expect(calculator.differenceBetweenDates(d('2020-01-29'), d('2021-01-28'), 'MONTH')).toBe(11)

        expect(calculator.differenceBetweenDates(d('2020-01-29'), d('2020-02-29'), 'DAY_OF_MONTH')).toBe(31)
    })
    it('should get DateTime field', () => {
        const date = new Date('2020-01-30T10:10:13')
        expect(calculator.getDateTimeField(date, 'YEAR', tz)).toBe(2020)
        expect(calculator.getDateTimeField(date, 'MONTH', tz)).toBe(1)
        expect(calculator.getDateTimeField(date, 'DAY_OF_MONTH', tz)).toBe(30)
        expect(calculator.getDateTimeField(date, 'HOUR', tz)).toBe(10)
        expect(calculator.getDateTimeField(date, 'MINUTE', tz)).toBe(10)
        expect(calculator.getDateTimeField(date, 'SECOND', tz)).toBe(13)
    })
    it('should set DateTime field', () => {
        const date = new Date('2020-01-30T10:10:13')

        expect(calculator.withDateTimeField(date, 'YEAR', 2021, tz)).toStrictEqual(new Date('2021-01-30T10:10:13'))
        expect(calculator.withDateTimeField(date, 'MONTH', 3, tz)).toStrictEqual(new Date('2020-03-30T10:10:13'))
        expect(calculator.withDateTimeField(date, 'DAY_OF_MONTH', 10, tz)).toStrictEqual(
            new Date('2020-01-10T10:10:13'),
        )
        expect(calculator.withDateTimeField(date, 'HOUR', 23, tz)).toStrictEqual(new Date('2020-01-30T23:10:13'))
        expect(calculator.withDateTimeField(date, 'MINUTE', 5, tz)).toStrictEqual(new Date('2020-01-30T10:05:13'))
        expect(calculator.withDateTimeField(date, 'SECOND', 0, tz)).toStrictEqual(new Date('2020-01-30T10:10:00'))

        expect(calculator.withDateTimeField(date, 'MONTH', 2, tz)).toStrictEqual(new Date('2020-02-29T10:10:13'))

        expect(() => calculator.withDateTimeField(date, 'MONTH', 13, tz)).toThrow()
        expect(() => calculator.withDateTimeField(date, 'DAY_OF_MONTH', 32, tz)).toThrow()
        expect(() => calculator.withDateTimeField(date, 'HOUR', 24, tz)).toThrow()
        expect(() => calculator.withDateTimeField(date, 'MINUTE', 60, tz)).toThrow()
        expect(() => calculator.withDateTimeField(date, 'SECOND', 60, tz)).toThrow()
    })
    it('should add DateTime field', () => {
        const date = new Date('2020-01-30T10:10:13')

        expect(calculator.addDateTimeField(date, 'YEAR', 1, tz)).toStrictEqual(new Date('2021-01-30T10:10:13'))
        expect(calculator.addDateTimeField(date, 'MONTH', 1, tz)).toStrictEqual(new Date('2020-02-29T10:10:13'))
        expect(calculator.addDateTimeField(date, 'DAY_OF_MONTH', 31, tz)).toStrictEqual(new Date('2020-03-01T10:10:13'))
        expect(calculator.addDateTimeField(date, 'MONTH', 12, tz)).toStrictEqual(new Date('2021-01-30T10:10:13'))
        expect(calculator.addDateTimeField(date, 'DAY_OF_MONTH', 365, tz)).toStrictEqual(
            new Date('2021-01-29T10:10:13'),
        )

        expect(calculator.addDateTimeField(date, 'HOUR', 2, tz)).toStrictEqual(new Date('2020-01-30T12:10:13'))
        expect(calculator.addDateTimeField(date, 'HOUR', 26, tz)).toStrictEqual(new Date('2020-01-31T12:10:13'))
        expect(calculator.addDateTimeField(date, 'MINUTE', 61, tz)).toStrictEqual(new Date('2020-01-30T11:11:13'))
        expect(calculator.addDateTimeField(date, 'SECOND', 61, tz)).toStrictEqual(new Date('2020-01-30T10:11:14'))
    })
    it('should differentiate Date and DateTime', () => {
        const date = new Date('2020-01-30T10:10:13')
        expect(calculator.isDate(date)).toBeTruthy()
        expect(calculator.isDateTime(date)).toBeTruthy()
    })
    it('should throw if timezone not equal to system', () => {
        expect(() => calculator.today('Africa/Mbabane')).toThrow()
    })
})
