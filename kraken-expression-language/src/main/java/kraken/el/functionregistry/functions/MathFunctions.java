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
package kraken.el.functionregistry.functions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.money.MonetaryAmount;

import org.apache.commons.lang3.Range;

import kraken.el.ExpressionEvaluationException;
import kraken.el.functionregistry.Example;
import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionDocumentation;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.GenericType;
import kraken.el.functionregistry.LibraryDocumentation;
import kraken.el.functionregistry.Native;
import kraken.el.functionregistry.NotNull;
import kraken.el.functionregistry.ParameterDocumentation;
import kraken.el.functionregistry.ParameterType;
import kraken.el.math.Numbers;

/**
 * @author mulevicius
 */
@LibraryDocumentation(
    name = "Math",
    description = "Functions that operate with numerical values.",
    since = "1.0.28"
)
@Native
public class MathFunctions implements FunctionLibrary {

    @FunctionDocumentation(
        description = "Rounds to Integer using IEEE 754 Round Half Up strategy. "
            + "If the number falls midway, then it is rounded to the nearest "
            + "value above (for positive numbers) or below (for negative numbers).",
        example = {
            @Example(value = "Round(1.5)", result = "2"),
            @Example(value = "Round(-1.5)", result = "-2"),
            @Example(value = "Round(2.5)", result = "3"),
            @Example(value = "Round(-2.5)", result = "-3"),
        },
        since = "1.5.0"
    )
    @ExpressionFunction("Round")
    public static Number round(
        @NotNull @ParameterDocumentation(name = "number") Number number
    ) {
        return Numbers.round(number);
    }

    @FunctionDocumentation(
        description = "Rounds to Integer using IEEE 754 Round Half Even strategy. "
            + "If the number falls midway, then it is rounded to the nearest value "
            + "with an even least significant digit.",
        example = {
            @Example(value = "RoundEven(1.5)", result = "2"),
            @Example(value = "RoundEven(-1.5)", result = "-2"),
            @Example(value = "RoundEven(2.5)", result = "2"),
            @Example(value = "RoundEven(-2.5)", result = "-2"),
        },
        since = "1.5.0"
    )
    @ExpressionFunction("RoundEven")
    public static Number roundEven(
        @NotNull @ParameterDocumentation(name = "number") Number number
    ) {
        return Numbers.roundEven(number);
    }

    @FunctionDocumentation(
        description = "Rounds to scale using IEEE 754 Round Half Up strategy. "
            + "If the number falls midway, then it is rounded to the nearest "
            + "value above (for positive numbers) or below (for negative numbers).",
        since = "1.5.0",
        example = {
            @Example(value = "Round(11.555, 2)", result = "11.56"),
            @Example(value = "Round(-11.555, 2)", result = "-11.56"),
            @Example(value = "Round(11.565, 2)", result = "11.57"),
            @Example(value = "Round(-11.565, 2)", result = "-11.57"),
        }
    )
    @ExpressionFunction("Round")
    public static Number round(
        @NotNull @ParameterDocumentation(name = "number") Number number,
        @NotNull
        @ParameterDocumentation(
            name = "scale",
            description = "Indicates how many floating point digits should remain after rounding. "
                + "If scale is zero, then number will be rounded to integer"
        ) Number scale
    ) {
        return Numbers.round(number, scale.intValue());
    }

    @FunctionDocumentation(
        since = "1.5.0",
        description = "Rounds to Integer using IEEE 754 Round Half Even strategy. "
            + "If the number falls midway, then it is rounded to the nearest "
            + "value with an even least significant digit.",
        example = {
            @Example(value = "RoundEven(11.555, 2)", result = "11.56"),
            @Example(value = "RoundEven(-11.555, 2)", result = "-11.56"),
            @Example(value = "RoundEven(11.565, 2)", result = "11.56"),
            @Example(value = "RoundEven(-11.565, 2)", result = "-11.56"),
        }
    )
    @ExpressionFunction("RoundEven")
    public static Number roundEven(
        @NotNull
        @ParameterDocumentation(name = "number")
            Number number,
        @NotNull
        @ParameterDocumentation(
            name = "scale",
            description = "Indicates how many floating point digits should remain after rounding. "
                + "If scale is zero, then number will be rounded to integer"
        )
            Number scale
    ) {
        return Numbers.roundEven(number, scale.intValue());
    }

    @FunctionDocumentation(
        description = "Sum all values in collection and return single value. "
            + "Can sum only numbers. When collection is `null` or empty, returns `null`.",
        throwsError = "if value in collection is not a number",
        example = {
            @Example(value = "Sum({1, 2, 3})", result = "6"),
            @Example(value = "Sum({})", result = "null"),
            @Example(value = "Sum(null)", result = "null"),
            @Example(value = "Sum({1, 2, null})", validCall = false),
            @Example(value = "Sum(Coverage.limitAmount)"),
        }
    )
    @ExpressionFunction("Sum")
    public static Number sum(
        @ParameterType("Number[]")
        @ParameterDocumentation(name = "collection")
            Collection<?> collection
    ) {
        return Optional.ofNullable(collection)
            .map(MathFunctions::toCollectionOfNumbers)
            .map(Numbers::sum)
            .orElse(null);
    }

    @FunctionDocumentation(
        description = "Find average value from all values provided in the "
            + "collection and return a single value. "
            + "Can find average only from numbers. When collection is null or empty, returns `null`.",
        example = {
            @Example(value = "Avg({1,2,3}})", result = "2"),
            @Example(value = "Avg({})", result = "null"),
            @Example(value = "Avg(null)", result = "null"),
            @Example(value = "Avg({1, 2, null})", validCall = false),
            @Example("Avg(Coverage.limitAmount)"),
        }
    )
    @ExpressionFunction("Avg")
    public static Number avg(
        @ParameterType("Number[]")
        @ParameterDocumentation(name = "collection")
            Collection<?> collection
    ) {
        return Optional.ofNullable(collection)
            .map(MathFunctions::toCollectionOfNumbers)
            .map(Numbers::avg)
            .orElse(null);
    }

    @FunctionDocumentation(
        description = "Calculates a result of a mathematical floor function which "
            + "returns greatest integer that is less than or equal to provided number.",
        example = {
            @Example(value = "Floor(1.9", result = "1"),
            @Example(value = "Floor(-1.9", result = "1"),
        },
        since = "1.5.0"
    )
    @ExpressionFunction("Floor")
    public static Number floor(@NotNull @ParameterDocumentation(name = "number") Number number) {
        return Numbers.floor(number);
    }

    @FunctionDocumentation(
        description = "Calculates a result of a mathematical ceiling function which returns "
            + "the least integer that is greater than or equal to provided number.",
        example = {
            @Example(value = "Ceil(1.1)", result = "2"),
            @Example(value = "Ceil(-1.9)", result = "-1"),
        },
        since = "1.5.0"
    )
    @ExpressionFunction("Ceil")
    public static Number ceil(@NotNull @ParameterDocumentation(name = "number") Number number) {
        return Numbers.ceil(number);
    }

    @FunctionDocumentation(
        description = "Calculates absolute number.",
        example = {
            @Example(value = "Abs(-5)", result = "5"),
            @Example(value = "Abs(5)", result = "5"),
        },
        since = "1.5.0"
    )
    @ExpressionFunction("Abs")
    public static Number abs(@NotNull @ParameterDocumentation(name = "number") Number number) {
        return Numbers.abs(number);
    }

    @FunctionDocumentation(
        description = "Calculates sign of a number. Returns 1 when number is positive, "
            + "0 when number is zero and -1 when number is negative.",
        since = "1.5.0",
        example = {
            @Example(value = "Sign(100)", result = "1"),
            @Example(value = "Sign(0)", result = "0"),
            @Example(value = "Sign(-100)", result = "-1"),
        }
    )
    @ExpressionFunction("Sign")
    public static Number sign(@NotNull @ParameterDocumentation(name = "number") Number number) {
        return Numbers.sign(number);
    }

    @FunctionDocumentation(
        description = "Returns smaller of the two numbers.",
        example = { 
            @Example(value = "Min(1, 2)", result = "1"), 
            @Example(value = "Min(2020-01-01, 2021-01-01)", result = "2020-01-01"),
        },
        since = "1.5.0"
    )
    @ExpressionFunction(
        value = "Min",
        genericTypes = @GenericType(generic = "T", bound = "Number | Date | DateTime")
    )
    public static <T> T min(
        @NotNull @ParameterDocumentation(name = "first") T first,
        @NotNull @ParameterDocumentation(name = "second") T second
    ) {
        if(isNumber(first) && isNumber(second)) {
            return (T) Numbers.min(asNumber(first), asNumber(second));
        }
        if(first instanceof LocalDate && second instanceof LocalDate) {
            return (T) minDate(List.of((LocalDate) first, (LocalDate) second));
        }
        if(first instanceof LocalDateTime && second instanceof LocalDateTime) {
            return (T) minTime(List.of((LocalDateTime) first, (LocalDateTime) second));
        }
        throw new ExpressionEvaluationException(
            "Function 'Min' parameters are invalid: " + first + ", " + second + ". "
                + "Parameters must be only numbers or only dates."
        );
    }

    @FunctionDocumentation(
        description = "Returns larger of the two numbers, dates or times.",
        example = { 
            @Example(value = "Max(1, 2)", result = "2"),
            @Example(value = "Max(2020-01-01, 2021-01-01)", result = "2021-01-01"),
        },
        since = "1.5.0"
    )
    @ExpressionFunction(
        value = "Max",
        genericTypes = @GenericType(generic = "T", bound = "Number | Date | DateTime")
    )
    public static <T> T max(
        @NotNull @ParameterDocumentation(name = "first") T first,
        @NotNull @ParameterDocumentation(name = "second") T second
    ) {
       if(isNumber(first) && isNumber(second)) {
           return (T) Numbers.max(asNumber(first), asNumber(second));
       }
       if(first instanceof LocalDate && second instanceof LocalDate) {
           return (T) maxDate(List.of((LocalDate) first, (LocalDate) second));
       }
       if(first instanceof LocalDateTime && second instanceof LocalDateTime) {
           return (T) maxTime(List.of((LocalDateTime) first, (LocalDateTime) second));
       }
       throw new ExpressionEvaluationException(
           "Function 'Max' parameters are invalid: " + first + ", " + second + ". "
               + "Parameters must be only numbers or only dates."
       );
    }

    @FunctionDocumentation(
        description = "Finds the smallest value between all values in the collection. "
            + "Collection must contain Number, Date or DateTime values. "
            + "If collection is null or empty, then this function returns `null`.",
        throwsError = "If collection contains value other than Number, Date or DateTime.",
        example = {
            @Example(value = "Min({1,2})", result = "1"),
            @Example(value = "Min({})", result = "null"),
            @Example(value = "Min(null)", result = "null"),
            @Example(value = "Min({2020-01-01, 2021-01-01})", result = "2020-01-01"),
            @Example(value = "Min({2020-01-01, 1})", validCall = false),
            @Example(value = "Min({1, 2, null})", validCall = false),
        }
    )
    @ExpressionFunction(
        value = "Min",
        genericTypes = @GenericType(generic = "T", bound = "Number | Date | DateTime")
    )
    public static <T> T min(
        @ParameterDocumentation(name = "collection")
            Collection<T> collection
    ) {
        if(collection == null || collection.isEmpty()) {
            return null;
        }
        if(collection.stream().allMatch(MathFunctions::isNumber)) {
            return (T) Numbers.min(MathFunctions.toCollectionOfNumbers(collection));
        }
        if(collection.stream().allMatch(i -> i instanceof LocalDate)) {
            var dates = collection.stream().map(i -> (LocalDate) i).collect(Collectors.toList());
            return (T) minDate(dates);
        }
        if(collection.stream().allMatch(i -> i instanceof LocalDateTime)) {
            var times = collection.stream().map(i -> (LocalDateTime) i).collect(Collectors.toList());
            return (T) minTime(times);
        }
        throw new ExpressionEvaluationException(
            "Function 'Min' parameter is invalid. Parameter must be a collection of Number, LocalDate or LocalDateTime."
        );
    }

    @FunctionDocumentation(
        description = "Finds the largest value between all values in the collection. "
            + "Collection must contain Number, Date or DateTime values. "
            + "If collection is null or empty, then this function returns `null`.",
        throwsError = "If Collection contains value other than Number, Date or DateTime.",
        example = {
            @Example(value = "Max({1,2})", result = "2"),
            @Example(value = "Max({})", result = "null"),
            @Example(value = "Max(null)", result = "null"),
            @Example(value = "Max({2020-01-01, 2021-01-01})", result = "2021-01-01"),
            @Example(value = "Max({2020-01-01, 1})", validCall = false),
            @Example(value = "Max({1, 2, null})", validCall = false),
        }
    )
    @ExpressionFunction(
        value = "Max",
        genericTypes = @GenericType(generic = "T", bound = "Number | Date | DateTime")
    )
    public static <T> T max(
        @ParameterDocumentation(name = "collection")
            Collection<T> collection
    ) {
        if(collection == null || collection.isEmpty()) {
            return null;
        }
        if(collection.stream().allMatch(MathFunctions::isNumber)) {
            return (T) Numbers.max(MathFunctions.toCollectionOfNumbers(collection));
        }
        if(collection.stream().allMatch(i -> i instanceof LocalDate)) {
            var dates = collection.stream().map(i -> (LocalDate) i).collect(Collectors.toList());
            return (T) maxDate(dates);
        }
        if(collection.stream().allMatch(i -> i instanceof LocalDateTime)) {
            var times = collection.stream().map(i -> (LocalDateTime) i).collect(Collectors.toList());
            return (T) maxTime(times);
        }
        throw new ExpressionEvaluationException(
            "Function 'Max' parameter is invalid. Parameter must be a collection of Number, LocalDate or LocalDateTime."
        );
    }

    @FunctionDocumentation(
        description = "Calculates squared root of a number..",
        example = {
            @Example(value = "Sqrt(1)", result = "2"),
        },
        since = "1.5.0"
    )
    @ExpressionFunction("Sqrt")
    public static Number sqrt(@NotNull @ParameterDocumentation(name = "number") Number number) {
        return Numbers.sqrt(number);
    }

    @FunctionDocumentation(
        throwsError = "if step is positive and from->to parameters are going negative and vice versa",
        description = "Generates a sequence of numbers by adding 1 or -1 to the starting number until "
            + "it reaches the ending number inclusively. Returns a collection of numbers in order. "
            + "Parameters cannot be `null`.",
        since = "1.0.35",
        example = {
            @Example(value = "NumberSequence(0, 5)", result = "{0,1,2,3,4,5}"),
            @Example(value = "NumberSequence(5, 0)", result = "{5,4,3,2,1,0}"),
            @Example(value = "NumberSequence(0, 5)", result = "{0}"),
        }
    )
    @ExpressionFunction("NumberSequence")
    public static List<Number> numberSequence(
        @NotNull @ParameterDocumentation(name = "from", description = "sequence starting number") Number from,
        @NotNull @ParameterDocumentation(name = "to", description = "sequence end number") Number to,
        @NotNull @ParameterDocumentation(name = "step", description = "number to add each step") Number step
    ) {
        final BigDecimal start = Numbers.normalized(from);
        final BigDecimal end = Numbers.normalized(to);
        final BigDecimal bdStep = Numbers.normalized(step);

        validateDirection(start, end, bdStep);

        return sequence(start, end, bdStep);
    }

    @FunctionDocumentation(
        description = "Generates a sequence of numbers by adding 1 or -1 to the starting number until "
            + "it reaches the ending number inclusively. Returns a collection of numbers in order. "
            + "Parameters cannot be `null`.",
        example = {
            @Example(value = "NumberSequence(0, 5)", result = "{0,1,2,3,4,5}"),
        }
    )
    @ExpressionFunction("NumberSequence")
    public static List<Number> numberSequence(
        @NotNull @ParameterDocumentation(name = "from") Number from,
        @NotNull @ParameterDocumentation(name = "to") Number to
    ) {
        BigDecimal start = Numbers.normalized(from);
        BigDecimal end = Numbers.normalized(to);
        BigDecimal step = new BigDecimal(start.compareTo(end) > 0 ? -1 : 1);
        return sequence(start, end, step);
    }

    private static List<Number> sequence(BigDecimal start, BigDecimal end, BigDecimal step) {
        final List<Number> sequence = new ArrayList<>();
        final Range<BigDecimal> between = Range.between(start, end);
        while (between.contains(start)) {
            sequence.add(start);
            start = start.add(step);
        }
        return sequence;
    }

    private static void validateDirection(BigDecimal start, BigDecimal end, BigDecimal step) {
        final int direction = end.compareTo(start);
        final int directionOfStep = step.signum();
        if (directionOfStep == 0 || direction != directionOfStep) {
            throw new ExpressionEvaluationException(
                "Function 'NumberSequence' parameters are invalid: "
                    + start
                    + ", "
                    + end
                    + ", "
                    + step
                    + ". These parameters would generate an infinite sequence of numbers."
            );
        }
    }

    /**
     * Special manual handling for number collections
     * because currently KEL does not convert collections of Money to Numbers automatically
     *
     * @param collection
     * @return
     */
    private static Collection<Number> toCollectionOfNumbers(Collection<?> collection) {
        return collection.stream().map(MathFunctions::asNumber).collect(Collectors.toList());
    }

    private static boolean isNumber(Object value) {
        return value instanceof MonetaryAmount || value instanceof Number;
    }

    private static Number asNumber(Object value) {
        if (value instanceof MonetaryAmount) {
            return Numbers.fromMoney((MonetaryAmount) value);
        }
        if (value instanceof Number) {
            return (Number) value;
        }
        var valueDescription = "";
        if (value == null) {
            valueDescription = "null";
        } else {
            valueDescription = "value ('" + value + " of type " + value.getClass().getName() + "')";
        }
        throw new ExpressionEvaluationException(
            "Collection function can be invoked only with collection that contains numbers, " +
                "but it was invoked with " + valueDescription);
    }

    public static LocalDate maxDate(Collection<LocalDate> dates) {
        return dates.stream().max(Comparator.naturalOrder()).orElse(null);
    }

    public static LocalDate minDate(Collection<LocalDate> dates) {
        return dates.stream().min(Comparator.naturalOrder()).orElse(null);
    }

    public static LocalDateTime maxTime(Collection<LocalDateTime> times) {
        return times.stream().max(Comparator.naturalOrder()).orElse(null);
    }

    public static LocalDateTime minTime(Collection<LocalDateTime> times) {
        return times.stream().min(Comparator.naturalOrder()).orElse(null);
    }

}
