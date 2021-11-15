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

import java.util.Map;

import kraken.benchmarks.interpreting.AbstractExpressionBenchmark;

/**
 * @author mulevicius
 */
public class NestedIterationBenchmark extends AbstractExpressionBenchmark {

    private String expression =
            "Sum(for i in {1,2,3,4,5,6,7,8,9} " +
                    "return i + Sum(for j in {10,11,12,13,14,15,16,17,18,19,20} " +
                    "return i + j + Sum(for k in {100,101,102,103,104,105,106,107,108,109} " +
                    "return i + j + k + property)))";

    public static void main(String[] args) {
        new NestedIterationBenchmark().log();
    }

    @Override
    public String[] getExpressions() {
        return new String[]{ expression };
    }

    @Override
    public Object getContext() {
        return Map.of("property", 1);
    }

}
