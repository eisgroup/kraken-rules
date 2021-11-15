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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import kraken.benchmarks.interpreting.AbstractExpressionBenchmark;

/**
 * @author mulevicius
 */
public class ComparisonOfDatesBenchmark extends AbstractExpressionBenchmark {

    public static void main(String[] args) {
        new ComparisonOfDatesBenchmark().log();
    }

    @Override
    public String[] getExpressions() {
        String expression = "(big > small) and (small < big)";
        for(int i = 0; i < 100; i++) {
            expression = expression + String.format(" and (big%s > small%s) and (small%s < big%s)", i, i, i, i);
        }
        return new String[] {expression};
    }

    @Override
    public Object getContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("big", LocalDateTime.of(2000, 1, 1, 8, 0, 0));
        context.put("small", LocalDateTime.of(1990, 1, 1, 8, 0, 0));
        for(int i = 0; i < 100; i++) {
            context.put("big" + i, LocalDateTime.of(2000 + i, 1, 1, 8, 0, 0));
            context.put("small" + i, LocalDateTime.of(1990 + i, 1, 1, 8, 0, 0));
        }
        return context;
    }

}
