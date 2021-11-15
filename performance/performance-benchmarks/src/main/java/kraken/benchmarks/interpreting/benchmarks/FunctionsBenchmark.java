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
import java.util.HashMap;
import java.util.Map;

import kraken.benchmarks.interpreting.AbstractExpressionBenchmark;

/**
 * @author mulevicius
 */
public class FunctionsBenchmark extends AbstractExpressionBenchmark {

    private static final String expressionPart = "IsEmpty(IsBlank(NumberToString(Sign(Abs(numberProperty%s)))))";

    public static void main(String[] args) {
        new FunctionsBenchmark().log();
    }

    @Override
    public String[] getExpressions() {
        String expression = String.format(expressionPart, "");
        for(int i = 0; i < 10; i++) {
            expression = expression + " or " + String.format(expressionPart, i);
        }
        return new String[] {expression};
    }

    @Override
    public Object getContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("numberProperty", new BigDecimal("-1"));
        for(int i = 0; i < 10; i++) {
            context.put("numberProperty" + i, new BigDecimal("-" + i));
        }
        return context;
    }

}
