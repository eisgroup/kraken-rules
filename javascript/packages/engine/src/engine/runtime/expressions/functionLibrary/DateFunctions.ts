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

import moment from "moment";
import { message } from "./function.utils";
// tslint:disable: triple-equals

function Today(): Date {
    const now = new Date();
    return new Date(now.getFullYear(), now.getMonth(), now.getDate(), 0, 0);
}

function Now(): Date {
    return new Date();
}

function FxDate(dateString?: string): Date;
function FxDate(year?: number, month?: number, day?: number): Date;

function FxDate(dateString?: string | number, month?: number, day?: number): Date {
    if (!dateString) {
        throw new Error("Failed to execute function 'Date' with parameters: " + [...arguments].join());
    }
    if (typeof dateString === "string") {
        return new Date(dateString);
    }
    if (month && day) {
        return new Date(dateString, month - 1, day);
    }
    throw new Error("Failed to execute function 'Date' with parameters: " + [...arguments].join());

}

function PlusYears(dateArg?: Date, num?: number): Date {
    if (!dateArg) {
        throw new Error(message("PlusYears", message.reason.firstParam));
    }
    if (num == undefined) {
        throw new Error(message("PlusYears", message.reason.secondParam));
    }
    return new Date(
        dateArg.getFullYear() + num, dateArg.getMonth(),
        dateArg.getDate()
    );
}

function PlusMonths(dateArg?: Date, num?: number): Date {
    if (!dateArg) {
        throw new Error(message("PlusMonths", message.reason.firstParam));
    }
    if (num == undefined) {
        throw new Error(message("PlusMonths", message.reason.secondParam));
    }
    return new Date(
        dateArg.getFullYear(),
        dateArg.getMonth() + num,
        dateArg.getDate(),
        dateArg.getHours(),
        dateArg.getMinutes(),
        dateArg.getMilliseconds()
    );
}

function PlusDays(dateArg?: Date, num?: number): Date {
    if (!dateArg) {
        throw new Error(message("PlusDays", message.reason.firstParam));
    }
    if (num == undefined) {
        throw new Error(message("PlusDays", message.reason.secondParam));
    }
    return new Date(
        dateArg.getFullYear(),
        dateArg.getMonth(),
        dateArg.getDate() + num,
        dateArg.getHours(),
        dateArg.getMinutes(),
        dateArg.getMilliseconds()
    );
}

function AsDate(dateArg?: Date): Date {
    if (!dateArg) {
        throw new Error(message("AsDate", message.reason.firstParam));
    }
    return new Date(
        dateArg.getFullYear(), dateArg.getMonth(), dateArg.getDate()
    );
}

function AsTime(dateArg?: Date): Date {
    if (!dateArg) {
        throw new Error(message("AsTime", message.reason.firstParam));
    }
    return new Date(
        dateArg.getFullYear(), dateArg.getMonth(), dateArg.getDate(), 0, 0, 0, 0
    );
}

function IsDateBetween(dateToCheck?: Date, start?: Date, end?: Date): boolean {
    if (!dateToCheck) {
        throw new Error(message("NumberOfDaysBetween", message.reason.firstParam));
    }
    if (!start) {
        throw new Error(message("NumberOfDaysBetween", message.reason.secondParam));
    }
    if (!end) {
        throw new Error(message("NumberOfDaysBetween", message.reason.thirdParam));
    }
    return dateAfter(dateToCheck, start) && dateBefore(dateToCheck, end);
}

function GetDay(date?: Date): number {
    if (!date) {
        throw new Error("Failed to execute function 'GetDay'. Parameter is absent");
    }
    try {
        return date.getDate();
    } catch (error) {
        throw new Error(`Failed to execute function 'GetDay'. Parameter '${date}' is invalid`);
    }
}

function GetYear(date?: Date): number {
    if (!date) {
        throw new Error("Failed to execute function 'GetYear'. Parameter is absent");
    }
    try {
        return date.getFullYear();
    } catch (error) {
        throw new Error(`Failed to execute function 'GetYear'. Parameter '${date}' is invalid`);
    }
}

function GetMonth(date?: Date): number {
    if (!date) {
        throw new Error("Failed to execute function 'GetMonth'. Parameter is absent");
    }
    try {
        return date.getMonth() + 1;
    } catch (error) {
        throw new Error(`Failed to execute function 'GetMonth'. Parameter '${date}' is invalid`);
    }
}

function NumberOfDaysBetween(start?: Date, end?: Date): number {
    if (!start) {
        throw new Error(message("NumberOfDaysBetween", message.reason.firstParam));
    }
    if (!end) {
        throw new Error(message("NumberOfDaysBetween", message.reason.secondParam));
    }
    const day = 24 * 60 * 60 * 1000;
    const diffTime = Math.abs((start.getTime() - end.getTime()));
    return Math.floor(diffTime / day);
}

function NumberOfMonthsBetween(d1?: Date, d2?: Date): number {
    if (!d1) {
        throw new Error(message("NumberOfMonthsBetween", message.reason.firstParam));
    }
    if (!d2) {
        throw new Error(message("NumberOfMonthsBetween", message.reason.secondParam));
    }
    try {
        const date1 = AsDate(d1);
        const date2 = AsDate(d2);

        const from = date1 < date2 ? date1 : date2;
        const to = date2 > date1 ? date2 : date1;

        let fullMonths = (to.getFullYear() - from.getFullYear()) * 12 + (to.getMonth() - from.getMonth());
        if (fullMonths > 0) {
            fullMonths -= 1;
        }

        let partialMonth = 0;
        if ((from.getFullYear() < to.getFullYear() || from.getMonth() < to.getMonth())
            && to.getDate() >= from.getDate()) {
            partialMonth = 1;
        }

        return fullMonths + partialMonth;
    } catch (error) {
        throw new Error(error("NumberOfMonthsBetween", "Parameters are invalid: " + [...arguments]));
    }
}

function NumberOfYearsBetween(start?: Date, end?: Date): number {
    return Math.floor(NumberOfMonthsBetween(start, end) / 12);
}

function Format(date?: Date, format: string = "YYYY-MM-DD"): string {
    if (!date) {
        throw new Error(message("Format", message.reason.firstParam));
    }
    return moment(date).format(format);
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
    IsDateBetween
};

const dateAfter = (date: Date, start: Date) => date >= start;
const dateBefore = (date: Date, end: Date) => date <= end;
