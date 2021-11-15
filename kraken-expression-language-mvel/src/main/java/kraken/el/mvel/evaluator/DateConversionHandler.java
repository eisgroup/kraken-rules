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
package kraken.el.mvel.evaluator;

import org.mvel2.ConversionHandler;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * @author psurinin
 */
class DateConversionHandler {

    private DateConversionHandler() {
    }

    private static ConversionHandler DATE_TO_DATETIME = new DateToDateTime();
    private static ConversionHandler DATETIME_TO_DATE = new DatetimeToDate();

    static ConversionHandler getDateToDatetime() {
        return DATE_TO_DATETIME;
    }

    static ConversionHandler getDatetimeToDate() {
        return DATETIME_TO_DATE;
    }

    private static class DateToDateTime implements ConversionHandler {

        @Override
        public Object convertFrom(Object in) {
            if (in instanceof LocalDate) {
                return ((LocalDate) in).atStartOfDay();
            }
            throw new IllegalArgumentException();
        }

        @Override
        public boolean canConvertFrom(Class cls) {
            return LocalDate.class.isAssignableFrom(cls);
        }
    }

    private static class DatetimeToDate implements ConversionHandler {
        @Override
        public Object convertFrom(Object in) {
            if (in instanceof LocalDateTime) {
                return ((LocalDateTime) in).toLocalDate();
            }
            throw new IllegalArgumentException();
        }

        @Override
        public boolean canConvertFrom(Class cls) {
            return LocalDateTime.class.isAssignableFrom(cls);
        }
    }
}
