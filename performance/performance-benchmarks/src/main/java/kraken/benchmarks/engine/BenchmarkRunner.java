/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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
package kraken.benchmarks.engine;

import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * @since 1.1.0
 * Benchmark                             Mode  Cnt   Score   Error  Units
 * ResourceEngineBenchmark._3Rules_10riskItems   avgt    5  1.459 ± 0.075  ms/op
 * ResourceEngineBenchmark._3Rules_1_riskItem    avgt    5  0.161 ± 0.003  ms/op
 * ResourceEngineBenchmark.allRules_10riskItems  avgt    5  9.875 ± 0.699  ms/op
 * ResourceEngineBenchmark.allRules_1riskItem    avgt    5  1.502 ± 0.038  ms/op
 *
 * @author psurinin@eisgroup.com
 */
public class BenchmarkRunner {

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(RuleEngineBenchmark.class.getSimpleName())
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
