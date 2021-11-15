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

import java.util.Collection;

import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.Iterable;
import kraken.el.functionregistry.Native;
import kraken.el.functionregistry.NotNull;
import kraken.el.math.Numbers;
import org.apache.commons.lang3.StringUtils;

/**
 * @author avasiliauskas
 */
@Native
public class StringFunctions implements FunctionLibrary {

    /**
     *
     * @param value
     * @return true if string is null, empty, or consists of whitespace characters only
     */
    @ExpressionFunction("IsBlank")
    public static Boolean isBlank(String value) {
        return StringUtils.isBlank(value);
    }

    /**
     * Extracting text by parameters from provided text.
     * Ex. Substring("SubstringValue", 3) == "stringValue"
     *
     * @param value      to substring
     * @param beginIndex
     * @return left overs of substring
     * @throws IllegalStateException
     */
    @ExpressionFunction("Substring")
    public static String substring(@NotNull String value, @NotNull Number beginIndex) {
        int beginIndexInt = beginIndex.intValue();
        if (beginIndexInt < 0 || beginIndexInt > value.length()) {
            throw new IllegalStateException(String.format(
                    "Cannot perform substring to value: %s, with begin index: %s", value, beginIndex)
            );
        }
        return value.substring(beginIndexInt);
    }

    /**
     * Substring provided value.
     * Begin index is index which from substring of value begins and
     * End index is ending index which to substring will end on provided value.
     * Ex. Substring("SubstringValue", 3, 9) == "string"
     *
     * @param value      to substring
     * @param beginIndex
     * @return left overs of substring
     * @throws IllegalStateException
     */
    @ExpressionFunction("Substring")
    public static String substring(@NotNull String value, @NotNull Number beginIndex, @NotNull Number endIndex) {
        int beginIndexInt = beginIndex.intValue();
        int endIndexInt = endIndex.intValue();
        if (beginIndexInt > value.length() || beginIndexInt < 0 || endIndexInt > value.length() || beginIndexInt > endIndexInt) {
            throw new IllegalStateException(String.format(
                    "Cannot perform substring to value: %s, " +
                            "with begin index: %s, and end index: %s", value, beginIndex, endIndex)
            );
        }
        return value.substring(beginIndexInt, endIndexInt);
    }

    /**
     * Concatenates objects to one string. For correct concatenation better to use {@link Integer},
     * {@link java.math.BigDecimal}, {@link String}.
     *
     * @param items to concatenate
     * @return concatenated string
     */
    @ExpressionFunction("Concat")
    public static String concat(Collection items) {
        if (items == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (Object string : items) {
            if (string != null) {
                stringBuilder.append(string);
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Calculates string length. If string is {@code null}, returns {@code 0}
     *
     * @param string to calculate length of
     * @return length of string
     */
    @ExpressionFunction("StringLength")
    public static Integer length(String string) {
        if (string == null) {
            return 0;
        }
        return string.length();
    }

    /**
     * Checks for element to be included in collection or string to contain string.
     * If {@code null} appears in as a collection or element to search {@code false}
     * will be returned.
     *
     * @param stringsOrString collection or string of elements to be checked
     * @param searchElement   element to search in string or collection
     * @return does collection or string contains search element.
     * @throws IllegalStateException if not collection or string is passed as an item to search in.
     */
    @ExpressionFunction("Includes")
    public static Boolean includes(@Iterable(false) Object stringsOrString, Object searchElement) {
        if (stringsOrString == null) {
            return false;
        }
        if (searchElement == null) {
            return false;
        }
        if (stringsOrString instanceof String) {
            return ((String) stringsOrString).contains(searchElement.toString());
        }
        if (stringsOrString instanceof Collection) {
            return ((Collection) stringsOrString).contains(searchElement);
        }
        throw new IllegalStateException("Invalid first parameter passed to function 'Includes'");
    }

    /**
     * Creates a string from number. If {@code null} is passed then empty string will be returned.
     *
     * @param number to convert to string
     * @return number converted to string
     */
    @ExpressionFunction("NumberToString")
    public static String numberToString(Number number) {
        if (number == null) {
            return "";
        }
        return Numbers.toString(number);
    }

    /**
     * Pads string on the left side if it's shorter than length. If it is longer that length,
     * base string will be returned as is.
     *
     * @param base                string to pad. Default is ""
     * @param filler              to fill the space to desired length. Only one length strings are applicable here. Default is " "
     * @param lengthOfFinalString length of final string
     * @return padded string.
     * @throws IllegalStateException if second parameters length is more than one
     */
    @ExpressionFunction("PadLeft")
    public static String padLeft(String base, String filler, Number lengthOfFinalString) {
        String fxBase = base == null ? "" : base;
        String fxFiller = filler == null ? " " : filler;
        int fxLength = lengthOfFinalString == null ? 0 : lengthOfFinalString.intValue();
        if (fxFiller.length() > 1) {
            throw new IllegalStateException("Second parameters in 'PadLeft' function length must be '1', " +
                    "instead got '" + fxFiller.length() + "', as a parameter: '" + fxFiller + "'");
        }
        if (fxBase.length() >= fxLength) {
            return base;
        }
        int numberOfCharsToFill = fxLength - fxBase.length();
        String repeated = fxFiller.repeat(numberOfCharsToFill);
        return repeated + fxBase;
    }

    /**
     * Pads string on the right side if it's shorter than length. If it is longer that length,
     * base string will be returned as is.
     *
     * @param base                string to pad. Default is ""
     * @param filler              to fill the space to desired length. Only one length strings are applicable here. Default is " "
     * @param lengthOfFinalString length of final string. Default is 0.
     * @return padded string.
     * @throws IllegalStateException if second parameters length is more than one
     */
    @ExpressionFunction("PadRight")
    public static String padRight(String base, String filler, Number lengthOfFinalString) {
        String fxBase = base == null ? "" : base;
        String fxFiller = filler == null ? " " : filler;
        int fxLength = lengthOfFinalString == null ? 0 : lengthOfFinalString.intValue();
        if (fxFiller.length() > 1) {
            throw new IllegalStateException("Second parameters in 'PadRight' function length must be '1', " +
                    "instead got '" + fxFiller.length() + "', as a parameter: '" + fxFiller + "'");
        }
        if (fxBase.length() >= fxLength) {
            return base;
        }
        int numberOfCharsToFill = fxLength - fxBase.length();
        String repeated = fxFiller.repeat(numberOfCharsToFill);
        return fxBase + repeated;
    }

    /**
     * Removes leading and trailing whitespaces.
     * If text is {@code null}, null will be returned
     *
     * @param text to trim
     * @return trimmed text
     */
    @ExpressionFunction("Trim")
    public static String trim(String text) {
        if (text == null) {
            return null;
        }
        return text.trim();
    }

    /**
     * Converts text to uppercase.
     * If text is {@code null}, null will be returned
     *
     * @param text to convert.
     * @return converted text to uppercase
     */
    @ExpressionFunction("UpperCase")
    public static String upperCase(String text) {
        if (text == null) {
            return null;
        }
        return text.toUpperCase();
    }


    /**
     * Converts text to lowercase.
     * If text is {@code null}, null will be returned
     *
     * @param text to convert
     * @return converted text to uppercase
     */
    @ExpressionFunction("LowerCase")
    public static String lowerCase(String text) {
        if (text == null) {
            return null;
        }
        return text.toLowerCase();
    }
    /**
     * Checks string to start with some text. If text to check is shorter, than subtext,
     * {@code false} will be returned.
     *
     * @param text to check for start.
     * @param start to be at the start of the text.
     */
    @ExpressionFunction("StartsWith")
    public static Boolean startsWith(@NotNull String text, @NotNull String start) {
        return text.startsWith(start);
    }

    /**
     * Checks string to ends with some text. If text to check is shorter, than subtext,
     * {@code false} will be returned.
     *
     * @param text to check for end.
     * @param end to be at the end of the text.
     */
    @ExpressionFunction("EndsWith")
    public static Boolean endsWith(@NotNull String text, @NotNull String end) {
        return text.endsWith(end);
    }

}
