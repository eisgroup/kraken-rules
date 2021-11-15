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
package kraken.engine.evaluation;

import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.Policy;
import org.junit.Test;

import static kraken.testing.matchers.KrakenMatchers.hasNoValidationFailures;
import static kraken.testing.matchers.KrakenMatchers.hasValidationFailures;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author rzukaitis
 * @author avasiliauskas
 * @since 1.0
 */
public final class RegExpRuleTest extends EvaluationEngineBaseTest{
    private static final String ENTRY_POINT_NAME = "ValidPolicyNumber";

    @Test
    public void ruleEngineShouldIdentifyThatFieldDoesNotSatisfyRegExpRule() {
        Policy policy = new Policy();
        policy.setPolicyNumber("incorrect");
        EntryPointResult entryPointResult = engine.evaluate(policy, ENTRY_POINT_NAME);

        assertThat(entryPointResult, hasValidationFailures(1));
    }

    @Test
    public void ruleEngineShouldIdentifyThatNullFieldSatisfiesRegExpRule() {
        Policy policy = new Policy();
        policy.setPolicyNumber(null);

        EntryPointResult entryPointResult = engine.evaluate(policy, ENTRY_POINT_NAME);
        assertThat(entryPointResult, hasNoValidationFailures());
    }

    @Test
    public void ruleEngineShouldIdentifyThatEmptyFieldSatisfiesRegExpRule() {
        Policy policy = new Policy();

        EntryPointResult entryPointResult = engine.evaluate(policy, ENTRY_POINT_NAME);
        assertThat(entryPointResult, hasNoValidationFailures());
    }

    @Test
    public void ruleEngineShouldIdentifyThatFieldSatisfiesRegExpRule() {
        Policy policy = new Policy();
        policy.setPolicyNumber("Q0001");

        EntryPointResult entryPointResult = engine.evaluate(policy, ENTRY_POINT_NAME);
        assertThat(entryPointResult, hasNoValidationFailures());

    }

    @Test
    public void multiLegnthRegExpShouldPassOnMinMaxLength() {
        String entryPointName = "ValidPolicyNumber2";

        Policy policy = new Policy();
        policy.setPolicyNumber("12345678901");

        EntryPointResult entryPointResult = engine.evaluate(policy, entryPointName);
        assertThat(entryPointResult, hasNoValidationFailures());

        policy.setPolicyNumber("1234567890123");

        entryPointResult = engine.evaluate(policy, entryPointName);
        assertThat(entryPointResult, hasNoValidationFailures());
    }

    @Test
    public void multiLegnthRegExpShouldFailOnMinMaxLength() {
        String testString = "1234512345";

        Pattern p = Pattern.compile("^\\d{10,11}$");

        Matcher m = p.matcher(testString);

        System.out.println(m.matches());


        String entryPointName = "ValidPolicyNumber2";

        Policy policy = new Policy();
        policy.setPolicyNumber("123456789");

        EntryPointResult entryPointResult = engine.evaluate(policy, entryPointName);
        assertThat(entryPointResult, hasValidationFailures(1));

        policy.setPolicyNumber("12345678901234");

        entryPointResult = engine.evaluate(policy, entryPointName);
        assertThat(entryPointResult, hasValidationFailures(1));
    }
}
