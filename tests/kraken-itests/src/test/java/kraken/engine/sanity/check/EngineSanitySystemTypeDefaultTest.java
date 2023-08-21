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

import static kraken.testing.matchers.KrakenMatchers.hasNoIgnoredRules;
import static kraken.testing.matchers.KrakenMatchers.hasNoValidationFailures;
import static kraken.testing.matchers.KrakenMatchers.hasRuleResults;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.SysDate;

/** @author psurinin */
public class EngineSanitySystemTypeDefaultTest extends SanityEngineBaseTest {

    @Test
    public void shouldEvaluateRules() {
        final Policy policy = new Policy();
        policy.setBackupSystemDate(new SysDate("2020-02-02T02:02:02"));

        final EntryPointResult result = engine.evaluate(policy, "on system context");

        assertThat(result, hasRuleResults(1));
        assertThat(result, hasNoValidationFailures());
        assertThat(result, hasNoIgnoredRules());
        assertThat(policy.getSystemDate(), is(policy.getBackupSystemDate()));
    }

}