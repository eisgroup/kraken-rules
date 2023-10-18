/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utils for working with Java dates
 *
 * @author Mindaugas Ulevicius
 */
public class Dates {

    public static String convertLocalDateTimeToISO(LocalDateTime localDateTime) {
        return ZonedDateTime.of(localDateTime, ZoneId.systemDefault())
            .truncatedTo(ChronoUnit.MILLIS)
            .format(DateTimeFormatter.ISO_INSTANT);
    }

    public static LocalDateTime convertISOToLocalDateTime(String isoDateTime) {
        return ZonedDateTime.parse(isoDateTime).withZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static String convertLocalDateToISO(LocalDate localDate) {
        return localDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public static LocalDate convertISOToLocalDate(String isoDate) {
        return LocalDate.parse(isoDate, DateTimeFormatter.ISO_LOCAL_DATE);
    }

}
