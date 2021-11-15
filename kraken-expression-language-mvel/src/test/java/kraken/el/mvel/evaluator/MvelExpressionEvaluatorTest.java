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
package kraken.el.mvel.evaluator;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import kraken.el.ExpressionEvaluationException;
import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * @author mulevicius
 */
public class MvelExpressionEvaluatorTest {

    private MvelExpressionEvaluator evaluator;

    @Before
    public void setUp() throws Exception {
        evaluator = new MvelExpressionEvaluator();
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldThrowWhenEvaluatingMissingPropertyInObject() {
        evaluator.evaluate("property", new Object(), Collections.emptyMap());
    }

    @Test
    public void shouldNotThrowWhenEvaluatingMissingPropertyInMap() {
        Object result = evaluator.evaluate("property", Collections.emptyMap(), Collections.emptyMap());

        assertThat(result, nullValue());
    }

    @Test
    public void shouldDoModulus() {
        assertThat(eval("_mod(10.1B, 3)"), comparesEqualTo(number("1.1")));
        assertThat(eval("_mod(840.7B, 0.1B)"), comparesEqualTo(number("0.0")));
        assertThat(eval("_mod(-1.0B, 0.3B)"), comparesEqualTo(number("-0.1")));
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldDoModulusThrowWhenNotANumber() {
        eval("_mod(1, null)");
    }

    @Test
    public void shouldDoSubtraction() {
        assertThat(eval("_sub(840.7B, 0.11B)"), comparesEqualTo(number("840.59")));
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldDoSubtractionThrowWhenNotANumber() {
        eval("_sub(1, null)");
    }

    @Test
    public void shouldDoAddition() {
        assertThat(eval("_add(840.7B, 0.11B)"), comparesEqualTo(number("840.81")));
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldDoAdditionThrowWhenNotANumber() {
        eval("_add(1, null)");
    }

    @Test
    public void shouldDoMultiplication() {
        assertThat(eval("_mult(840.7B, 0.1B)"), comparesEqualTo(number("84.07")));
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldDoMultiplicationThrowWhenNotANumber() {
        eval("_mult(1, null)");
    }

    @Test
    public void shouldDoDivision() {
        assertThat(eval("_div(840.7B, 0.1B)"), comparesEqualTo(number("8407")));
        assertThat(eval("_div(2, 3)"), comparesEqualTo(number(   "0.6666666666666667")));
        assertThat(eval("_div(1, 3)"), comparesEqualTo(number(   "0.3333333333333333")));
        assertThat(eval("_div(200, 3)"), comparesEqualTo(number("66.66666666666667")));
        assertThat(eval("_div(100, 3)"), comparesEqualTo(number("33.33333333333333")));
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldDoDivisionThrowWhenNotANumber() {
        eval("_div(1, null)");
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldDoDivisionThrowWhenDivisionByZero() {
        eval("_div(1, 0)");
    }

    @Test
    public void shouldDoPower() {
        assertThat(eval("_pow(2.22B, 2)"), comparesEqualTo(number("4.9284")));
        assertThat(eval("_pow(4, -2)"), comparesEqualTo(number("0.0625")));
        assertThat(eval("_pow(4, -2.1)"), comparesEqualTo(number("0.0625")));
        assertThat(eval("_pow(2.0B, 3)"), comparesEqualTo(number("8.0")));
    }

    @Test(expected = ExpressionEvaluationException.class)
    public void shouldDoPowerThrowWhenNotANumber() {
        eval("_pow(1, null)");
    }

    private BigDecimal number(String number) {
        return new BigDecimal(number);
    }

    private BigDecimal eval(String expression) {
        return (BigDecimal) evaluator.evaluate(expression, Collections.emptyMap(), Collections.emptyMap());
    }
}
