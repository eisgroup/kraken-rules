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
package kraken.el.math;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.money.MonetaryAmount;

/**
 * A collection of utils for doing Math on Number in consistent way
 *
 * @author mulevicius
 */
public class Numbers {

    // IEEE 754R Decimal64 (decimalPlaces: 16, RoundingMode: HALF_EVEN)
    // cannot use Decimal128 because javascript supports only 64 bit decimals
    public static final MathContext DEFAULT_MATH_CONTEXT = MathContext.DECIMAL64;

    public static Number modulus(Number first, Number second) {
        return normalized(first).remainder(normalized(second), DEFAULT_MATH_CONTEXT);
    }

    public static Number power(Number number, Number p) {
        return normalized(number).pow(p.intValue(), DEFAULT_MATH_CONTEXT);
    }

    public static Number sqrt(Number number) {
        return normalized(number).sqrt(DEFAULT_MATH_CONTEXT);
    }

    public static Number divide(Number first, Number second) {
        return normalized(first).divide(normalized(second), DEFAULT_MATH_CONTEXT);
    }

    public static Number multiply(Number first, Number second) {
        return normalized(first).multiply(normalized(second), DEFAULT_MATH_CONTEXT);
    }

    public static Number add(Number first, Number second) {
        return normalized(first).add(normalized(second), DEFAULT_MATH_CONTEXT);
    }

    public static Number subtract(Number first, Number second) {
        return normalized(first).subtract(normalized(second), DEFAULT_MATH_CONTEXT);
    }

    public static Number floor(Number number) {
        return normalized(number).setScale(0, RoundingMode.FLOOR);
    }

    public static Number ceil(Number number) {
        return normalized(number).setScale(0, RoundingMode.CEILING);
    }

    public static Number abs(Number number) {
        return normalized(number).abs();
    }

    public static Number sign(Number number) {
        return normalized(normalized(number).signum());
    }

    public static Number round(Number number) {
        return normalized(number).setScale(0, RoundingMode.HALF_UP);
    }

    public static Number roundEven(Number number) {
        return normalized(number).setScale(0, RoundingMode.HALF_EVEN);
    }

    public static Number round(Number number, Integer scale) {
        return normalized(number).setScale(scale, RoundingMode.HALF_UP).stripTrailingZeros();
    }

    public static Number roundEven(Number number, Integer scale) {
        return normalized(number).setScale(scale, RoundingMode.HALF_EVEN).stripTrailingZeros();
    }

    public static Number max(Number first, Number second) {
        return max(List.of(first, second));
    }

    public static Number max(Collection<Number> numbers) {
        return numbers.stream().max(Numbers::compareTo).orElse(null);
    }

    public static Number min(Number first, Number second) {
        return min(List.of(first, second));
    }

    public static Number min(Collection<Number> numbers) {
        return numbers.stream().min(Numbers::compareTo).orElse(null);
    }

    public static Number avg(Collection<Number> numbers) {
        return numbers.stream().reduce(Numbers::add).map(n -> Numbers.divide(n, numbers.size())).orElse(null);
    }

    public static Number sum(Collection<Number> numbers) {
        return numbers.stream().reduce(Numbers::add).orElse(null);
    }

    public static String toString(Number number) {
        return normalized(number).stripTrailingZeros().toPlainString();
    }

    public static boolean isValueInNumberSet(@Nonnull Number valueNum,
                                             @Nullable Number minNum,
                                             @Nullable Number maxNum,
                                             @Nullable Number stepNum) {
        var value = Numbers.normalized(valueNum);
        var min = minNum != null ? Numbers.normalized(minNum) : null;
        var max = maxNum != null ? Numbers.normalized(maxNum) : null;
        var step = stepNum != null ? Numbers.normalized(stepNum) : null;
        if(min != null && value.compareTo(min) < 0) {
            return false;
        }
        if(max != null && value.compareTo(max) > 0) {
            return false;
        }
        if(step != null) {
            var shiftedValue = value;
            if(min != null) {
                shiftedValue = shiftedValue.subtract(min);
            } else if(max != null) {
                shiftedValue = shiftedValue.subtract(max);
            }
            return shiftedValue.remainder(step).compareTo(BigDecimal.ZERO) == 0;
        }
        return true;
    }

    public static BigDecimal normalized(Number number) {
        if(number instanceof BigDecimal) {
            if(((BigDecimal) number).precision() > DEFAULT_MATH_CONTEXT.getPrecision()) {
                return new BigDecimal(((BigDecimal) number).toPlainString(), DEFAULT_MATH_CONTEXT);
            }
            return (BigDecimal) number;
        }
        return new BigDecimal(number.toString(), DEFAULT_MATH_CONTEXT);
    }

    public static boolean areEqual(Number first, Number second) {
        return compareTo(first, second) == 0;
    }

    public static int compareTo(Number first, Number second) {
        return normalized(first).compareTo(normalized(second));
    }

    public static BigDecimal fromMoney(MonetaryAmount monetaryAmount) {
        return monetaryAmount != null && monetaryAmount.getNumber() != null
            ? monetaryAmount.getNumber().numberValue(BigDecimal.class)
            : null;
    }
}
