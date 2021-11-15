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
package kraken.el.functions;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author psurinin
 */
public class DateFunctionsTest {

    @Test
    public void shouldAddDays() {
        final Object localDate = DateFunctions.plusDays(DateFunctions.date("2011-11-11"), 1);
        final Object localDateTime = DateFunctions.plusDays(LocalDateTime.of(2011, 11, 11, 0, 0), 1);
        assertThat(localDate, instanceOf(LocalDate.class));
        assertThat(((LocalDate) localDate).getDayOfMonth(), is(12));
        assertThat(localDateTime, instanceOf(LocalDateTime.class));
        assertThat(((LocalDateTime) localDateTime).getDayOfMonth(), is(12));
    }

    @Test
    public void dateShouldBeCompatibleWithTime() {
        assertThat(DateFunctions.today().equals(DateFunctions.asDate(LocalDateTime.now())), is(true));
        assertThat(DateFunctions.dateTime("2011-11-11T00:00:00").equals(DateFunctions.asTime(DateFunctions.date("2011-11-11"))), is(true));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowOnAddDaysWithIllegalType() {
        DateFunctions.plusDays("2011-11-11", 1);
    }

    @Test
    public void shouldAddMonths() {
        final Object localDate = DateFunctions.plusMonths(DateFunctions.date("2011-11-11"), 1);
        final Object localDateTime = DateFunctions.plusMonths(LocalDateTime.of(2011, 11, 11, 0, 0), 1);
        assertThat(localDate, instanceOf(LocalDate.class));
        assertThat(((LocalDate) localDate).getMonth().getValue(), is(12));
        assertThat(localDateTime, instanceOf(LocalDateTime.class));
        assertThat(((LocalDateTime) localDateTime).getMonth().getValue(), is(12));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowOnAddMonthsWithIllegalType() {
        DateFunctions.plusMonths("2011-11-11", 1);
    }

    @Test
    public void shouldAddYears() {
        final Object localDate = DateFunctions.plusYears(DateFunctions.date("2011-11-11"), 1);
        final Object localDateTime = DateFunctions.plusYears(LocalDateTime.of(2011, 11, 11, 0, 0), 1);
        assertThat(localDate, instanceOf(LocalDate.class));
        assertThat(((LocalDate) localDate).getYear(), is(2012));
        assertThat(localDateTime, instanceOf(LocalDateTime.class));
        assertThat(((LocalDateTime) localDateTime).getYear(), is(2012));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowOnAddYearsWithIllegalType() {
        DateFunctions.plusYears("2011-11-11", 1);
    }

    @Test
    public void shouldCountNumberOfDaysBetweenNegative() {
        final long between = DateFunctions.numberOfDaysBetween(
                LocalDate.of(2018, Month.JULY, 1),
                LocalDate.of(2018, Month.JULY, 10)
        );
        assertThat(between, is(9L));
    }

    @Test
    public void shouldCountNumberOfDaysBetweenMonthsAndYears() {
        final long between = DateFunctions.numberOfDaysBetween(
                LocalDate.of(2017, Month.JANUARY, 1),
                LocalDate.of(2018, Month.JULY, 1)
        );
        assertThat(between, is(546L));
    }

    @Test
    public void shouldCountNumberOfDaysBetween() {
        final long between = DateFunctions.numberOfDaysBetween(
                LocalDate.of(2018, Month.JULY, 10),
                LocalDate.of(2018, Month.JULY, 1)
        );
        assertThat(between, is(9L));
    }

    @Test
    public void shouldDefineIsDateBetween_True() {
        final boolean isBetween = DateFunctions.isDateBetween(
                LocalDate.of(2018, Month.JULY, 5),
                LocalDate.of(2018, Month.JULY, 1),
                LocalDate.of(2018, Month.JULY, 10)
        );
        assertTrue(isBetween);
    }

    @Test
    public void shouldDefineIsDateBetween_True_Inclusive_Start() {
        final boolean isBetween = DateFunctions.isDateBetween(
                LocalDate.of(2018, Month.JULY, 1),
                LocalDate.of(2018, Month.JULY, 1),
                LocalDate.of(2018, Month.JULY, 10)
        );
        assertTrue(isBetween);
    }

    @Test
    public void shouldDefineIsDateBetween_True_Inclusive_End() {
        final boolean isBetween = DateFunctions.isDateBetween(
                LocalDate.of(2018, Month.JULY, 10),
                LocalDate.of(2018, Month.JULY, 1),
                LocalDate.of(2018, Month.JULY, 10)
        );
        assertTrue(isBetween);
    }

    @Test
    public void shouldDefineIsDateBetween_False() {
        final boolean isBetween = DateFunctions.isDateBetween(
                LocalDate.of(2018, Month.AUGUST, 5),
                LocalDate.of(2018, Month.JULY, 1),
                LocalDate.of(2018, Month.JULY, 10)
        );
        assertFalse(isBetween);
    }

    @Test
    public void shouldCreateDateFrom3Params() {
        assertThat(DateFunctions.date(2011, 11, 11), is(LocalDate.of(2011, 11, 11)));
        assertThat(DateFunctions.date(2011l, 11l, 11l), is(LocalDate.of(2011, 11, 11)));
    }

    @Test
    public void shouldGetDayFromDate() {
        assertThat(DateFunctions.getDay(LocalDate.of(2011, 11, 12)), is(12));
        assertThat(DateFunctions.getDay(LocalDateTime.of(2011, 11, 12, 13, 14)), is(12));
    }

    @Test
    public void shouldGetMonthFromDate() {
        assertThat(DateFunctions.getMonth(LocalDate.of(2011, 11, 12)), is(11));
        assertThat(DateFunctions.getMonth(LocalDateTime.of(2011, 11, 12, 13, 14)), is(11));
    }

    @Test
    public void shouldGetYearFromDate() {
        assertThat(DateFunctions.getYear(LocalDate.of(2011, 11, 12)), is(2011));
        assertThat(DateFunctions.getYear(LocalDateTime.of(2011, 11, 12, 13, 14)), is(2011));
    }

    @Test
    public void shouldFormatDate() {
        assertThat(DateFunctions.format(LocalDate.of(2011, 12, 31), "YYYY-MM-DD"), is("2011-12-31"));
        assertThat(DateFunctions.format(LocalDate.of(2011, 12, 31), null), is("2011-12-31"));
        assertThat(DateFunctions.format(LocalDate.of(2011, 12, 31), "MM-DD-YYYY"), is("12-31-2011"));
        assertThat(DateFunctions.format(LocalDate.of(2011, 12, 31), "DD-MM-YYYY"), is("31-12-2011"));
        assertThat(DateFunctions.format(LocalDate.of(2011, 12, 31), "DD MM YYYY"), is("31 12 2011"));
        assertThat(DateFunctions.format(LocalDate.of(2011, 12, 31), "DD:MM:YYYY"), is("31:12:2011"));
        assertThat(DateFunctions.format(LocalDate.of(2011, 12, 31), "DD/MM/YYYY"), is("31/12/2011"));
        assertThat(DateFunctions.format(LocalDate.of(2011, 12, 31), "DD/MM-=-YYYY"), is("31/12-=-2011"));
    }

    @Test
    public void shouldCalculateMonthsBetween() {
        assertThat(DateFunctions.numberOfMonthsBetween(
                LocalDate.of(2011, 1, 15),
                LocalDate.of(2011, 2, 15)
                ),
                is(1L)
        );
        assertThat(DateFunctions.numberOfMonthsBetween(
                LocalDate.of(2011, 1, 16),
                LocalDate.of(2011, 2, 15)
                ),
                is(0L)
        );
        assertThat(DateFunctions.numberOfMonthsBetween(
                LocalDate.of(2011, 1, 1),
                LocalDate.of(2011, 1, 31)
                ),
                is(0L)
        );
        assertThat(DateFunctions.numberOfMonthsBetween(
                LocalDate.of(2011, 1, 31),
                LocalDate.of(2011, 2, 1)
                ),
                is(0L)
        );
        assertThat(DateFunctions.numberOfMonthsBetween(
                LocalDate.of(2012, 2, 28),
                LocalDate.of(2011, 1, 31)
                ),
                is(12L)
        );
        assertThat(DateFunctions.numberOfMonthsBetween(
                LocalDate.of(2011, 1, 31),
                LocalDate.of(2012, 2, 28)
                ),
                is(12L)
        );
    }

    @Test
    public void shouldCalculateYearsBetween() {
        assertThat(DateFunctions.numberOfYearsBetween(
                LocalDate.of(2011, 1, 15),
                LocalDate.of(2011, 2, 15)
                ),
                is(0L)
        );
        assertThat(DateFunctions.numberOfYearsBetween(
                LocalDate.of(2011, 12, 31),
                LocalDate.of(2012, 1, 30)
                ),
                is(0L)
        );
        assertThat(DateFunctions.numberOfYearsBetween(
                LocalDate.of(2011, 10, 31),
                LocalDate.of(2012, 10, 30)
                ),
                is(0L)
        );
        assertThat(DateFunctions.numberOfYearsBetween(
                LocalDate.of(2010, 1, 10),
                LocalDate.of(2011, 1, 1)
                ),
                is(0L)
        );
        assertThat(DateFunctions.numberOfYearsBetween(
                LocalDate.of(2010, 1, 16),
                LocalDate.of(2011, 1, 16)
                ),
                is(1L)
        );
        assertThat(DateFunctions.numberOfYearsBetween(
                LocalDate.of(2010, 1, 16),
                LocalDate.of(2011, 12, 16)
                ),
                is(1L)
        );
        assertThat(DateFunctions.numberOfYearsBetween(
                LocalDate.of(2022, 1, 31),
                LocalDate.of(2011, 1, 31)
                ),
                is(11L)
        );
        assertThat(DateFunctions.numberOfYearsBetween(
                LocalDate.of(2011, 1, 31),
                LocalDate.of(2022, 1, 31)
                ),
                is(11L)
        );
    }
}