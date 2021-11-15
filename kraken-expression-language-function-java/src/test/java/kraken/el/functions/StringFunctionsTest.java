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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author avasiliauskas
 */
public class StringFunctionsTest {

    @Test
    public void shouldSubstringFromProvidedIndex() {
        String fullValue = "fullString";
        String substringValue = StringFunctions.substring(fullValue, 4);
        assertThat(substringValue, is("String"));
    }

    @Test
    public void shouldSubstringString() {
        String fullValue = "fullStringLeft";
        String substringValue = StringFunctions.substring(fullValue, 4, 10);
        assertThat(substringValue, is("String"));
    }

    @Test
    public void shouldSubstringStringWhenBothIndexIsZero() {
        String fullValue = "fullStringLeft";
        String substringValue = StringFunctions.substring(fullValue, 0, 0);
        assertThat(substringValue, is(""));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenBeginIndexIsHigherThanStringLength() {
        String fullValue = "String";
        StringFunctions.substring(fullValue, 8, 9);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenBeginIndexIsNegative() {
        String fullValue = "String";
        StringFunctions.substring(fullValue, -1, 9);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenEndIndexIsHigherThanStringLength() {
        String fullValue = "String";
        StringFunctions.substring(fullValue, 3, 8);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowExceptionWhenEndIndexIsLowerThanBeginIndex() {
        String fullValue = "String";
        StringFunctions.substring(fullValue, 3, 2);
    }

    @Test
    public void shouldConcatString() {
        assertThat(StringFunctions.concat(List.of("a", "b", "c")), is("abc"));
        assertThat(StringFunctions.concat(null), is(""));
        ArrayList<String> s1 = new ArrayList<>();
        s1.add("a");
        s1.add(null);
        s1.add("c");
        assertThat(StringFunctions.concat(s1), is("ac"));
        ArrayList<Object> s2 = new ArrayList<>();
        s2.add(1);
        s2.add("2");
        s2.add(new BigDecimal(3));
        assertThat(StringFunctions.concat(s2), is("123"));
    }

    @Test
    public void shouldReturnStringLength() {
        assertThat(StringFunctions.length("333"), is(3));
        assertThat(StringFunctions.length(null), is(0));
    }

    @Test
    public void shouldSearchInStringCollection() {
        assertThat(StringFunctions.includes(List.of("a", "b", "c"), "a"), is(true));
        assertThat(StringFunctions.includes(List.of("a", "b", "c"), "d"), is(false));
        assertThat(StringFunctions.includes(List.of("a", "b", "c"), null), is(false));
        assertThat(StringFunctions.includes(List.of(), "a"), is(false));
        assertThat(StringFunctions.includes(null, "a"), is(false));
        assertThat(StringFunctions.includes("abc", "b"), is(true));
        assertThat(StringFunctions.includes("abc", "d"), is(false));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowOnIncludesChecksInNonStringOrCollection() {
        StringFunctions.includes(1, "1");
    }

    @Test
    public void shouldConvertNumberToString() {
        assertThat(StringFunctions.numberToString(1), is("1"));
        assertThat(StringFunctions.numberToString(new BigDecimal("1.0")), is("1"));
        assertThat(StringFunctions.numberToString(new BigDecimal("1")), is("1"));
        assertThat(StringFunctions.numberToString(null), is(""));
        assertThat(StringFunctions.numberToString(
                new BigDecimal("0.0000000003333333333333333")),
                is("0.0000000003333333333333333")
        );
    }

    @Test
    public void shouldAddStringsFromLeft() {
        assertThat(StringFunctions.padLeft("11", "0", 4), is("0011"));
        assertThat(StringFunctions.padLeft("11", "0", 2), is("11"));
        assertThat(StringFunctions.padLeft("11", "0", 1), is("11"));
        assertThat(StringFunctions.padLeft("11", null, 4), is("  11"));
        assertThat(StringFunctions.padLeft("11", "0", null), is("11"));
        assertThat(StringFunctions.padLeft(null, "0", 1), is("0"));
        assertThat(StringFunctions.padLeft(null, "0", 2), is("00"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowOnPadLeftFillerIsMoreThan1LengthLong() {
        StringFunctions.padLeft("a", "-+-", 10);
    }

    @Test
    public void shouldAddStringsFromRight() {
        assertThat(StringFunctions.padRight("11", "0", 4), is("1100"));
        assertThat(StringFunctions.padRight("111", "0", 4), is("1110"));
        assertThat(StringFunctions.padRight("11", "0", 2), is("11"));
        assertThat(StringFunctions.padRight("11", "0", 1), is("11"));
        assertThat(StringFunctions.padRight("11", null, 4), is("11  "));
        assertThat(StringFunctions.padRight("11", "0", null), is("11"));
        assertThat(StringFunctions.padRight(null, "0", 1), is("0"));
        assertThat(StringFunctions.padRight(null, "0", 2), is("00"));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowOnPadRightFillerIsMoreThan1LengthLong() {
        StringFunctions.padRight("a", "-+-", 10);
    }

    @Test
    public void shouldTrimString() {
        assertThat(StringFunctions.trim("  a  "), is("a"));
        assertThat(StringFunctions.trim(""), is(""));
        assertThat(StringFunctions.trim(null), is(nullValue()));
    }

    @Test
    public void shouldConvertStringToUpperCase() {
        assertThat(StringFunctions.upperCase("abc"), is("ABC"));
        assertThat(StringFunctions.upperCase(""), is(""));
        assertThat(StringFunctions.upperCase(null), is(nullValue()));
    }

    @Test
    public void shouldConvertStringToLowerCase() {
        assertThat(StringFunctions.lowerCase("ABC"), is("abc"));
        assertThat(StringFunctions.lowerCase(""), is(""));
        assertThat(StringFunctions.lowerCase(null), is(nullValue()));
    }

    @Test
    public void shouldCheckIsStringStartsWith() {
        assertThat(StringFunctions.startsWith("ABC", "AB"), is(true));
        assertThat(StringFunctions.startsWith("ABC", "B"), is(false));
        assertThat(StringFunctions.startsWith("AB", "ABC"), is(false));
        assertThat(StringFunctions.startsWith("", "ABC"), is(false));
    }

    @Test
    public void shouldCheckIsStringEndsWith() {
        assertThat(StringFunctions.endsWith("ABC", "BC"), is(true));
        assertThat(StringFunctions.endsWith("ABC", "B"), is(false));
        assertThat(StringFunctions.endsWith("BC", "ABC"), is(false));
        assertThat(StringFunctions.endsWith("", "ABC"), is(false));
    }

    @Test
    public void shouldCheckIfStringIsBlank() {
        assertThat(StringFunctions.isBlank(null), is(true));
        assertThat(StringFunctions.isBlank(""), is(true));
        assertThat(StringFunctions.isBlank(" "), is(true));
        assertThat(StringFunctions.isBlank("a"), is(false));
    }
}