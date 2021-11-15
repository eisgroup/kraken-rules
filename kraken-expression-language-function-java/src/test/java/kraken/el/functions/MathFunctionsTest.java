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
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.javamoney.moneta.Money;
import org.junit.Test;

import static kraken.el.functions.MathFunctions.numberSequence;
import static kraken.el.functions.MathFunctions.round;
import static kraken.el.functions.MathFunctions.roundEven;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * @author mulevicius
 */
public class MathFunctionsTest {

    @Test
    public void shouldFindMinValueFromCollection() {
        Collection collection = List.of(
                BigInteger.valueOf(958),
                1.99,
                Money.of(2.01, "USD"),
                2,
                2l
        );
        Number result = MathFunctions.min(collection);

        assertThat(result, equalTo(1.99));
    }

    @Test
    public void minFunctionShouldReturnNullWhenCollectionsIsEmpty() {
        Number result = MathFunctions.min(Collections.EMPTY_LIST);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void minFunctionShouldReturnNullWhenCollectionsIsNull() {
        Number result = MathFunctions.min(null);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void shouldFindMaxValueFromCollection() {
        Collection collection = List.of(
                1.79,
                Money.of(32.787, "USD"),
                BigDecimal.valueOf(1.76),
                15,
                BigInteger.valueOf(2)
        );
        Number result = MathFunctions.max(collection);

        assertThat(result, equalTo(BigDecimal.valueOf(32.787)));
    }

    @Test
    public void maxFunctionShouldReturnNullWhenCollectionsIsEmpty() {
        Number result = MathFunctions.max(Collections.EMPTY_LIST);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void maxFunctionShouldReturnNullWhenCollectionsIsNull() {
        Number result = MathFunctions.max(null);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void shouldFindAvgValueFromCollection() {
        Collection collection = List.of(2.01, 32.615, 1.69, 0.99);
        BigDecimal result = (BigDecimal) MathFunctions.avg(collection);

        assertThat(result, equalTo(BigDecimal.valueOf(9.32625)));
    }

    @Test
    public void shouldFindAvgValueFromCollectionWithDifferentTypes() {
        Collection collection = List.of(
                1.60,
                Money.of(5, "USD"),
                BigDecimal.valueOf(1.76),
                BigInteger.valueOf(2),
                BigDecimal.valueOf(77.91)
        );
        BigDecimal result = (BigDecimal) MathFunctions.avg(collection);

        assertThat(result, equalTo(BigDecimal.valueOf(17.654)));
    }

    @Test
    public void avgFunctionShouldReturnNullWhenCollectionsIsEmpty() {
        Number result = MathFunctions.avg(Collections.EMPTY_LIST);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void avgFunctionShouldReturnNullWhenCollectionsIsNull() {
        Number result = MathFunctions.avg(null);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void shouldFindSumFromCollectionOfBigDecimals() {
        Collection collection = List.of(
                BigDecimal.valueOf(9999.87),
                BigDecimal.valueOf(31.7),
                BigDecimal.valueOf(69.69),
                BigDecimal.valueOf(99.548)
        );
        BigDecimal result = (BigDecimal) MathFunctions.sum(collection);

        assertThat(result, equalTo(BigDecimal.valueOf(10200.808)));
    }

    @Test
    public void shouldFindSumFromCollectionOfDecimals() {
        Collection collection = List.of(2.01, 32.615, 1.69, 0.99);
        BigDecimal result = (BigDecimal) MathFunctions.sum(collection);

        assertThat(result, equalTo(BigDecimal.valueOf(37.305)));
    }

    @Test
    public void shouldFindSumFromCollectionWithMoney() {
        Collection collection = List.of(
                Money.of(50.879, "USD"),
                Money.of(0.658, "USD"),
                32.615,
                1.69,
                0.99
        );
        BigDecimal result = (BigDecimal) MathFunctions.sum(collection);

        assertThat(result, equalTo(BigDecimal.valueOf(86.832)));
    }

    @Test
    public void sumFunctionShouldReturnNullWhenCollectionsIsEmpty() {
        Number result = MathFunctions.sum(Collections.EMPTY_LIST);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void sumFunctionShouldReturnNullWhenCollectionsIsNull() {
        Number result = MathFunctions.sum(null);

        assertThat(result, is(nullValue()));
    }

    @Test
    public void shouldFindSumFromCollectionWithDifferentTypes() {
        Collection collection = List.of(
                1.60, 5,
                BigDecimal.valueOf(1.76),
                BigInteger.valueOf(2),
                BigDecimal.valueOf(77.91)
        );
        BigDecimal result = (BigDecimal) MathFunctions.sum(collection);

        assertThat((BigDecimal) result, comparesEqualTo(BigDecimal.valueOf(88.27)));
    }

    @Test
    public void shouldRound() {
        assertThat((BigDecimal) round(number("1")), comparesEqualTo(number("1")));
        assertThat((BigDecimal) round(number("1.5")), comparesEqualTo(number("2")));
        assertThat((BigDecimal) round(number("2.5")), comparesEqualTo(number("3")));
        assertThat((BigDecimal) round(number("-1")), comparesEqualTo(number("-1")));
        assertThat((BigDecimal) round(number("-1.5")), comparesEqualTo(number("-2")));
        assertThat((BigDecimal) round(number("-2.5")), comparesEqualTo(number("-3")));

        assertThat((BigDecimal) roundEven(number("1")), comparesEqualTo(number("1")));
        assertThat((BigDecimal) roundEven(number("1.5")), comparesEqualTo(number("2")));
        assertThat((BigDecimal) roundEven(number("2.5")), comparesEqualTo(number("2")));
        assertThat((BigDecimal) roundEven(number("-1")), comparesEqualTo(number("-1")));
        assertThat((BigDecimal) roundEven(number("-1.5")), comparesEqualTo(number("-2")));
        assertThat((BigDecimal) roundEven(number("-2.5")), comparesEqualTo(number("-2")));

        assertThat((BigDecimal) round(number("1"), 2), comparesEqualTo(number("1")));
        assertThat((BigDecimal) round(number("1.555"), 2), comparesEqualTo(number("1.56")));
        assertThat((BigDecimal) round(number("100.565"), 2), comparesEqualTo(number("100.57")));
        assertThat((BigDecimal) round(number("-1"), 2), comparesEqualTo(number("-1")));
        assertThat((BigDecimal) round(number("-1.555"), 2), comparesEqualTo(number("-1.56")));
        assertThat((BigDecimal) round(number("-100.565"), 2), comparesEqualTo(number("-100.57")));

        assertThat((BigDecimal) roundEven(number("1"), 2), comparesEqualTo(number("1")));
        assertThat((BigDecimal) roundEven(number("1.555"), 2), comparesEqualTo(number("1.56")));
        assertThat((BigDecimal) roundEven(number("100.565"), 2), comparesEqualTo(number("100.56")));
        assertThat((BigDecimal) roundEven(number("-1"), 2), comparesEqualTo(number("-1")));
        assertThat((BigDecimal) roundEven(number("-1.555"), 2), comparesEqualTo(number("-1.56")));
        assertThat((BigDecimal) roundEven(number("-100.565"), 2), comparesEqualTo(number("-100.56")));
    }

    @Test
    public void shouldFloor() {
        assertThat((BigDecimal) MathFunctions.floor(number("1")), comparesEqualTo(number("1")));
        assertThat((BigDecimal) MathFunctions.floor(number("1.9")), comparesEqualTo(number("1")));
        assertThat((BigDecimal) MathFunctions.floor(number("-1")), comparesEqualTo(number("-1")));
        assertThat((BigDecimal) MathFunctions.floor(number("-1.1")), comparesEqualTo(number("-2")));
    }

    @Test
    public void shouldCeil() {
        assertThat((BigDecimal) MathFunctions.ceil(number("1")), comparesEqualTo(number("1")));
        assertThat((BigDecimal) MathFunctions.ceil(number("1.1")), comparesEqualTo(number("2")));
        assertThat((BigDecimal) MathFunctions.ceil(number("-1")), comparesEqualTo(number("-1")));
        assertThat((BigDecimal) MathFunctions.ceil(number("-1.9")), comparesEqualTo(number("-1")));
    }

    @Test
    public void shouldAbs() {
        assertThat((BigDecimal) MathFunctions.abs(number("1")), comparesEqualTo(number("1")));
        assertThat((BigDecimal) MathFunctions.abs(number("-1.1")), comparesEqualTo(number("1.1")));
    }

    @Test
    public void shouldSign() {
        assertThat((BigDecimal) MathFunctions.sign(number("1")), comparesEqualTo(number("1")));
        assertThat((BigDecimal) MathFunctions.sign(number("0")), comparesEqualTo(number("0")));
        assertThat((BigDecimal) MathFunctions.sign(number("-1.1")), comparesEqualTo(number("-1")));
    }

    @Test
    public void shouldSqrt() {
        assertThat((BigDecimal) MathFunctions.sqrt(number("1")), comparesEqualTo(number("1")));
        assertThat((BigDecimal) MathFunctions.sqrt(number("9")), comparesEqualTo(number("3")));
        assertThat((BigDecimal) MathFunctions.sqrt(number("15.29344444444444")), comparesEqualTo(number("3.910683373074895")));
    }

    @Test
    public void shouldReturnMaxOfTwoNumbers() {
        assertThat(
                (BigDecimal) MathFunctions.max(number("0"), number("1")),
                comparesEqualTo(number("1"))
        );
        assertThat(
                (BigDecimal) MathFunctions.max(number("-1.1"), number("-10")),
                comparesEqualTo(number("-1.1"))
        );
    }

    @Test
    public void shouldReturnMinOfTwoNumbers() {
        assertThat(
                (BigDecimal) MathFunctions.min(number("0"), number("1")),
                comparesEqualTo(number("0"))
        );
        assertThat(
                (BigDecimal) MathFunctions.min(number("-1.1"), number("-10")),
                comparesEqualTo(number("-10"))
        );
    }
    @Test
    public void shouldGenerateSequenceWithDefaultStep() {
        assertThat(numberSequence(3, 0), contains(numbers(3, 2, 1, 0)));
        assertThat(numberSequence(0, 3), contains(numbers(0, 1, 2, 3)));
        assertThat(numberSequence(0, 0), contains(numbers(0)));
    }

    @Test
    public void shouldGenerateSequenceWithDefaultNumberTypes() {
        assertThat(numberSequence(1, 0), contains(numbers(1, 0)));
        assertThat(numberSequence(1L, 0L), contains(numbers(1, 0)));
        assertThat(numberSequence(number(1), number(0)), contains(numbers(1, 0)));
    }
    
    @Test
    public void shouldGenerateSequence() {
        assertThat(numberSequence(3, 0, -1), contains(numbers(3, 2, 1, 0)));
        assertThat(numberSequence(0, 3, 1), contains(numbers(0, 1, 2, 3)));
        assertThat(numberSequence(0, 3, 4), contains(numbers(0)));
        assertThat(
                numberSequence(-19.68, 23.872, 10.888),
                contains(numbers(-19.68, -8.792, 2.096, 12.984, 23.872))
        );
    }

    @Test
    public void shouldGenerateSequenceWithTypes() {
        assertThat(numberSequence(1, 0, -1), contains(numbers(1, 0)));
        assertThat(numberSequence(1L, 0L, -1L), contains(numbers(1, 0)));
        assertThat(numberSequence(number(1), number(0), number(-1)), contains(numbers(1, 0)));
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowOnStepIsInWrongDirectionUP() {
        numberSequence(10, 0, 1);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowOnStepIsInWrongDirectionDOWN() {
        numberSequence(-10, 0, -1);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldThrowOnStepIsZero() {
        numberSequence(1, 11, 0);
    }
    
    private BigDecimal number(Number number) {
        return number(number.toString());
    }

    private BigDecimal[] numbers(Number... number) {
        return Arrays.stream(number).map(n -> number(n)).collect(Collectors.toList()).toArray(new BigDecimal[] {});
    }

    private BigDecimal number(String number) {
        return new BigDecimal(number);
    }

}
