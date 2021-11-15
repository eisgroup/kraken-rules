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
package kraken.el.functions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.money.MonetaryAmount;

import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.Native;
import kraken.el.functionregistry.NotNull;
import kraken.el.math.Numbers;
import org.apache.commons.lang3.Range;

import static kraken.el.math.Numbers.normalized;

/**
 * @author mulevicius
 */
@Native
public class MathFunctions implements FunctionLibrary {

    /**
     * Rounds to Integer using IEEE 754 Round Half Up strategy.
     * <p/>
     * A Half Up strategy:
     * if the number falls midway, then it is rounded to the nearest value above (for positive numbers) or below (for negative numbers)
     *
     * @param number
     * @return rounded number
     */
    @ExpressionFunction("Round")
    public static Number round(@NotNull Number number) {
        return Numbers.round(number);
    }

    /**
     * Rounds to Integer using IEEE 754 Round Half To Even strategy
     * <p/>
     * A Half To Even strategy:
     * if the number falls midway, then it is rounded to the nearest value with an even least significant digit
     *
     * @param number
     * @return rounded number
     */
    @ExpressionFunction("RoundEven")
    public static Number roundEven(@NotNull Number number) {
        return Numbers.roundEven(number);
    }

    /**
     * Rounds to scale using IEEE 754 Round Half Up strategy
     * <p/>
     * A Half Up strategy:
     * if the number falls midway, then it is rounded to the nearest value above (for positive numbers) or below (for negative numbers)
     *
     * @param number
     * @param scale indicates how many floating point digits should remain after rounding.
     *              If scale is zero, then number will be rounded to integer.
     * @return rounded number
     */
    @ExpressionFunction("Round")
    public static Number round(@NotNull Number number, @NotNull Number scale) {
        return Numbers.round(number, scale.intValue());
    }

    /**
     * Rounds to scale using IEEE 754 Round Half To Even strategy
     * <p/>
     * A Half To Even strategy:
     * if the number falls midway, then it is rounded to the nearest value with an even least significant digit
     *
     * @param number
     * @param scale indicates how many floating point digits should remain after rounding.
     *              If scale is zero, then number will be rounded to integer.
     * @return rounded number
     */
    @ExpressionFunction("RoundEven")
    public static Number roundEven(@NotNull Number number, @NotNull Number scale) {
        return Numbers.roundEven(number, scale.intValue());
    }

    /**
     *
     * @param collection
     * @return Sum all values from provided collection
     */
    @ExpressionFunction("Sum")
    public static Number sum(Collection<?> collection) {
        return Optional.ofNullable(collection)
                .map(MathFunctions::toCollectionOfNumbers)
                .map(Numbers::sum)
                .orElse(null);
    }

    /**
     * Calculate average of all values in provided collection
     *
     * @param collection
     * @return
     */
    @ExpressionFunction("Avg")
    public static Number avg(Collection<?> collection) {
        return Optional.ofNullable(collection)
                .map(MathFunctions::toCollectionOfNumbers)
                .map(Numbers::avg)
                .orElse(null);
    }

    /**
     * Find lowest value in provided collection
     *
     * @param collection
     * @return
     */
    @ExpressionFunction("Min")
    public static Number min(Collection<?> collection) {
        return Optional.ofNullable(collection)
                .map(MathFunctions::toCollectionOfNumbers)
                .map(coll -> Numbers.min(coll))
                .orElse(null);
    }

    /**
     * Find highest value in provided collection
     *
     * @param collection
     * @return
     */
    @ExpressionFunction("Max")
    public static Number max(Collection<?> collection) {
        return Optional.ofNullable(collection)
                .map(MathFunctions::toCollectionOfNumbers)
                .map(Numbers::max)
                .orElse(null);
    }

    /**
     * @param number
     * @return  result of a mathematical floor function which returns
     *          greatest integer that is less than or equal to provided number
     */
    @ExpressionFunction("Floor")
    public static Number floor(@NotNull Number number) {
        return Numbers.floor(number);
    }

    /**
     * @param number
     * @return  result of a mathematical ceiling function which returns
     *          the least integer that is greater than or equal to provided number
     */
    @ExpressionFunction("Ceil")
    public static Number ceil(@NotNull Number number) {
        return Numbers.ceil(number);
    }

    /**
     *
     * @param number
     * @return absolute number
     */
    @ExpressionFunction("Abs")
    public static Number abs(@NotNull Number number) {
        return Numbers.abs(number);
    }

    /**
     *
     * @param number
     * @return  a result of a mathematical signum function, which returns:
     *          <ul>
     *              <li>-1 - if number is less than zero</li>
     *              <li> 0 - if number is equal to zero</li>
     *              <li>+1 - if number is greater than zero</li>
     *          </ul>
     */
    @ExpressionFunction("Sign")
    public static Number sign(@NotNull Number number) {
        return Numbers.sign(number);
    }

    /**
     *
     * @param first
     * @param second
     * @return smaller of the two numbers
     */
    @ExpressionFunction("Min")
    public static Number min(@NotNull Number first, @NotNull Number second) {
        return Numbers.min(first, second);
    }

    /**
     *
     * @param first
     * @param second
     * @return larger of the two numbers
     */
    @ExpressionFunction("Max")
    public static Number max(@NotNull Number first, @NotNull Number second) {
        return Numbers.max(first, second);
    }

    /**
     *
     * @param number
     * @return square root
     */
    @ExpressionFunction("Sqrt")
    public static Number sqrt(@NotNull Number number) {
        return Numbers.sqrt(number);
    }

    /**
     * Generates a sequence of numbers by adding <step> to starting number until it is less than
     * or equals to ending number. Returns a collection of numbers in order.
     * Parameters cannot be {@code null}.
     *
     * @param from sequence starting number
     * @param to   sequence end number
     * @param step number to add each step
     * @return ordered collection
     * @since 11.2
     */
    @ExpressionFunction("NumberSequence")
    public static List<Number> numberSequence(@NotNull Number from, @NotNull Number to, @NotNull Number step) {
        final BigDecimal start = normalized(from);
        final BigDecimal end = normalized(to);
        final BigDecimal bdStep = normalized(step);

        validateDirection(start, end, bdStep);

        return sequence(start, end, bdStep);
    }

    /**
     * Generates a sequence of numbers by adding 1 to starting number until it is less than
     * or equals to ending number. Returns a collection of numbers in order.
     * Parameters cannot be {@code null}.
     *
     * @param from sequence starting number
     * @param to   sequence end number
     * @return ordered collection
     * @since 11.2
     */
    @ExpressionFunction("NumberSequence")
    public static List<Number> numberSequence(@NotNull Number from, @NotNull Number to) {
        BigDecimal start = normalized(from);
        BigDecimal end = normalized(to);
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
            throw new IllegalStateException(
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

    private static Number asNumber(Object value) {
        if(value instanceof MonetaryAmount) {
            return MoneyFunctions.fromMoney((MonetaryAmount) value);
        }
        if(value instanceof Number) {
            return (Number) value;
        }
        var valueDescription = "";
        if (value == null) {
            valueDescription = "null";
        } else {
            valueDescription = "value ('" + value + " of type " + value.getClass().getName() + "')";
        }
        throw new IllegalStateException(
                "Collection function can be invoked only with collection that contains 'Number' and/or 'Money' values, " +
                        "but it was invoked with " + valueDescription);
    }

}
