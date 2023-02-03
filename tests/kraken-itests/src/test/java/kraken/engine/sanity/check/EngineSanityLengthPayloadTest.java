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
import kraken.runtime.engine.result.reducers.validation.ValidationResult;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.Party;
import kraken.utils.MockAutoPolicyBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

import static kraken.test.KrakenItestMatchers.matchesSnapshot;
import static io.github.jsonSnapshot.SnapshotMatcher.start;
import static io.github.jsonSnapshot.SnapshotMatcher.validateSnapshots;
import static kraken.testing.matchers.KrakenMatchers.hasRuleResults;
import static kraken.testing.matchers.KrakenMatchers.hasValidationFailures;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class EngineSanityLengthPayloadTest extends SanityEngineBaseTest {

    @BeforeClass
    public static void beforeAll() {
        start();
    }

    @AfterClass
    public static void afterAll() {
        validateSnapshots();
    }

    @Test
    public void shouldExecuteLengthAutoPolicyEntryPointWithValidData() {
        Policy policy = new MockAutoPolicyBuilder()
                .addParty(new Party(null, "Related", "1"))
                .addParty(new Party(null, "Not Related", "2"))
                .build();
        final EntryPointResult result = engine.evaluate(policy, "LengthAutoPolicy");

        assertThat(result, matchesSnapshot());
        assertThat(result, hasRuleResults(2));
        assertThat(result, hasValidationFailures(0));
    }

    @Test
    public void shouldExecuteLengthAutoPolicyEntryPointWithEmptyData() {
        Policy policy = new MockAutoPolicyBuilder()
                .addParty(new Party("1"))
                .addParty(new Party("2"))
                .build();
        final EntryPointResult result = engine.evaluate(policy, "LengthAutoPolicy");

        assertThat(result, matchesSnapshot());
        assertThat(result, hasRuleResults(2));
        assertThat(result, hasValidationFailures(0));
    }

    @Test
    public void shouldExecuteLengthAutoPolicyEntryPointWithNotValidData() {
        Policy policy = new MockAutoPolicyBuilder()
                .addParty(new Party(null, "Related to Primary Insured", "1"))
                .addParty(new Party(null, "Not Related to Primary Insured", "2"))
                .build();
        final EntryPointResult result = engine.evaluate(policy, "LengthAutoPolicy");
        final List<ValidationResult> errorResults = validationStatusReducer.reduce(result).getErrorResults();

        assertThat(result, matchesSnapshot());
        assertThat(result, hasRuleResults(2));
        assertThat(result, hasValidationFailures(2));
        assertThat(errorResults.get(0).getRuleName(), is("R0149A"));
        assertThat(errorResults.get(0).getContextFieldInfo().getContextName(), is("Party"));
    }

    @Test
    public void shouldExecuteLengthAutoPolicyEntryPointWithEqualLengthData() {
        Policy policy = new MockAutoPolicyBuilder()
                .addParty(new Party(null, "Relationn to Primary", "1"))
                .addParty(new Party(null, "Related or related??", "2"))
                .build();
        final EntryPointResult result = engine.evaluate(policy, "LengthAutoPolicy");

        assertThat(result, matchesSnapshot());
        assertThat(result, hasRuleResults(2));
        assertThat(result, hasValidationFailures(0));
    }
}
