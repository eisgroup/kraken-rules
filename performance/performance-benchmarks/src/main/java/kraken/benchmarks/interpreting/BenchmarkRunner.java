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

import java.util.concurrent.TimeUnit;

import kraken.benchmarks.interpreting.benchmarks.*;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Benchmarks runtime expression evaluation by comparing MVEL evaluator with Interpreting evaluator
 *
 * Benchmark                                  Mode   Cnt   Score    Error  Units
 * AccessByIndexBenchmark.interpreting        avgt    5    0.002 ±  0.001  ms/op
 * AccessByIndexBenchmark.mvel                avgt    5    0.002 ±  0.001  ms/op
 * BooleanOperatorsBenchmark.interpreting     avgt    5    0.014 ±  0.009  ms/op
 * BooleanOperatorsBenchmark.mvel             avgt    5    0.014 ±  0.002  ms/op
 * ComparisonOfDatesBenchmark.interpreting    avgt    5    0.025 ±  0.003  ms/op
 * ComparisonOfDatesBenchmark.mvel            avgt    5    0.045 ±  0.005  ms/op
 * ComparisonOfNumbersBenchmark.interpreting  avgt    5    0.027 ±  0.002  ms/op
 * ComparisonOfNumbersBenchmark.mvel          avgt    5    0.032 ±  0.002  ms/op
 * FilteringBenchmark.interpreting            avgt    5  138.768 ±  8.126  ms/op
 * FilteringBenchmark.mvel                    avgt    5  206.279 ± 22.836  ms/op
 * FunctionsBenchmark.interpreting            avgt    5    0.010 ±  0.001  ms/op
 * FunctionsBenchmark.mvel                    avgt    5    0.010 ±  0.001  ms/op
 * MathOperatorsBenchmark.interpreting        avgt    5    0.079 ±  0.005  ms/op
 * MathOperatorsBenchmark.mvel                avgt    5    0.075 ±  0.003  ms/op
 * NestedIterationBenchmark.interpreting      avgt    5    0.605 ±  0.053  ms/op
 * NestedIterationBenchmark.mvel              avgt    5    0.821 ±  0.046  ms/op
 * PathsBenchmark.interpreting                avgt    5    0.010 ±  0.001  ms/op
 * PathsBenchmark.mvel                        avgt    5    0.001 ±  0.001  ms/op
 *
 *
 * @author mulevicius
 */
public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                /*
                .include(NestedIterationBenchmark.class.getSimpleName())
                .include(PathsBenchmark.class.getSimpleName())
                .include(MathOperatorsBenchmark.class.getSimpleName())
                .include(BooleanOperatorsBenchmark.class.getSimpleName())
                .include(ComparisonOfNumbersBenchmark.class.getSimpleName())
                .include(ComparisonOfDatesBenchmark.class.getSimpleName())
                .include(AccessByIndexBenchmark.class.getSimpleName())
                .include(FunctionsBenchmark.class.getSimpleName())
                .include(FilteringBenchmark.class.getSimpleName())

                 */
                .include(FunctionsInlineCollectionBenchmark.class.getSimpleName())
                .mode(Mode.AverageTime)
                .timeUnit(TimeUnit.MILLISECONDS)
                .forks(1)
                .jvmArgs("-Xms2G", "-Xmx2G")
                .warmupIterations(3)
                .measurementIterations(5)
                .build();

        new Runner(opt).run();
    }

}
