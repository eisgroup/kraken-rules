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
package kraken.el.ast.builder;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * Utilities for working with Literals supported by Kraken Expression Language
 *
 * @author mulevicius
 */
public class Literals {

    private static final MathContext EXACT_DECIMAL64 = new MathContext(16, RoundingMode.UNNECESSARY);

    private Literals() {
    }

    public static String stripQuotes(String str) {
        return str.substring(1, str.length() - 1);
    }

    /**
     * Converts text from dsl domain to string domain. Opposite of {@link Literals#deescape(String)}
     *
     * @param str to escape
     */
    public static String escape(String str) {
        return str.replace("\\\"", "\"")
                .replace("\\'", "'")
                .replace("\\\\", "\\");
    }

    /**
     * Converts text from string domain to dsl domain. Opposite of {@link Literals#escape(String)}
     *
     * @param str to deescape
     */
    public static String deescape(String str) {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("'", "\\'");
    }

    public static Boolean getBoolean(String boolStr) {
        return Boolean.valueOf(boolStr.toLowerCase());
    }

    /**
     *
     * @param decimalStr
     * @return
     * @throws ArithmeticException when decimal literal cannot be exactly parsed to decimal64 due to loss of precision
     * @throws NumberFormatException when string is not a number
     */
    public static BigDecimal getExactDecimal64(String decimalStr) throws ArithmeticException, NumberFormatException {
        return new BigDecimal(decimalStr, EXACT_DECIMAL64);
    }

    /**
     *
     * @param decimalStr
     * @return decimal parsed from string
     * @throws NumberFormatException when string is not a number
     */
    public static BigDecimal getDecimal(String decimalStr) throws NumberFormatException {
        return new BigDecimal(decimalStr);
    }

    public static Integer getInteger(String integerString) {
        return Integer.parseInt(integerString);
    }

    public static LocalDate getDate(String isoDate) {
        return LocalDate.parse(isoDate);
    }

    public static LocalDateTime getDateTime(String isoDate) {
        return LocalDateTime.ofInstant(ZonedDateTime.parse(isoDate).toInstant(), ZoneId.systemDefault());
    }

}
