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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import kraken.benchmarks.interpreting.AbstractExpressionBenchmark;

/**
 * @author mulevicius
 */
public class FunctionsInlineCollectionBenchmark extends AbstractExpressionBenchmark {

    private static final String[] firstSet = "abcdefghijklmn".split("");

    private static final String[] secondSet = "mnopqrstuvwxyz".split("");

    private static final String expressionPart = ""+
            "every item " +
            "in Intersection(FirstSet, SecondSet) " +
            "satisfies " +
            "      item in Union(FirstSet, SecondSet) " +
            "  and item in Union(SecondSet, FirstSet) " +
            "  and item in Union(FirstSet, SecondSet) " +
            "  and item in Union(SecondSet, FirstSet) " +
            "  and item in Union(FirstSet, SecondSet) " +
            "  and item in Union(SecondSet, FirstSet) " +
            "  and item in Union(FirstSet, SecondSet) " +
            "  and item in Union(SecondSet, SecondSet) " +
            "  and item in Union(FirstSet, FirstSet) ";

    public static void main(String[] args) {
        new FunctionsInlineCollectionBenchmark().log();
    }

    @Override
    public String[] getExpressions() {
        return new String[] {expressionPart};
    }

    @Override
    public Object getContext() {
        Map<String, Object> context = new HashMap<>();
        context.put("FirstSet", Set.of(firstSet));
        context.put("SecondSet", Set.of(secondSet));
        return context;
    }

}
