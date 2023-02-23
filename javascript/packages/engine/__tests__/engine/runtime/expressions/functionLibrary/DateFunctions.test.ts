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

import { dateFunctions as f } from '../../../../../src/engine/runtime/expressions/functionLibrary/DateFunctions'

describe('Date Functions', () => {
    it('should create date from numbers', () => {
        expect(f.Date(2011, 12, 31)).k_toBeDateEqualTo(new Date('2011-12-31'))
        expect(f.Date('2011-12-31')).k_toBeDateEqualTo(new Date('2011-12-31'))
        expect(f.Date('2011-11-11T00:00:00')).k_toBeDateEqualTo(new Date('2011-11-11'))
        expect(f.Date(2017, 1, 1)).k_toBeDateEqualTo(f.Date(2017, 1, 1))
        expect(f.Date(2018, 7, 1)).k_toBeDateEqualTo(f.Date(2018, 7, 1))
        expect(() => f.Date('22011-11-11')).toThrow()
        expect(() => f.Date('2011-11-1100:00:00')).toThrow()
        expect(() => f.Date(10000, 12, 31)).toThrow()
        expect(() => f.Date(2000, 13, 31)).toThrow()
        expect(() => f.Date(2000, 12, 33)).toThrow()
        expect(() => f.Date(2011)).toThrow()
        expect(() => f.Date()).toThrow()
    })
    it('should get day from date', () => {
        expect(f.GetDay(f.Date('2011-12-31'))).toBe(31)
        expect(() => f.GetDay()).toThrow()
        // @ts-expect-error testing negative case
        expect(() => f.GetDay(12)).toThrow()
        // @ts-expect-error testing negative case
        expect(() => f.GetDay('12')).toThrow()
    })
    it('should get month from date', () => {
        expect(f.GetMonth(f.Date('2011-12-31'))).toBe(12)
        expect(() => f.GetMonth()).toThrow()
        // @ts-expect-error testing negative case
        expect(() => f.GetMonth(12)).toThrow()
        // @ts-expect-error testing negative case
        expect(() => f.GetMonth('12')).toThrow()
    })
    it('should get year from date', () => {
        expect(f.GetYear(f.Date('2011-12-31'))).toBe(2011)
        expect(() => f.GetYear()).toThrow()
        // @ts-expect-error testing negative case
        expect(() => f.GetYear(12)).toThrow()
        // @ts-expect-error testing negative case
        expect(() => f.GetYear('12')).toThrow()
    })
    it('should count number of days between', () => {
        expect(f.NumberOfDaysBetween(f.Date(2018, 7, 1), f.Date(2018, 7, 10))).toBe(9)

        expect(f.NumberOfDaysBetween(f.Date('2017-01-01'), f.Date('2018-07-01'))).toBe(546)
        expect(f.NumberOfDaysBetween(f.Date(2017, 1, 1), f.Date(2018, 7, 1))).toBe(546)
        expect(f.NumberOfDaysBetween(f.Date(2018, 7, 1), f.Date(2017, 1, 1))).toBe(546)

        expect(f.NumberOfDaysBetween(f.Date(2018, 7, 10), f.Date(2018, 7, 1))).toBe(9)
    })
    it('should define is date between true', () => {
        expect(f.IsDateBetween(new Date(2018, 7, 5), new Date(2018, 7, 1), new Date(2018, 7, 10))).toBe(true)
        expect(f.IsDateBetween(new Date(2018, 7, 1), new Date(2018, 7, 1), new Date(2018, 7, 10))).toBe(true)
        expect(f.IsDateBetween(new Date(2018, 7, 10), new Date(2018, 7, 1), new Date(2018, 7, 10))).toBe(true)
    })
    it('should define is date between false', () => {
        expect(f.IsDateBetween(new Date(2018, 8, 5), new Date(2018, 7, 1), new Date(2018, 7, 10))).toBe(false)
    })
    it('should get number of months between', () => {
        expect(f.NumberOfMonthsBetween(f.Date(2011, 1, 15), f.Date(2011, 2, 15))).toBe(1)
        expect(f.NumberOfMonthsBetween(f.Date(2011, 1, 16), f.Date(2011, 2, 15))).toBe(0)
        expect(f.NumberOfMonthsBetween(f.Date(2011, 1, 1), f.Date(2011, 1, 31))).toBe(0)
        expect(f.NumberOfMonthsBetween(f.Date(2011, 1, 31), f.Date(2011, 2, 1))).toBe(0)
        expect(f.NumberOfMonthsBetween(f.Date(2012, 2, 28), f.Date(2011, 1, 31))).toBe(12)
        expect(f.NumberOfMonthsBetween(f.Date(2011, 1, 31), f.Date(2012, 2, 28))).toBe(12)
        // @ts-expect-error testing negative case
        expect(() => f.NumberOfMonthsBetween(1, f.Date(2012, 1, 31))).toThrow()
        // @ts-expect-error testing negative case
        expect(() => f.NumberOfMonthsBetween(f.Date(2012, 1, 31), 1)).toThrow()
        expect(() => f.NumberOfMonthsBetween(f.Date(2012, 1, 31))).toThrow()
        expect(() => f.NumberOfMonthsBetween()).toThrow()
    })
    it('should get number of years between', () => {
        expect(f.NumberOfYearsBetween(f.Date(2011, 1, 15), f.Date(2011, 2, 15))).toBe(0)
        expect(f.NumberOfYearsBetween(f.Date(2011, 12, 31), f.Date(2012, 1, 30))).toBe(0)
        expect(f.NumberOfYearsBetween(f.Date(2011, 10, 31), f.Date(2012, 10, 30))).toBe(0)
        expect(f.NumberOfYearsBetween(f.Date(2010, 1, 10), f.Date(2011, 1, 1))).toBe(0)
        expect(f.NumberOfYearsBetween(f.Date(2010, 1, 16), f.Date(2011, 1, 16))).toBe(1)
        expect(f.NumberOfYearsBetween(f.Date(2010, 1, 16), f.Date(2011, 12, 16))).toBe(1)
        expect(f.NumberOfYearsBetween(f.Date(2022, 1, 31), f.Date(2011, 1, 31))).toBe(11)
        expect(f.NumberOfYearsBetween(f.Date(2011, 1, 31), f.Date(2022, 1, 31))).toBe(11)
        // @ts-expect-error testing negative case
        expect(() => f.NumberOfYearsBetween(1, f.Date(2012, 1, 31))).toThrow()
        // @ts-expect-error testing negative case
        expect(() => f.NumberOfYearsBetween(f.Date(2012, 1, 31), 1)).toThrow()
        expect(() => f.NumberOfYearsBetween(f.Date(2012, 1, 31))).toThrow()
        expect(() => f.NumberOfYearsBetween()).toThrow()
    })
    it('should format date', () => {
        expect(f.Format(f.Date(2011, 1, 15), 'YYYY-MM-DD')).toBe('2011-01-15')
        expect(f.Format(f.Date(2011, 1, 15), 'YY-MM-DD')).toBe('11-01-15')
        expect(f.Format(f.Date(2011, 1, 15))).toBe('2011-01-15')
        expect(f.Format(f.Date(2011, 1, 15), 'MM-DD-YYYY')).toBe('01-15-2011')
        expect(f.Format(f.Date(2011, 1, 15), 'DD-MM-YYYY')).toBe('15-01-2011')
        expect(f.Format(f.Date(2011, 1, 15), 'DD MM YYYY')).toBe('15 01 2011')
        expect(f.Format(f.Date(2011, 1, 15), 'DD:MM:YYYY')).toBe('15:01:2011')
        expect(f.Format(f.Date(2011, 1, 15), 'DD/MM/YYYY')).toBe('15/01/2011')
        expect(f.Format(f.Date(2011, 1, 15), 'DD/MM-=-YYYY')).toBe('15/01-=-2011')
        expect(() => f.Format()).toThrow()
    })
    it('should create date from time', () => {
        const date = new Date(2011, 11, 11, 59, 58)
        expect(f.AsDate(date).getMinutes()).toBe(0)
        expect(f.AsDate(date).getHours()).toBe(0)
    })
    it('should create date time from date', () => {
        const date = new Date(2011, 11, 11, 59, 58)
        expect(f.AsTime(date).getHours()).toBe(0)
        expect(f.AsTime(date).getMinutes()).toBe(0)
    })
    it('should throw on undefined parameters', () => {
        expect(() => f.AsDate()).toThrow()
        expect(() => f.AsTime()).toThrow()
        expect(() => f.PlusDays()).toThrow()
        expect(() => f.PlusDays(new Date())).toThrow()
        expect(() => f.PlusMonths()).toThrow()
        expect(() => f.PlusMonths(new Date())).toThrow()
        expect(() => f.PlusYears(new Date())).toThrow()
        expect(() => f.PlusYears()).toThrow()
        expect(() => f.PlusYears(new Date())).toThrow()
        expect(() => f.NumberOfDaysBetween()).toThrow()
        expect(() => f.NumberOfDaysBetween(new Date())).toThrow()
        expect(() => f.IsDateBetween()).toThrow()
        expect(() => f.IsDateBetween(new Date())).toThrow()
        expect(() => f.IsDateBetween(new Date(), new Date())).toThrow()
    })
    it('should change year in date', () => {
        const date = new Date('2011-01-01')
        expect(f.WithYear(date, 2012)).k_toBeDateEqualTo(new Date('2012-01-01'))

        const dateTime = new Date('2011-01-01T01:01:01Z')
        expect(f.WithYear(dateTime, 2012)).k_toBeDateTimeEqualTo(new Date('2012-01-01T01:01:01Z'))

        const date2 = new Date('2000-02-29')
        expect(f.WithYear(date2, 2001)).k_toBeDateEqualTo(new Date('2001-02-28'))

        expect(() => f.WithYear(undefined, 2012)).toThrow()
        expect(() => f.WithYear(date, undefined)).toThrow()
        expect(() => f.WithYear(date, 0)).toThrow()
        expect(() => f.WithYear(date, 10000)).toThrow()
    })
    it('should change year in date and preserve time', () => {
        const dateTime = new Date('2000-02-29T01:01:01Z')
        expect(f.WithYear(dateTime, 1999)).k_toBeDateTimeEqualTo(new Date('1999-02-28T01:01:01Z'))
    })
    it('should change month in date', () => {
        const date = new Date('2011-01-01')
        expect(f.WithMonth(date, 2)).k_toBeDateEqualTo(new Date('2011-02-01'))

        const dateTime = new Date('2011-01-01T01:01:01Z')
        expect(f.WithMonth(dateTime, 2)).k_toBeDateTimeEqualTo(new Date('2011-02-01T01:01:01Z'))

        const date2 = new Date('2000-01-30')
        expect(f.WithMonth(date2, 2)).k_toBeDateEqualTo(new Date('2000-02-29'))

        expect(() => f.WithMonth(undefined, 2012)).toThrow()
        expect(() => f.WithMonth(date, undefined)).toThrow()
        expect(() => f.WithMonth(date, 0)).toThrow()
        expect(() => f.WithMonth(date, 13)).toThrow()
    })
    it('should change month in date and preserve time', () => {
        const dateTime = new Date('2011-03-30T23:33:33Z')
        expect(f.WithMonth(dateTime, 2)).k_toBeDateTimeEqualTo(new Date('2011-02-28T23:33:33Z'))
    })
    it('should change day in date', () => {
        const date = new Date('2011-01-01')
        expect(f.WithDay(date, 2)).k_toBeDateEqualTo(new Date('2011-01-02'))

        const dateTime = new Date('2011-01-01T01:01:01Z')
        expect(f.WithDay(dateTime, 2)).k_toBeDateTimeEqualTo(new Date('2011-01-02T01:01:01Z'))

        expect(() => f.WithDay(undefined, 2012)).toThrow()
        expect(() => f.WithDay(date, undefined)).toThrow()
        expect(() => f.WithDay(date, 0)).toThrow()
        expect(() => f.WithDay(date, 32)).toThrow()

        const date2 = new Date(2000, 1, 28)
        expect(() => f.WithDay(date2, 30)).toThrow()
    })
    it('should change day in date and preserve time', () => {
        const date = new Date('1999-03-31T23:33:33Z')
        expect(f.WithDay(date, 10)).k_toBeDateTimeEqualTo(new Date('1999-03-10T23:33:33Z'))
    })
    it('should add days', () => {
        const date = new Date('2000-01-01')

        expect(f.PlusDays(date, 1)).k_toBeDateEqualTo(new Date('2000-01-02'))
        expect(f.PlusDays(date, 32)).k_toBeDateEqualTo(new Date('2000-02-02'))
    })
    it('should add months', () => {
        const date = new Date('2000-01-30')

        expect(f.PlusMonths(date, 1)).k_toBeDateEqualTo(new Date('2000-02-29'))
        expect(f.PlusMonths(date, 13)).k_toBeDateEqualTo(new Date('2001-02-28'))
    })
    it('should add years', () => {
        const date = new Date('2000-02-29')

        expect(f.PlusYears(date, 1)).k_toBeDateEqualTo(new Date('2001-02-28'))
        expect(f.PlusYears(date, -1)).k_toBeDateEqualTo(new Date('1999-02-28'))
    })
    it('should add days to date and preserve time', () => {
        const date = new Date('2000-01-01T07:33:33Z')
        expect(f.PlusDays(date, 1)).k_toBeDateTimeEqualTo(new Date('2000-01-02T07:33:33Z'))
        expect(f.PlusDays(date, 32)).k_toBeDateTimeEqualTo(new Date('2000-02-02T07:33:33Z'))

        const date2 = new Date('2005-03-09T01:01:01Z')
        expect(f.PlusDays(date2, 30)).k_toBeDateTimeEqualTo(new Date('2005-04-08T01:01:01Z'))

        const date3 = new Date('2005-04-08T23:33:33Z')
        expect(f.PlusDays(date3, -30)).k_toBeDateTimeEqualTo(new Date('2005-03-09T23:33:33Z'))
    })
    it('should add months to date and preserve time', () => {
        const date = new Date('2000-01-30T07:33:33Z')

        expect(f.PlusMonths(date, 1)).k_toBeDateTimeEqualTo(new Date('2000-02-29T07:33:33Z'))
        expect(f.PlusMonths(date, 13)).k_toBeDateTimeEqualTo(new Date('2001-02-28T07:33:33Z'))

        const date3 = new Date('2005-03-09T01:01:01Z')

        expect(f.PlusMonths(date3, -12)).k_toBeDateTimeEqualTo(new Date('2004-03-09T01:01:01Z'))
        expect(f.PlusMonths(date3, 1)).k_toBeDateTimeEqualTo(new Date('2005-04-09T01:01:01Z'))

        const date2 = new Date('2005-03-09T23:33:33Z')

        expect(f.PlusMonths(date2, -12)).k_toBeDateTimeEqualTo(new Date('2004-03-09T23:33:33Z'))
        expect(f.PlusMonths(date2, 1)).k_toBeDateTimeEqualTo(new Date('2005-04-09T23:33:33Z'))
    })
    it('should add years to date and preserve time', () => {
        const date = new Date('2000-02-29T07:33:33Z')

        expect(f.PlusYears(date, 1)).k_toBeDateTimeEqualTo(new Date('2001-02-28T07:33:33Z'))
        expect(f.PlusYears(date, -1)).k_toBeDateTimeEqualTo(new Date('1999-02-28T07:33:33Z'))

        const date2 = new Date('2004-02-29T01:01:01Z')

        expect(f.PlusYears(date2, 1)).k_toBeDateTimeEqualTo(new Date('2005-02-28T01:01:01Z'))
        expect(f.PlusYears(date2, -1)).k_toBeDateTimeEqualTo(new Date('2003-02-28T01:01:01Z'))

        const date3 = new Date('2004-02-29T23:33:33Z')

        expect(f.PlusYears(date3, 1)).k_toBeDateTimeEqualTo(new Date('2005-02-28T23:33:33Z'))
        expect(f.PlusYears(date3, -1)).k_toBeDateTimeEqualTo(new Date('2003-02-28T23:33:33Z'))
    })
    it('should add years to date and preserve on new years eve', () => {
        const date = new Date('2004-12-31T23:33:33Z')

        expect(f.PlusYears(date, 1)).k_toBeDateTimeEqualTo(new Date('2005-12-31T23:33:33Z'))
        expect(f.PlusYears(date, -1)).k_toBeDateTimeEqualTo(new Date('2003-12-31T23:33:33Z'))

        const date2 = new Date('2005-01-01T01:01:01Z')

        expect(f.PlusYears(date2, 1)).k_toBeDateTimeEqualTo(new Date('2006-01-01T01:01:01Z'))
        expect(f.PlusYears(date2, -1)).k_toBeDateTimeEqualTo(new Date('2004-01-01T01:01:01Z'))
    })
})
