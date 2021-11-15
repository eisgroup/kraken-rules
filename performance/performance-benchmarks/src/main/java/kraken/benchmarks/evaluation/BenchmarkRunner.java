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

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * Benchmarks runtime expression evaluation after expressions were translated.
 *
 * @since 1.1.0
 *
 * Benchmark                                    Mode  Cnt  Score   Error  Units
 * ComplexBenchmark.complexAccelerated          avgt    5  3.437 ± 0.436  ms/op
 * ComplexBenchmark.complexRegular              avgt    5  2.926 ± 0.157  ms/op
 * LiteralsBenchmark.literalsAccelerated        avgt    5  0.129 ± 0.001  ms/op
 * LiteralsBenchmark.literalsRegular            avgt    5  1.197 ± 0.085  ms/op
 * MapPropertyBenchmark.mapPropertyAccelerated  avgt    5  0.215 ± 0.047  ms/op
 * MapPropertyBenchmark.mapPropertyRegular      avgt    5  2.156 ± 1.606  ms/op
 * ReflectionPathBenchmark.pathReflective       avgt    5  0.429 ± 0.035  ms/op
 * ReflectionPathBenchmark.pathRegular          avgt    5  0.780 ± 0.070  ms/op
 *
 * @author mulevicius
 */
public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(ComplexBenchmark.class.getSimpleName())
                .include(LiteralsBenchmark.class.getSimpleName())
                .include(MapPropertyBenchmark.class.getSimpleName())
                .include(ReflectionPathBenchmark.class.getSimpleName())
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
