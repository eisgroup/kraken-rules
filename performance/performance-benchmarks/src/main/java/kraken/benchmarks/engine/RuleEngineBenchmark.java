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

import java.util.Map;

import kraken.runtime.EvaluationConfig;
import kraken.runtime.RuleEngine;
import kraken.runtime.engine.EntryPointResult;
import kraken.utils.Namespaces;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import static kraken.testproduct.TestProduct.NAMESPACE;

/**
 * @author psurinin@eisgroup.com
 * @since 1.0.29
 */
@State(org.openjdk.jmh.annotations.Scope.Benchmark)
public class RuleEngineBenchmark {

    private RuleEngine engine;
    private EvaluationConfig config;

    @Setup
    public void setup() {
        this.engine = Engines.fromResourceDirectory(NAMESPACE, "database/gap/");
        this.config = new EvaluationConfig(
                Map.of(
                        "dimensions",
                        Map.of(
                                "plan",
                                "pizza"
                        ),
                        "additional",
                        Map.of(
                                "package",
                                "Pizza"
                        )
                ),
                "USD"
        );
    }

    @Benchmark
    public void allRules_1riskItem(Blackhole bh) {
        EntryPointResult result = eval(1, "All");
        bh.consume(result);
    }

    @Benchmark
    public void allRules_10riskItems(Blackhole bh) {
        EntryPointResult result = eval(10, "All");
        bh.consume(result);
    }

    @Benchmark
    public void _3Rules_1_riskItem(Blackhole bh) {
        EntryPointResult result = eval(1, "AssertionCarCoverage");
        bh.consume(result);
    }

    @Benchmark
    public void _3Rules_10riskItems(Blackhole bh) {
        EntryPointResult result = eval(10, "AssertionCarCoverage");
        bh.consume(result);
    }

    public EntryPointResult eval(int childrenMultiplier, String entryPointName) {
        return engine.evaluate(PolicyBuilder.getPolicy(childrenMultiplier), withNamespace(entryPointName), config);
    }

    private String withNamespace(String entryPointName) {
        return Namespaces.toFullName(NAMESPACE, entryPointName);
    }

}
