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

package kraken.engine.sanity.check;

import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.Policy;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.*;
import static org.junit.Assert.assertThat;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;

/**
 * @author psurinin@eisgroup.com
 * @since 1.0.30
 */
public class EngineSanitySelfReferenceTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldReferenceSameContextAsRuleDefined() {
        Policy policy = new Policy();
        policy.setPolicyNumber("P1");
        final EntryPointResult result = engine.evaluate(policy, "SelfReference");

        assertThat(result, hasRuleResults(3));
        assertThat(result, hasNoValidationFailures());
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, matchesSnapshot());
    }
}
