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

package kraken.engine.namespace;

import kraken.runtime.EvaluationConfig;
import kraken.runtime.engine.EntryPointResult;
import kraken.test.TestResources;
import kraken.testing.matchers.KrakenMatchers;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;
import static kraken.testing.matchers.KrakenMatchers.hasNoIgnoredRules;
import static org.junit.Assert.assertThat;

/**
 * @author psurinin
 */
public class NamespaceTestFoo extends NamespaceBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Override
    protected TestResources getResources() {
        return TestResources.create(TestResources.Info.NAMESPACE_FOO);
    }

    @Test
    public void shouldEvaluate_constraints() {
        final EntryPointResult result =
                engine.evaluate(getDataObject(), "constraints");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldEvaluate_dataGather() {
        final EntryPointResult result =
                engine.evaluate(getDataObject(), "dataGather");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldEvaluate_CCR1() {
        final EntryPointResult result =
                engine.evaluate(getDataObject(), "R-CCR-addressinfo-to-testpolicy");
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldEvaluate_ExpressionContext() {
        final EvaluationConfig config =
                new EvaluationConfig(Map.of("dimensions", Map.of("state", "CA")), "USD");
        final EntryPointResult result =
                engine.evaluate(
                        getDataObject(),
                        "ExpressionContext",
                        config
                );
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }

    @Test
    public void shouldEvaluateInSeveralNamespaces() {
        final EntryPointResult resultFoo =
                engine.evaluate(getDataObject(), "constraints");
        assertThat(resultFoo, hasNoIgnoredRules());
        assertThat(resultFoo, KrakenMatchers.hasValidationFailures(0));
    }
}
