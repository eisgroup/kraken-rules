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
package kraken.benchmarks.interpreting;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import kraken.el.KrakenKel;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.interpreter.evaluator.InterpretingExpressionEvaluator;
import kraken.el.mvel.evaluator.MvelExpressionEvaluator;
import kraken.el.mvel.translator.MvelAstTranslator;
import kraken.el.scope.Scope;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

/**
 * @author mulevicius
 */
@State(org.openjdk.jmh.annotations.Scope.Benchmark)
public abstract class AbstractExpressionBenchmark {

    private String[] expressions;
    private String[] mvelExpressions;
    private Object context;

    private InterpretingExpressionEvaluator interpretingEvaluator = new InterpretingExpressionEvaluator(KrakenKel.CONFIGURATION);

    private MvelExpressionEvaluator mvelEvaluator = new MvelExpressionEvaluator();

    @Setup
    public void setup() {
        MvelAstTranslator mvelAstTranslator = new MvelAstTranslator(KrakenKel.CONFIGURATION);

        this.expressions = getExpressions();
        this.context = getContext();

        this.mvelExpressions = new String[expressions.length];
        for (int i = 0; i < expressions.length; i++) {
            this.mvelExpressions[i] = mvelAstTranslator.translate(
                    AstBuilder.from(expressions[i], getContextScope())
            );
        }
    }

    @Benchmark
    public void mvel(Blackhole bh) {
        Map<String, Object> empty = Collections.emptyMap();
        for (int i = 0; i < mvelExpressions.length; i++) {
            Object result = mvelEvaluator.evaluate(mvelExpressions[i], context, empty);
            bh.consume(result);
        }
    }

    @Benchmark
    public void interpreting(Blackhole bh) {
        Map<String, Object> empty = Collections.emptyMap();
        for(int i = 0; i < expressions.length; i++) {
            Object result = interpretingEvaluator.evaluate(expressions[i], context, empty);
            bh.consume(result);
        }
    }

    public void log() {
        setup();

        System.out.println("DSL Expressions:");
        System.out.println(Arrays.stream(getExpressions()).collect(Collectors.joining(System.lineSeparator())));
        System.out.println("MVEL Translated Expressions:");
        System.out.println(Arrays.stream(mvelExpressions).collect(Collectors.joining(System.lineSeparator())));
        System.out.println("Evaluation Context:");
        System.out.println(getContext());
    }

    public abstract String[] getExpressions();

    public abstract Object getContext();

    public Scope getContextScope() {
        return Scope.dynamic();
    }

}
