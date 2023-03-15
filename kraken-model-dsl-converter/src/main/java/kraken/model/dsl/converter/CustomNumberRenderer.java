/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl.converter;

import java.util.Locale;

import org.stringtemplate.v4.NumberRenderer;

import kraken.el.math.Numbers;

/**
 * Custom number renderer used to format {@link Number} with custom formats.
 * Normalizes and rounds all numbers to adhere to Decimal64 format when writing to DSL.
 * Format <code>minMaxOrInteger</code> will render {@link Integer#MIN_VALUE} as MIN, {@link Integer#MAX_VALUE} as MAX,
 * or integer number otherwise.
 *
 * @author mulevicius
 * @since 1.40.0
 */
public class CustomNumberRenderer extends NumberRenderer {

    @Override
    public String toString(Object o, String formatString, Locale locale) {
        if("minMaxOrInteger".equals(formatString)){
            if(o instanceof Integer) {
                Integer integer = (Integer) o;
                if(integer.equals(Integer.MAX_VALUE)) {
                    return "MAX";
                }
                if(integer.equals(Integer.MIN_VALUE)) {
                    return "MIN";
                }
                return String.valueOf(integer);
            }
            throw new IllegalArgumentException("Cannot render number with 'minMaxOrInteger' format, "
                + "because number is not an Integer.");
        }
        if(o instanceof Number && formatString == null) {
            return Numbers.toString((Number)o);
        }
        if(o instanceof Number) {
            o = Numbers.normalized((Number)o);
        }
        return super.toString(o, formatString, locale);
    }
}
