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
package kraken.el.date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThrows;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import org.junit.Before;
import org.junit.Test;

import kraken.el.date.DateCalculator.DateField;
import kraken.el.date.DateCalculator.DateTimeField;
import kraken.utils.Dates;

/**
 * @author Mindaugas Ulevicius
 */
public class DefaultDateCalculatorTest {

    private final ZoneId tz = ZoneId.of("Europe/Berlin");

    private DefaultDateCalculator calculator;

    @Before
    public void setUp() throws Exception {
        this.calculator = new DefaultDateCalculator();
    }

    @Test
    public void shouldCreateDate() {
        assertThat(
            calculator.createDate(2020, 1, 23),
            equalTo(LocalDate.of(2020, 1, 23))
        );
        assertThrows(DateTimeException.class, () -> calculator.createDate(2020, 2, 30));
        assertThrows(DateTimeException.class, () -> calculator.createDate(2020, 0, 1));
        assertThrows(DateTimeException.class, () -> calculator.createDate(2020, 13, 1));
        assertThrows(DateTimeException.class, () -> calculator.createDate(2020, 1, 0));

        assertThat(
            calculator.createDate("2020-01-23"),
            equalTo(LocalDate.of(2020, 1, 23))
        );
        assertThrows(DateTimeException.class, () -> calculator.createDate("2020-02-30"));
        assertThrows(DateTimeParseException.class, () -> calculator.createDate("2020-00-01"));
        assertThrows(DateTimeException.class, () -> calculator.createDate("2020-13-01"));
        assertThrows(DateTimeException.class, () -> calculator.createDate("2020-01-00"));

        assertThrows(DateTimeParseException.class, () -> calculator.createDate("2020-01-23T10:00:00"));
        assertThrows(DateTimeParseException.class, () -> calculator.createDate("2020-01-23T10:00:00Z"));
    }

    @Test
    public void shouldCreateDateTime() {
        assertThrows(DateTimeParseException.class, () -> calculator.createDateTime("2020-01-23"));
        assertThrows(DateTimeParseException.class, () -> calculator.createDateTime("2020-01-23T10:00:00"));
        assertThrows(DateTimeParseException.class, () -> calculator.createDateTime("2020-02-30T10:00:00Z"));
        assertThrows(DateTimeParseException.class, () -> calculator.createDateTime("2020-00-01T10:00:00Z"));
        assertThrows(DateTimeParseException.class, () -> calculator.createDateTime("2020-13-01T10:00:00Z"));
        assertThrows(DateTimeParseException.class, () -> calculator.createDateTime("2020-01-00T10:00:00Z"));
        assertThrows(DateTimeParseException.class, () -> calculator.createDateTime("2020-01-23T25:00:00Z"));
        assertThrows(DateTimeParseException.class, () -> calculator.createDateTime("2020-01-23T10:60:00Z"));
        assertThrows(DateTimeParseException.class, () -> calculator.createDateTime("2020-01-23T10:00:60Z"));

        assertThat(
            calculator.createDateTime("2020-01-23T10:00:00", tz),
            equalTo(atLocalTz(ZonedDateTime.of(LocalDateTime.parse("2020-01-23T10:00:00"), tz)))
        );
        assertThat(
            calculator.createDateTime("2020-01-23T10:00:00Z"),
            equalTo(Dates.convertISOToLocalDateTime("2020-01-23T10:00:00Z"))
        );
    }

    @Test
    public void shouldReturnToday() {
        assertThat(calculator.today(ZoneId.systemDefault()), equalTo(LocalDate.now()));
        assertThat(calculator.today(tz), equalTo(atTz(LocalDateTime.now()).toLocalDate()));
    }

    @Test
    public void shouldConvertDateToDateTime() {
        var startOfDay = calculator.toDateTime(LocalDate.of(2020, 1, 23), ZoneId.systemDefault());
        assertThat(startOfDay, equalTo(LocalDateTime.of(2020, 1, 23, 0, 0, 0)));

        var startOfDayInVilnius = calculator.toDateTime(LocalDate.of(2020, 1, 23), tz);
        var expectedStartOfDayInVilnius = atLocalTz(LocalDate.of(2020, 1, 23).atStartOfDay(tz));

        assertThat(startOfDayInVilnius, equalTo(expectedStartOfDayInVilnius));
    }

    @Test
    public void shouldConvertDateTimeToDate() {
        var time = LocalDateTime.of(2020, 1, 23, 10, 0, 0);

        assertThat(calculator.toDate(time, ZoneId.systemDefault()), equalTo(LocalDate.of(2020, 1, 23)));
        assertThat(calculator.toDate(time, tz), equalTo(atTz(time).toLocalDate()));
    }

    @Test
    public void shouldGetDateField() {
        var date = LocalDate.of(2020, 1, 30);
        assertThat(calculator.getDateField(date, DateField.YEAR), equalTo(2020));
        assertThat(calculator.getDateField(date, DateField.MONTH), equalTo(1));
        assertThat(calculator.getDateField(date, DateField.DAY_OF_MONTH), equalTo(30));
    }

    @Test
    public void shouldSetDateField() {
        var date = LocalDate.of(2020, 1, 30);
        assertThat(calculator.withDateField(date, DateField.YEAR, 2021), equalTo(LocalDate.of(2021, 1, 30)));
        assertThat(calculator.withDateField(date, DateField.MONTH, 3), equalTo(LocalDate.of(2020, 3, 30)));
        assertThat(calculator.withDateField(date, DateField.MONTH, 2), equalTo(LocalDate.of(2020, 2, 29)));
        assertThat(calculator.withDateField(date, DateField.DAY_OF_MONTH, 10), equalTo(LocalDate.of(2020, 1, 10)));

        assertThrows(DateTimeException.class, () -> calculator.withDateField(date, DateField.MONTH, 13));
        assertThrows(DateTimeException.class, () -> calculator.withDateField(date, DateField.DAY_OF_MONTH, 32));
    }

    @Test
    public void shouldAddDateField() {
        var date = LocalDate.of(2020, 1, 30);
        assertThat(calculator.addDateField(date, DateField.YEAR, 1), equalTo(LocalDate.of(2021, 1, 30)));
        assertThat(calculator.addDateField(date, DateField.MONTH, 1), equalTo(LocalDate.of(2020, 2, 29)));
        assertThat(calculator.addDateField(date, DateField.MONTH, 12), equalTo(LocalDate.of(2021, 1, 30)));
        assertThat(calculator.addDateField(date, DateField.DAY_OF_MONTH, 31), equalTo(LocalDate.of(2020, 3, 1)));
        assertThat(calculator.addDateField(date, DateField.DAY_OF_MONTH, 365), equalTo(LocalDate.of(2021, 1, 29)));
    }

    @Test
    public void shouldDiffDateField() {
        assertThat(calculator.difference(d("2020-01-30"), d("2022-01-30"), DateField.YEAR), equalTo(2L));
        assertThat(calculator.difference(d("2020-01-30"), d("2022-01-29"), DateField.YEAR), equalTo(1L));
        assertThat(calculator.difference(d("2020-01-30"), d("2021-01-29"), DateField.YEAR), equalTo(0L));

        assertThat(calculator.difference(d("2020-01-29"), d("2020-02-29"), DateField.MONTH), equalTo(1L));
        assertThat(calculator.difference(d("2020-01-30"), d("2020-02-29"), DateField.MONTH), equalTo(0L));
        assertThat(calculator.difference(d("2020-01-30"), d("2020-03-01"), DateField.MONTH), equalTo(1L));
        assertThat(calculator.difference(d("2020-01-29"), d("2021-01-28"), DateField.MONTH), equalTo(11L));

        assertThat(calculator.difference(d("2020-01-29"), d("2020-02-29"), DateField.DAY_OF_MONTH), equalTo(31L));
    }

    @Test
    public void shouldGetDateTimeFieldInSystemTz() {
        var dateTime = LocalDateTime.of(2020, 1, 30, 10, 10, 13);
        var defaultTz = ZoneId.systemDefault();
        assertThat(calculator.getDateTimeField(dateTime, DateTimeField.YEAR, defaultTz), equalTo(2020));
        assertThat(calculator.getDateTimeField(dateTime, DateTimeField.MONTH, defaultTz), equalTo(1));
        assertThat(calculator.getDateTimeField(dateTime, DateTimeField.DAY_OF_MONTH, defaultTz), equalTo(30));
        assertThat(calculator.getDateTimeField(dateTime, DateTimeField.HOUR, defaultTz), equalTo(10));
        assertThat(calculator.getDateTimeField(dateTime, DateTimeField.MINUTE, defaultTz), equalTo(10));
        assertThat(calculator.getDateTimeField(dateTime, DateTimeField.SECOND, defaultTz), equalTo(13));
    }

    @Test
    public void shouldSetDateTimeFieldInSystemTz() {
        var dateTime = LocalDateTime.of(2020, 1, 30, 10, 10, 13);
        var defaultTz = ZoneId.systemDefault();

        assertThat(
            calculator.withDateTimeField(dateTime, DateTimeField.YEAR, 2021, defaultTz),
            equalTo(LocalDateTime.of(2021, 1, 30, 10, 10, 13)));
        assertThat(
            calculator.withDateTimeField(dateTime, DateTimeField.MONTH, 3, defaultTz),
            equalTo(LocalDateTime.of(2020, 3, 30, 10, 10, 13)));
        assertThat(
            calculator.withDateTimeField(dateTime, DateTimeField.DAY_OF_MONTH, 10, defaultTz),
            equalTo(LocalDateTime.of(2020, 1, 10, 10, 10, 13)));
        assertThat(
            calculator.withDateTimeField(dateTime, DateTimeField.HOUR, 23, defaultTz),
            equalTo(LocalDateTime.of(2020, 1, 30, 23, 10, 13)));
        assertThat(
            calculator.withDateTimeField(dateTime, DateTimeField.MINUTE, 5, defaultTz),
            equalTo(LocalDateTime.of(2020, 1, 30, 10, 5, 13)));
        assertThat(
            calculator.withDateTimeField(dateTime, DateTimeField.SECOND, 0, defaultTz),
            equalTo(LocalDateTime.of(2020, 1, 30, 10, 10, 0)));

        assertThat(calculator.withDateTimeField(dateTime, DateTimeField.MONTH, 2, defaultTz),
            equalTo(LocalDateTime.of(2020, 2, 29, 10, 10, 13)));

        assertThrows(DateTimeException.class, () -> calculator.withDateTimeField(dateTime, DateTimeField.MONTH, 13, defaultTz));
        assertThrows(DateTimeException.class, () -> calculator.withDateTimeField(dateTime, DateTimeField.DAY_OF_MONTH, 32, defaultTz));
        assertThrows(DateTimeException.class, () -> calculator.withDateTimeField(dateTime, DateTimeField.HOUR, 24, defaultTz));
        assertThrows(DateTimeException.class, () -> calculator.withDateTimeField(dateTime, DateTimeField.MINUTE, 60, defaultTz));
        assertThrows(DateTimeException.class, () -> calculator.withDateTimeField(dateTime, DateTimeField.SECOND, 60, defaultTz));
    }

    @Test
    public void shouldAddDateTimeFieldInSystemTz() {
        var dateTime = LocalDateTime.of(2020, 1, 30, 10, 10, 13);
        var defaultTz = ZoneId.systemDefault();

        assertThat(
            calculator.addDateTimeField(dateTime, DateTimeField.YEAR, 1, defaultTz),
            equalTo(LocalDateTime.of(2021, 1, 30, 10, 10, 13)));
        assertThat(
            calculator.addDateTimeField(dateTime, DateTimeField.MONTH, 1, defaultTz),
            equalTo(LocalDateTime.of(2020, 2, 29, 10, 10, 13)));
        assertThat(
            calculator.addDateTimeField(dateTime, DateTimeField.DAY_OF_MONTH, 31, defaultTz),
            equalTo(LocalDateTime.of(2020, 3, 1, 10, 10, 13)));
        assertThat(
            calculator.addDateTimeField(dateTime, DateTimeField.MONTH, 12, defaultTz),
            equalTo(LocalDateTime.of(2021, 1, 30, 10, 10, 13)));
        assertThat(
            calculator.addDateTimeField(dateTime, DateTimeField.DAY_OF_MONTH, 365, defaultTz),
            equalTo(LocalDateTime.of(2021, 1, 29, 10, 10, 13)));
    }

    private LocalDate d(String iso) {
        return LocalDate.parse(iso);
    }

    private LocalDateTime atLocalTz(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    private LocalDateTime atTz(LocalDateTime localDateTime) {
        return localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(tz).toLocalDateTime();
    }
}
