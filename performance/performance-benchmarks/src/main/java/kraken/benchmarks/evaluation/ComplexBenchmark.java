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
package kraken.benchmarks.evaluation;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import kraken.el.ast.AstType;
import kraken.el.mvel.evaluator.MvelExpressionEvaluator;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * @author mulevicius
 */
@State(org.openjdk.jmh.annotations.Scope.Benchmark)
public class ComplexBenchmark {

    private int DEFAULT_UNIQUE_EXPRESSIONS = 10000;

    private String[] expressions;
    private Map<String, Object> context;

    private MvelExpressionEvaluator expressionEvaluator = new MvelExpressionEvaluator();

    private AcceleratedExpressionEvaluator acceleratedExpressionEvaluator = new AcceleratedExpressionEvaluator(new MvelExpressionEvaluator());

    @Setup
    public void setup() {
        this.expressions = new String[DEFAULT_UNIQUE_EXPRESSIONS];
        this.context = new HashMap<>();
        for (int i = 0; i < DEFAULT_UNIQUE_EXPRESSIONS; i++) {
            String expression = "property" + i + " + 5";
            this.expressions[i] = expression;
            this.context.put("property" + i, i);

            AcceleratedExpressionEvaluator.expressionCache.put(expression,
                    new ExpressionMetadata(expression, AstType.COMPLEX, null));
        }
    }

    @Benchmark
    public void complexRegular(Blackhole bh) {
        Map<String, Object> empty = Collections.emptyMap();
        for(int i = 0; i < expressions.length; i++) {
            Object result = expressionEvaluator.evaluate(expressions[i], context, empty);
            bh.consume(result);
        }
    }

    @Benchmark
    public void complexAccelerated(Blackhole bh) {
        Map<String, Object> empty = Collections.emptyMap();
        for(int i = 0; i < expressions.length; i++) {
            Object result = acceleratedExpressionEvaluator.evaluate(expressions[i], context, empty);
            bh.consume(result);
        }
    }

}
