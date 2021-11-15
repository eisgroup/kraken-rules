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

import { dateFunctions as f } from "../../../../../src/engine/runtime/expressions/functionLibrary/DateFunctions";

describe("Date Functions", () => {
    it("should create date from numbers", () => {
        expect(f.Date(2011, 12, 31)).k_toBeDate(new Date(2011, 12, 31));
        expect(f.Date("2011-12-31")).k_toBeDate(new Date(2011, 12, 31));
        expect(() => f.Date(2011)).toThrow();
        expect(() => f.Date()).toThrow();
    });
    it("should get day from date", () => {
        expect(f.GetDay(f.Date("2011-12-31"))).toBe(31);
        expect(() => f.GetDay()).toThrow();
        // @ts-expect-error
        expect(() => f.GetDay(12)).toThrow();
        // @ts-expect-error
        expect(() => f.GetDay("12")).toThrow();
    });
    it("should get month from date", () => {
        expect(f.GetMonth(f.Date("2011-12-31"))).toBe(12);
        expect(() => f.GetMonth()).toThrow();
        // @ts-expect-error
        expect(() => f.GetMonth(12)).toThrow();
        // @ts-expect-error
        expect(() => f.GetMonth("12")).toThrow();
    });
    it("should get year from date", () => {
        expect(f.GetYear(f.Date("2011-12-31"))).toBe(2011);
        expect(() => f.GetYear()).toThrow();
        // @ts-expect-error
        expect(() => f.GetYear(12)).toThrow();
        // @ts-expect-error
        expect(() => f.GetYear("12")).toThrow();
    });
    it("should get number of months between", () => {
        expect(f.NumberOfMonthsBetween(f.Date(2011, 1, 15), f.Date(2011, 2, 15))).toBe(1);
        expect(f.NumberOfMonthsBetween(f.Date(2011, 1, 16), f.Date(2011, 2, 15))).toBe(0);
        expect(f.NumberOfMonthsBetween(f.Date(2011, 1, 1), f.Date(2011, 1, 31))).toBe(0);
        expect(f.NumberOfMonthsBetween(f.Date(2011, 1, 31), f.Date(2011, 2, 1))).toBe(0);
        expect(f.NumberOfMonthsBetween(f.Date(2012, 2, 28), f.Date(2011, 1, 31))).toBe(12);
        expect(f.NumberOfMonthsBetween(f.Date(2011, 1, 31), f.Date(2012, 2, 28))).toBe(12);
        // @ts-expect-error
        expect(() => f.NumberOfMonthsBetween(1, f.Date(2012, 1, 31))).toThrow();
        // @ts-expect-error
        expect(() => f.NumberOfMonthsBetween(f.Date(2012, 1, 31), 1)).toThrow();
        expect(() => f.NumberOfMonthsBetween(f.Date(2012, 1, 31))).toThrow();
        expect(() => f.NumberOfMonthsBetween()).toThrow();
    });
    it("should get number of years between", () => {
        expect(f.NumberOfYearsBetween(f.Date(2011, 1, 15), f.Date(2011, 2, 15))).toBe(0);
        expect(f.NumberOfYearsBetween(f.Date(2011, 12, 31), f.Date(2012, 1, 30))).toBe(0);
        expect(f.NumberOfYearsBetween(f.Date(2011, 10, 31), f.Date(2012, 10, 30))).toBe(0);
        expect(f.NumberOfYearsBetween(f.Date(2010, 1, 10), f.Date(2011, 1, 1))).toBe(0);
        expect(f.NumberOfYearsBetween(f.Date(2010, 1, 16), f.Date(2011, 1, 16))).toBe(1);
        expect(f.NumberOfYearsBetween(f.Date(2010, 1, 16), f.Date(2011, 12, 16))).toBe(1);
        expect(f.NumberOfYearsBetween(f.Date(2022, 1, 31), f.Date(2011, 1, 31))).toBe(11);
        expect(f.NumberOfYearsBetween(f.Date(2011, 1, 31), f.Date(2022, 1, 31))).toBe(11);
        // @ts-expect-error
        expect(() => f.NumberOfYearsBetween(1, f.Date(2012, 1, 31))).toThrow();
        // @ts-expect-error
        expect(() => f.NumberOfYearsBetween(f.Date(2012, 1, 31), 1)).toThrow();
        expect(() => f.NumberOfYearsBetween(f.Date(2012, 1, 31))).toThrow();
        expect(() => f.NumberOfYearsBetween()).toThrow();
    });
    it("should format date", () => {
        expect(f.Format(f.Date(2011, 1, 15), "YYYY-MM-DD")).toBe("2011-01-15");
        expect(f.Format(f.Date(2011, 1, 15), "YY-MM-DD")).toBe("11-01-15");
        expect(f.Format(f.Date(2011, 1, 15))).toBe("2011-01-15");
        expect(f.Format(f.Date(2011, 1, 15), "MM-DD-YYYY")).toBe("01-15-2011");
        expect(f.Format(f.Date(2011, 1, 15), "DD-MM-YYYY")).toBe("15-01-2011");
        expect(f.Format(f.Date(2011, 1, 15), "DD MM YYYY")).toBe("15 01 2011");
        expect(f.Format(f.Date(2011, 1, 15), "DD:MM:YYYY")).toBe("15:01:2011");
        expect(f.Format(f.Date(2011, 1, 15), "DD/MM/YYYY")).toBe("15/01/2011");
        expect(f.Format(f.Date(2011, 1, 15), "DD/MM-=-YYYY")).toBe("15/01-=-2011");
        expect(() => f.Format()).toThrow();
    });
    it("should create date from time", () => {
        const date = new Date(2011, 11, 11, 59, 58);
        expect(f.AsDate(date).getMinutes()).toBe(0);
        expect(f.AsDate(date).getHours()).toBe(0);
    });
    it("should create date time from date", () => {
        const date = new Date(2011, 11, 11, 59, 58);
        expect(f.AsTime(date).getHours()).toBe(0);
        expect(f.AsTime(date).getMinutes()).toBe(0);
    });
    it("should throw on undefined parameters", () => {
        expect(() => f.AsDate()).toThrow();
        expect(() => f.AsTime()).toThrow();
        expect(() => f.PlusDays()).toThrow();
        expect(() => f.PlusDays(new Date())).toThrow();
        expect(() => f.PlusMonths()).toThrow();
        expect(() => f.PlusMonths(new Date())).toThrow();
        expect(() => f.PlusYears(new Date())).toThrow();
        expect(() => f.PlusYears()).toThrow();
        expect(() => f.PlusYears(new Date())).toThrow();
        expect(() => f.NumberOfDaysBetween()).toThrow();
        expect(() => f.NumberOfDaysBetween(new Date())).toThrow();
        expect(() => f.IsDateBetween()).toThrow();
        expect(() => f.IsDateBetween(new Date())).toThrow();
        expect(() => f.IsDateBetween(new Date(), new Date())).toThrow();
    });
});
