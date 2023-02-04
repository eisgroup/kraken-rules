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
package kraken.el.functionregistry.functions;

import static kraken.el.functionregistry.functions.DateFunctions.date;
import static kraken.el.functionregistry.functions.DateFunctions.dateTime;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

import org.junit.Test;

import kraken.el.ExpressionEvaluationException;
import kraken.el.ast.builder.Literals;

/**
 * @author psurinin
 */
public class DateFunctionsTest {

    @Test
    public void shouldThrowIfDatePatternIsNotValid() {
        assertThrows(ExpressionEvaluationException.class, () -> DateFunctions.date("22011-11-11"));
    }

    @Test
    public void shouldThrowIfDateTimePatternIsNotValid() {
        assertThrows(ExpressionEvaluationException.class, () -> DateFunctions.dateTime("2011-11-1100:00:00"));
    }

    @Test
    public void shouldAddDays() {
        LocalDate localDate = DateFunctions.plusDays(date("2011-11-11"), 1);
        LocalDateTime localDateTime = DateFunctions.plusDays(LocalDateTime.of(2011, 11, 11, 0, 0), 1);
        assertThat(localDate.getDayOfMonth(), is(12));
        assertThat(localDateTime.getDayOfMonth(), is(12));

        assertThat(DateFunctions.plusDays(date("2000-01-01"), 1), equalTo(date("2000-01-02")));
        assertThat(DateFunctions.plusDays(date("2000-01-01"), 32), equalTo(date("2000-02-02")));
    }

    @Test
    public void dateShouldBeCompatibleWithTime() {
        assertThat(DateFunctions.today().equals(DateFunctions.asDate(LocalDateTime.now())), is(true));
        assertThat(DateFunctions.dateTime("2011-11-11T00:00:00").equals(DateFunctions.asTime(date("2011-11-11"))), is(true));
    }

    @Test
    public void shouldThrowOnAddDaysWithIllegalType() {
        assertThrows(ExpressionEvaluationException.class,
                () -> DateFunctions.plusDays("2011-11-11", 1));
    }

    @Test
    public void shouldAddMonths() {
        LocalDate localDate = DateFunctions.plusMonths(date("2011-11-11"), 1);
        LocalDateTime localDateTime = DateFunctions.plusMonths(LocalDateTime.of(2011, 11, 11, 0, 0), 1);
        assertThat(localDate.getMonth().getValue(), is(12));
        assertThat(localDateTime.getMonth().getValue(), is(12));
    }

    @Test
    public void shouldAddDaysAndPreserveTime() {
        var date = Literals.getDateTime("2000-01-01T07:33:33Z");
        assertThat(DateFunctions.plusDays(date, 1), equalTo(Literals.getDateTime("2000-01-02T07:33:33Z")));
        assertThat(DateFunctions.plusDays(date, 32), equalTo(Literals.getDateTime("2000-02-02T07:33:33Z")));
    }

    @Test
    public void shouldAddMonthsAndPreserveTime() {
        var date = Literals.getDateTime("2000-01-30T07:33:33Z");
        assertThat(DateFunctions.plusMonths(date, 1), equalTo(Literals.getDateTime("2000-02-29T07:33:33Z")));
        assertThat(DateFunctions.plusMonths(date, 13), equalTo(Literals.getDateTime("2001-02-28T07:33:33Z")));
    }

    @Test
    public void shouldAddYearsAndPreserveTime() {
        var date = Literals.getDateTime("2000-02-29T07:33:33Z");
        assertThat(DateFunctions.plusYears(date, 1), equalTo(Literals.getDateTime("2001-02-28T07:33:33Z")));
        assertThat(DateFunctions.plusYears(date, -1), equalTo(Literals.getDateTime("1999-02-28T07:33:33Z")));
    }

    @Test
    public void shouldThrowOnAddMonthsWithIllegalType() {
        assertThrows(ExpressionEvaluationException.class,
                () -> DateFunctions.plusMonths("2011-11-11", 1));
    }

    @Test
    public void shouldAddYears() {
        LocalDate localDate = DateFunctions.plusYears(date("2011-11-11"), 1);
        LocalDateTime localDateTime = DateFunctions.plusYears(LocalDateTime.of(2011, 11, 11, 0, 0), 1);
        assertThat(localDate.getYear(), is(2012));
        assertThat(localDateTime.getYear(), is(2012));
    }

    @Test
    public void shouldThrowOnAddYearsWithIllegalType() {
        assertThrows(ExpressionEvaluationException.class,
                () -> DateFunctions.plusYears("2011-11-11", 1));
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
        assertThat(date(2011, 11, 11), is(LocalDate.of(2011, 11, 11)));
        assertThat(date(2011L, 11L, 11L), is(LocalDate.of(2011, 11, 11)));
    }

    @Test
    public void shouldGetDayFromDate() {
        assertThat(DateFunctions.getDay(LocalDate.of(2011, 11, 12)), is(12));
        assertThat(DateFunctions.getDay(LocalDateTime.of(2011, 11, 12, 13, 14)), is(12));
        assertThrows(ExpressionEvaluationException.class, () -> DateFunctions.getDay("2011, 11, 12"));
    }

    @Test
    public void shouldGetMonthFromDate() {
        assertThat(DateFunctions.getMonth(LocalDate.of(2011, 11, 12)), is(11));
        assertThat(DateFunctions.getMonth(LocalDateTime.of(2011, 11, 12, 13, 14)), is(11));
        assertThrows(ExpressionEvaluationException.class, () -> DateFunctions.getMonth("2011, 11, 12"));
    }

    @Test
    public void shouldGetYearFromDate() {
        assertThat(DateFunctions.getYear(LocalDate.of(2011, 11, 12)), is(2011));
        assertThat(DateFunctions.getYear(LocalDateTime.of(2011, 11, 12, 13, 14)), is(2011));
        assertThrows(ExpressionEvaluationException.class, () -> DateFunctions.getYear("2011, 11, 12"));
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

    @Test
    public void shouldReturnWithYearFromDate() {
        assertThat(
            DateFunctions.withYear(date("2011-11-11"), 2020),
            equalTo(date("2020-11-11"))
        );

        assertThat(
            DateFunctions.withYear(dateTime("2011-11-11T10:00:00Z"), 2020),
            equalTo(dateTime("2020-11-11T10:00:00Z"))
        );

        assertThat(
            DateFunctions.withYear(date("2000-02-29"), 2001),
            equalTo(date("2001-02-28"))
        );

        assertThrows(ExpressionEvaluationException.class, () -> DateFunctions.withYear(date("2011-11-11"), 0));
        assertThrows(ExpressionEvaluationException.class, () -> DateFunctions.withYear(date("2011-11-11"), 10000));
    }

    @Test
    public void shouldReturnWithMonthFromDate() {
        assertThat(
            DateFunctions.withMonth(date("2011-11-11"), 2),
            equalTo(date("2011-02-11"))
        );

        assertThat(
            DateFunctions.withMonth(dateTime("2011-11-11T10:00:00Z"), 2),
            equalTo(dateTime("2011-02-11T10:00:00Z"))
        );

        assertThat(
            DateFunctions.withMonth(date("2000-01-30"), 2),
            equalTo(date("2000-02-29"))
        );

        assertThrows(ExpressionEvaluationException.class, () -> DateFunctions.withMonth(date("2011-11-11"), 0));
        assertThrows(ExpressionEvaluationException.class, () -> DateFunctions.withMonth(date("2011-11-11"), 13));
    }

    @Test
    public void shouldReturnWithDayFromDate() {
        assertThat(
            DateFunctions.withDay(date("2011-11-11"), 2),
            equalTo(date("2011-11-02"))
        );

        assertThat(
            DateFunctions.withDay(dateTime("2011-11-11T10:00:00Z"), 2),
            equalTo(dateTime("2011-11-02T10:00:00Z"))
        );

        assertThrows(ExpressionEvaluationException.class, () -> DateFunctions.withDay(date("2011-11-11"), 0));
        assertThrows(ExpressionEvaluationException.class, () -> DateFunctions.withDay(date("2011-11-11"), 32));

        assertThrows(ExpressionEvaluationException.class, () -> DateFunctions.withDay(date("2000-02-28"), 30));
    }

}
