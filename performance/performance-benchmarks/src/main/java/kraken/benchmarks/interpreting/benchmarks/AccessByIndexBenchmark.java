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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import kraken.benchmarks.interpreting.AbstractExpressionBenchmark;

/**
 * @author mulevicius
 */
public class AccessByIndexBenchmark extends AbstractExpressionBenchmark {

    private static final int arraySize = 6;
    private static final String property = "arrayProperty";
    private static final String arrayTemplate = "[Count(%s)-1]";

    public static void main(String[] args) {
        new AccessByIndexBenchmark().log();
    }

    @Override
    public String[] getExpressions() {
        String expression = property;
        for(int i = 0; i < arraySize; i++) {
            String arrayTerm = i == 0 ? "[0]" : String.format(arrayTemplate, generateNestedArrayExpression(i));
            expression = expression + arrayTerm;
        }
        return new String[] {expression};
    }

    private String generateNestedArrayExpression(int size) {
        String expression = property;
        for(int i = 0; i < size; i++) {
            expression = expression + "[" + i + "]";
        }
        return expression;
    }

    @Override
    public Object getContext() {
        List<Object> array = new ArrayList<>();
        for(int i = 0; i < arraySize; i++) {
            array.add(generateNestedArray(arraySize));
        }
        return Map.of(property, array);
    }

    private List<Object> generateNestedArray(int currentDepth) {
        List<Object> array = new ArrayList<>();
        for(int i = 0; i < arraySize; i++) {
            List<Object> nestedArray = currentDepth == 0
                    ? IntStream.range(0, arraySize).boxed().collect(Collectors.toList())
                    : generateNestedArray(currentDepth-1);
            array.add(nestedArray);
        }
        return array;
    }

}
