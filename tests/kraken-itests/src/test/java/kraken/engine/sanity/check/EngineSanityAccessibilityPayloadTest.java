
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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static kraken.test.KrakenItestMatchers.matchesSnapshot;

public final class EngineSanityAccessibilityPayloadTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldExecuteAccessibilityAutoPolicyEntryPoint() {
        EntryPointResult result = engine.evaluate(getDataObject(), "AccessibilityAutoPolicy");

        assertThat(result, matchesSnapshot());
        assertThat(result, hasRuleResults(4));
        assertThat(result, hasDisabledFields(4));
    }
}
