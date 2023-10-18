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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @author Mindaugas Ulevicius
 */
public class DefaultDateCalculator implements DateCalculator {

    @Override
    public LocalDate createDate(String date) {
        return LocalDate.parse(date);
    }

    @Override
    public LocalDate createDate(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    @Override
    public LocalDate today(ZoneId zoneId) {
        return LocalDate.now(zoneId);
    }

    @Override
    public int getDateField(LocalDate date, DateField field) {
        return date.get(field.chronoField);
    }
    @Override
    public LocalDate withDateField(LocalDate date, DateField field, int value) {
        return date.with(field.chronoField, value);
    }

    @Override
    public LocalDate addDateField(LocalDate date, DateField field, long value) {
        return date.plus(value, field.chronoField.getBaseUnit());
    }

    @Override
    public long difference(LocalDate date1, LocalDate date2, DateField field) {
        return date1.until(date2, field.chronoField.getBaseUnit());
    }

    @Override
    public LocalDateTime createDateTime(String dateTime) {
        var zonedDateTime = ZonedDateTime.parse(dateTime);
        return atLocal(zonedDateTime);
    }

    @Override
    public LocalDateTime createDateTime(int year, int month, int day, int hours, int minutes, int seconds, ZoneId zoneId) {
        var zonedDateTime = ZonedDateTime.of(year, month, day, hours, minutes, seconds, 0, zoneId);
        return atLocal(zonedDateTime);
    }

    @Override
    public LocalDateTime createDateTime(String dateTime, ZoneId zoneId) {
        var zonedDateTime = LocalDateTime.parse(dateTime).atZone(zoneId);
        return atLocal(zonedDateTime);
    }

    @Override
    public LocalDateTime now() {
        return LocalDateTime.now();
    }

    @Override
    public int getDateTimeField(LocalDateTime dateTime, DateTimeField field, ZoneId zoneId) {
        return atZone(dateTime, zoneId).get(field.chronoField);
    }

    @Override
    public LocalDateTime withDateTimeField(LocalDateTime dateTime, DateTimeField field, int value, ZoneId zoneId) {
        var zonedDateTime = atZone(dateTime, zoneId).with(field.chronoField, value);
        return atLocal(zonedDateTime);
    }

    @Override
    public LocalDateTime addDateTimeField(LocalDateTime dateTime, DateTimeField field, long value, ZoneId zoneId) {
        var zonedDateTime = atZone(dateTime, zoneId).plus(value, field.chronoField.getBaseUnit());
        return atLocal(zonedDateTime);
    }

    @Override
    public LocalDateTime toDateTime(LocalDate date, ZoneId zoneId) {
        var zonedDateTime = date.atStartOfDay(zoneId);
        return atLocal(zonedDateTime);
    }

    @Override
    public LocalDate toDate(LocalDateTime dateTime, ZoneId zoneId) {
        return atZone(dateTime, zoneId).toLocalDate();
    }

    private LocalDateTime atLocal(ZonedDateTime zonedDateTime) {
        return zonedDateTime.withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    private ZonedDateTime atZone(LocalDateTime localDateTime, ZoneId zoneId) {
        return localDateTime.atZone(ZoneId.systemDefault()).withZoneSameInstant(zoneId);
    }

}
