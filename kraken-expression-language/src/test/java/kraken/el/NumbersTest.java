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
package kraken.el;

import static kraken.el.math.Numbers.isValueInNumberSet;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

import kraken.el.math.Numbers;

/**
 * @author Mindaugas Ulevicius
 */
public class NumbersTest {

    @Test
    public void shouldNotRoundIfPrecisionIsDecimal64() {
        var n = new BigDecimal("1234567890.123456");
        var normalized = Numbers.normalized(n);
        assertThat(normalized.toPlainString(), equalTo("1234567890.123456"));
        assertThat(normalized, is(n));
    }

    @Test
    public void shouldRoundIfPrecisionLargerThanDecimal64() {
        var n = new BigDecimal("1234567890.1234567");
        assertThat(n.toPlainString(), is("1234567890.1234567"));

        var normalized = Numbers.normalized(n);
        assertThat(normalized.toPlainString(), equalTo("1234567890.123457"));
        assertThat(normalized, not(is(n)));
    }

    @Test
    public void shouldCalculateIfNumberIsInNumberSet_NegativeMin_PositiveMax_Step() {
        // [-4, 4] with step 3 (values are -4, -1, 2)
        assertThatNumberSet(-4, 4, 3)
            .hasNumbers(-4, -1, 2)
            .doesNotHaveNumbers(-5, -3, -2, 0, 1, 3, new BigDecimal("3.569999"), 4, 5);
    }

    @Test
    public void shouldCalculateIfNumberIsInNumberSet_NegativeMin_NegativeMax_Step() {
        // [-40, -4] with step 25 (values are -40, -15)
        assertThatNumberSet(-40, -4, 25)
            .hasNumbers(-40, -15)
            .doesNotHaveNumbers(-10, 15, -4);
    }

    @Test
    public void shouldCalculateIfNumberIsInNumberSet_PositiveMin_PositiveMax_Step_FloatingPointNumbers() {
        // [1.515, 1.535] with step 0.01 (values are 1.515, 1.525, 1.535)
        assertThatNumberSet(new BigDecimal("1.515"), new BigDecimal("1.535"), new BigDecimal("0.01"))
            .hasNumbers(new BigDecimal("1.515"), new BigDecimal("1.525"), new BigDecimal("1.53500"))
            .doesNotHaveNumbers(new BigDecimal("1.5151"), new BigDecimal("1.53501"));
    }

    @Test
    public void shouldCalculateIfNumberIsInNumberSet_PositiveMin_PositiveMax_Step() {
        // [0, 15] with step 7.5 (0, 7.5, 15)
        assertThatNumberSet(0, 15, new BigDecimal("7.5"))
            .hasNumbers(0, 7.5, 15)
            .doesNotHaveNumbers(new BigDecimal("-7.5"), new BigDecimal("0.0001"), new BigDecimal("14.9998"));
    }

    @Test
    public void shouldCalculateIfNumberIsInNumberSet_NegativeMin_NullMax_Step() {
        // [-40, inf] with step 25 (values are -40, -15, 10, 35, ...)
        assertThatNumberSet(-40, null, 25)
            .hasNumbers(-40, -15, 10, 35)
            .doesNotHaveNumbers(-35, 25, -25);
    }

    @Test
    public void shouldCalculateIfNumberIsInNumberSet_NullMin_PositiveMax_Step() {
        // [-inf, 15] with step 7.5 (values are ..., -7.5, 0, 7.5, 15)
        assertThatNumberSet(null, 15, new BigDecimal("7.5"))
            .hasNumbers(new BigDecimal("-7.5"), 0, new BigDecimal("7.5"), 15)
            .doesNotHaveNumbers(14, 16, new BigDecimal("22.5"));
    }

    @Test
    public void shouldCalculateIfNumberIsInNumberSet_Min_Max_NullStep() {
        // [-10, 10]
        assertThatNumberSet(-10, 10, null)
            .hasNumbers(-10, new BigDecimal("2.113574"), 10)
            .doesNotHaveNumbers(new BigDecimal("-10.0001"), new BigDecimal("10.0001"));
    }

    @Test
    public void shouldCalculateIfNumberIsInNumberSet_Min_NullMax_NullStep() {
        // [-10, inf]
        assertThatNumberSet(-10, null, null)
            .hasNumbers(-10, new BigDecimal("2.113574"), 10, new BigDecimal("10.0001"))
            .doesNotHaveNumbers(new BigDecimal("-10.0001"));
    }

    @Test
    public void shouldCalculateIfNumberIsInNumberSet_NullMin_Max_NullStep() {
        // [-inf, 10]
        assertThatNumberSet(null, 10, null)
            .hasNumbers(new BigDecimal("-10.0001"), -10, new BigDecimal("2.113574"), 10)
            .doesNotHaveNumbers(new BigDecimal("10.0001"));
    }

    static class NumberSet {
        Number min;
        Number max;
        Number step;

        private NumberSet(Number min, Number max, Number step) {
            this.min = min;
            this.max = max;
            this.step = step;
        }

        NumberSet hasNumbers(Number... numbers) {
            for(Number number : numbers) {
                assertTrue(isValueInNumberSet(number, min, max, step));
            }
            return this;
        }

        NumberSet doesNotHaveNumbers(Number... numbers) {
            for(Number number : numbers) {
                assertFalse(isValueInNumberSet(number, min, max, step));
            }
            return this;
        }

    }

    static NumberSet assertThatNumberSet(Number min, Number max, Number step) {
        return new NumberSet(min, max, step);
    }
}


