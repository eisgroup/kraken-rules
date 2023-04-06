/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.engine.handlers.trace;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.money.MonetaryAmount;

import kraken.el.math.Numbers;
import kraken.utils.Dates;

/**
 * Renders field value as a string for the purpose of logging
 *
 * @author Mindaugas Ulevicius
 */
public class FieldValueRenderer {

    private static final DateTimeFormatter TEMPLATE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String render(Object object) {
        if(object instanceof MonetaryAmount) {
            return render(((MonetaryAmount) object).getNumber().numberValue(BigDecimal.class));
        }
        if(object instanceof BigDecimal) {
            return Numbers.toString((BigDecimal) object);
        }
        if(object instanceof LocalDate) {
            return ((LocalDate) object).format(TEMPLATE_DATE_FORMAT);
        }
        if(object instanceof LocalDateTime) {
            return Dates.convertLocalDateTimeToISO((LocalDateTime) object);
        }
        if(object instanceof Collection) {
            return ((Collection<?>) object).stream()
                    .map(FieldValueRenderer::render)
                    .collect(Collectors.joining(", ", "[", "]"));
        }
        return String.valueOf(object);
    }
}
