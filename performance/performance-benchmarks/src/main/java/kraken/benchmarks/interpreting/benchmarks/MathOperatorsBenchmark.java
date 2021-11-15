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
package kraken.benchmarks.interpreting.benchmarks;

import java.math.BigDecimal;
import java.util.Map;

import kraken.benchmarks.interpreting.AbstractExpressionBenchmark;

/**
 * @author mulevicius
 */
public class MathOperatorsBenchmark extends AbstractExpressionBenchmark {

    public static void main(String[] args) {
        new MathOperatorsBenchmark().log();
    }

    @Override
    public String[] getExpressions() {
        String[] operators = {"%", "-", "/", "+", "*", "**"};
        String expression = "11";
        for(int i = 0; i < operators.length * 100; i++) {
            int op = i % operators.length;
            expression = expression + " " + operators[op] + " number" + op;
        }
        return new String[] {expression};
    }

    @Override
    public Object getContext() {
        return Map.of(
                "number0", new BigDecimal("1.0"),
                "number1", 2L,
                "number2", new BigDecimal("3.0"),
                "number3", 4,
                "number4", new BigDecimal("5.0"),
                "number5", new BigDecimal("6.0")
        );
    }

}
