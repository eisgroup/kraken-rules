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

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;

import kraken.el.ExpressionEvaluationException;
import kraken.el.functionregistry.Example;
import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionDocumentation;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.Iterable;
import kraken.el.functionregistry.LibraryDocumentation;
import kraken.el.functionregistry.Native;
import kraken.el.functionregistry.NotNull;
import kraken.el.functionregistry.ParameterDocumentation;
import kraken.el.math.Numbers;


/**
 * @author avasiliauskas
 */
@LibraryDocumentation(
    name = "String",
    description = "Functions that operate with String values.",
    since = "1.0.28"
)
@Native
public class StringFunctions implements FunctionLibrary {

    @FunctionDocumentation(
        description = "Return `true` if string parameter is `null`, empty string or consists "
            + "only of whitespace characters.",
        example = {
            @Example(value = "IsBlank(null)", result = "true"),
            @Example(value = "IsBlank('')", result = "true"),
            @Example(value = "IsBlank(' ')", result = "true"),
            @Example(value = "IsBlank('test')", result = "false"),
        }
    )
    @ExpressionFunction("IsBlank")
    public static Boolean isBlank(@ParameterDocumentation(name = "text") String value) {
        return StringUtils.isBlank(value);
    }

    @FunctionDocumentation(
        description = "Return string that is a substring of provided value. The substring begins "
            + "at that specified beginIndex and extends to the end of provided value.",
        example = {
            @Example(value = "Substring('abc', 2)", result = "'c'"),
            @Example(value = "Substring('abc', -1)", validCall = false),
            @Example(value = "Substring('abc', 5)", validCall = false),
        },
        throwsError = "if index is less than 0 or more than a string length"
    )
    @ExpressionFunction("Substring")
    public static String substring(
        @NotNull @ParameterDocumentation(name = "text") String value,
        @NotNull @ParameterDocumentation(name = "beginIndex") Number beginIndex
    ) {
        int beginIndexInt = beginIndex.intValue();
        if (beginIndexInt < 0 || beginIndexInt > value.length()) {
            throw new IllegalStateException(String.format(
                "Cannot perform substring to value: %s, with begin index: %s", value, beginIndex)
            );
        }
        return value.substring(beginIndexInt);
    }

    @FunctionDocumentation(
        description = "Return string that is a substring of provided value. The substring begins at "
            + "specified beginIndex and extends to the character at endIndex - 1.",
        example = {
            @Example(value = "Substring('smiles', 1, 5)", result = "mile"),
            @Example(value = "Substring('smiles', -1, 5)", validCall = false),
            @Example(value = "Substring('smiles', 5, 10)", validCall = false),
            @Example(value = "Substring('smiles', 10, 5)", validCall = false),
        },
        throwsError = "if beginIndex and endIndex are less than 0 or more than a string length"
    )
    @ExpressionFunction("Substring")
    public static String substring(
        @NotNull @ParameterDocumentation(name = "text") String value,
        @NotNull @ParameterDocumentation(name = "beginIndex") Number beginIndex,
        @NotNull @ParameterDocumentation(name = "endIndex") Number endIndex
    ) {
        int beginIndexInt = beginIndex.intValue();
        int endIndexInt = endIndex.intValue();
        if (beginIndexInt > value.length() || beginIndexInt < 0 || endIndexInt > value.length()
            || beginIndexInt > endIndexInt) {
            throw new IllegalStateException(String.format(
                "Cannot perform substring to value: %s, " +
                    "with begin index: %s, and end index: %s", value, beginIndex, endIndex)
            );
        }
        return value.substring(beginIndexInt, endIndexInt);
    }

    @FunctionDocumentation(
        description = "Concatenate objects to one string.",
        example = {
            @Example(value = "Concat({\"a\", \"b\", \"c\", 1})", result = "'abc1'"),
            @Example(value = "Concat(null)", result = "null"),
            @Example(value = "Concat({'a', null, 'c'})", result = "'ac'"),
        }
    )
    @ExpressionFunction("Concat")
    public static String concat(
        @ParameterDocumentation(name = "items") Collection items
    ) {
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

    @FunctionDocumentation(
        description = "Calculate string length. If string is `null`, returns `0`",
        example = {
            @Example(value = "StringLength('four')", result = "4"),
            @Example(value = "StringLength(null)", result = "0"),
        }
    )
    @ExpressionFunction("StringLength")
    public static Integer length(
        @ParameterDocumentation(name = "text") String string
    ) {
        if (string == null) {
            return 0;
        }
        return string.length();
    }

    @FunctionDocumentation(
        description = "Check for element to be included in collection or string to contain string. "
            + "If null appears in as a collection or element to search `false` be returned.",
        example = {
            @Example(value = "Includes('abcd', 'cd')", result = "true"),
            @Example(value = "Includes({1, 2, 3}, 2)", result = "true"),
            @Example(value = "Includes({'a', 'b', 'c'}, 'b')", result = "true"),
            @Example(value = "Includes(null, 2)", result = "false"),
            @Example(value = "Includes('abc', null)", result = "false"),
            @Example(value = "Includes(123, null)", validCall = false),
            @Example(value = "Includes(Policy, null)", validCall = false),
            @Example(value = "Includes(false, null)", validCall = false),
        },
        throwsError = "if element is not a string or a collection"
    )
    @ExpressionFunction("Includes")
    public static Boolean includes(
        @Iterable(false)
        @ParameterDocumentation(name = "collectionOrString", description = "collection or string")
            Object stringsOrString,
        @ParameterDocumentation(
            name = "searchElement",
            description = "element to search in string or collection. If first parameter is string, "
                + "then this parameter must be a string, otherwise type must match collection "
                + "elements type"
        )
            Object searchElement
    ) {
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
        throw new ExpressionEvaluationException("Invalid first parameter passed to function 'Includes'");
    }

    @FunctionDocumentation(
        description = "Create a string from number. If `null` is passed then return empty string.",
        example = {
            @Example(value = "NumberToString(2)", result = "'2'"),
            @Example(value = "NumberToString(null)", result = "null"),
        }
    )
    @ExpressionFunction("NumberToString")
    public static String numberToString(@ParameterDocumentation(name = "number") Number number) {
        if (number == null) {
            return "";
        }
        return Numbers.toString(number);
    }

    @FunctionDocumentation(
        description = "Pad string on the left side if it's shorter than length. "
            + "If it is longer than length, return base string as is.",
        throwsError = "if filler string has more than one character",
        example = {
            @Example(value = "PadLeft('1', '0', '4')", result = "0001"),
            @Example(value = "PadLeft('1', ' ', '4')", result = "   1"),
            @Example(value = "PadLeft('1', null, '4')", result = "   1"),
            @Example(value = "PadLeft('1', 'abc', '4')", validCall = false),
        }
    )
    @ExpressionFunction("PadLeft")
    public static String padLeft(
        @ParameterDocumentation(
            name = "base",
            description = "string to pad. `null` equals to an empty string `''`"
        )
            String base,
        @ParameterDocumentation(
            name = "filler",
            description = "to fill the space to desired length. String of one char "
                + "is acceptable. Null equals to ' '"
        )
            String filler,
        @ParameterDocumentation(
            name = "length",
            description = "length of final string, if `null` equals to `0`"
        )
            Number lengthOfFinalString
    ) {
        String fxBase = base == null ? "" : base;
        String fxFiller = filler == null ? " " : filler;
        int fxLength = lengthOfFinalString == null ? 0 : lengthOfFinalString.intValue();
        if (fxFiller.length() > 1) {
            throw new ExpressionEvaluationException("Second parameters in 'PadLeft' function length must be '1', " +
                "instead got '" + fxFiller.length() + "', as a parameter: '" + fxFiller + "'");
        }
        if (fxBase.length() >= fxLength) {
            return base;
        }
        int numberOfCharsToFill = fxLength - fxBase.length();
        String repeated = fxFiller.repeat(numberOfCharsToFill);
        return repeated + fxBase;
    }

    @FunctionDocumentation(
        description = "Pad string on the right side if it's shorter than length. "
            + "If it is longer than length, return base string as is.",
        throwsError = "if filler string has more than one character",
        example = {
            @Example(value = "PadRight('1', '0', '4')", result = "1000"),
            @Example(value = "PadRight('1', ' ', '4')", result = "1    "),
            @Example(value = "PadRight('1', null, '4')", result = "1    "),
            @Example(value = "PadRight('1', 'abc', '4')", validCall = false),
        }
    )
    @ExpressionFunction("PadRight")
    public static String padRight(
        @ParameterDocumentation(
            name = "base",
            description = "string to pad. `null` equals to an empty string `''`"
        )
            String base,
        @ParameterDocumentation(
            name = "filler",
            description = "to fill the space to desired length. String of one char "
                + "is acceptable. Null equals to ' '"
        )
            String filler,
        @ParameterDocumentation(
            name = "length",
            description = "length of final string, if `null` equals to `0`"
        )
            Number lengthOfFinalString
    ) {
        String fxBase = base == null ? "" : base;
        String fxFiller = filler == null ? " " : filler;
        int fxLength = lengthOfFinalString == null ? 0 : lengthOfFinalString.intValue();
        if (fxFiller.length() > 1) {
            throw new ExpressionEvaluationException("Second parameters in 'PadRight' function length must be '1', " +
                "instead got '" + fxFiller.length() + "', as a parameter: '" + fxFiller + "'");
        }
        if (fxBase.length() >= fxLength) {
            return base;
        }
        int numberOfCharsToFill = fxLength - fxBase.length();
        String repeated = fxFiller.repeat(numberOfCharsToFill);
        return fxBase + repeated;
    }

    @FunctionDocumentation(
        description = "Remove leading and trailing whitespaces. If text is `null`, return null`.",
        example = {
            @Example(value = "Trim(' a ')", result = "'a'"),
            @Example(value = "Trim(null)", result = "null"),
        }
    )
    @ExpressionFunction("Trim")
    public static String trim(@ParameterDocumentation(name = "text") String text) {
        if (text == null) {
            return null;
        }
        return text.trim();
    }

    @FunctionDocumentation(
        description = "Convert text to upper case. If text is `null`, return `null`.",
        example = {
            @Example(value = "UpperCase('abc')", result = "'ABC'"),
            @Example(value = "UpperCase(null)", result = "null")
        }
    )
    @ExpressionFunction("UpperCase")
    public static String upperCase(@ParameterDocumentation(name = "text") String text) {
        if (text == null) {
            return null;
        }
        return text.toUpperCase();
    }

    @FunctionDocumentation(
        description = "Convert text to lower case. If text is `null`, return `null`.",
        example = {
            @Example(value = "LowerCase('AbC')", result = "'abc'"),
            @Example(value = "LowerCase(null)", result = "null"),
        }
    )
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
     * @param text  to check for start.
     * @param start to be at the start of the text.
     */
    @FunctionDocumentation(
        description = "Check the string to start with any text. "
            + "If text to check is shorter than subtext, return `false`.",
        example = {
            @Example(value = "StartsWith(\"abc123\", \"abc\")", result = "true"),
            @Example(value = "StartsWith(\"abc123\", \"123\")", result = "false"),
        }
    )
    @ExpressionFunction("StartsWith")
    public static Boolean startsWith(
        @NotNull @ParameterDocumentation(name = "text", description = "to check")
            String text,
        @NotNull @ParameterDocumentation(name = "start", description = "to be at the start of the text")
            String start
    ) {
        return text.startsWith(start);
    }

    @FunctionDocumentation(
        description = "Check string to end with any text. If text to check is shorter "
            + "than subtext, return `false`.",
        example = {
            @Example(value = "EndsWith(\"123abc\", \"abc\")", result = "true"),
            @Example(value = "EndsWith(\"bc\", \"abc\")", result = "false"),
        }
    )
    @ExpressionFunction("EndsWith")
    public static Boolean endsWith(
        @NotNull @ParameterDocumentation(name = "text", description = "to check")
            String text,
        @NotNull @ParameterDocumentation(name = "end", description = "to be at the end of the text")
            String end
    ) {
        return text.endsWith(end);
    }

}
