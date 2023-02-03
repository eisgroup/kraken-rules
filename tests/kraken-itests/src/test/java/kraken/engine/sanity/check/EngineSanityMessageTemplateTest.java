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
package kraken.engine.sanity.check;

import static kraken.runtime.utils.TemplateParameterRenderer.TEMPLATE_DATE_TIME_FORMAT;
import static kraken.testing.matchers.KrakenMatchers.hasNoIgnoredRules;
import static kraken.testing.matchers.KrakenMatchers.hasRuleResults;
import static kraken.testing.matchers.KrakenMatchers.hasValidationFailures;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Map;

import org.junit.Test;

import kraken.el.ast.builder.Literals;
import kraken.el.functionregistry.functions.DateFunctions;
import kraken.runtime.EvaluationConfig;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.TransactionDetails;
import kraken.testproduct.domain.Vehicle;

/**
 * Snapshot matching is disabled because date templates depends on server locale
 *
 * @author mulevicius
 */
public class EngineSanityMessageTemplateTest extends SanityEngineBaseTest {

    @Test
    public void shouldEvaluateMessageTemplate() {
        TransactionDetails transactionDetails = new TransactionDetails();
        transactionDetails.setTxEffectiveDate(Literals.getDateTime("2021-01-01T11:00:00Z"));

        Policy policy = new Policy();
        policy.setTransactionDetails(transactionDetails);
        policy.setPolicyNumber("P00");
        policy.setRiskItems(
            Arrays.asList(
                new Vehicle("P01"),
                new Vehicle("P02")
            )
        );

        EvaluationConfig evaluationConfig = new EvaluationConfig(Map.of(), "USD");
        EntryPointResult result = engine.evaluate(policy, "Templates", evaluationConfig);

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasRuleResults(3));
        assertThat(result, hasValidationFailures(2));

        var policyNumberResult = result(result, "Policy:-1:policyNumber");
        var policyNumberMsg = "Policy number 'P00' must be in vehicle models, but vehicle models are: [P01, P02]";
        assertThat(policyNumberResult.getMessage(), equalTo(policyNumberMsg));

        var policyTxEffectiveResult = result(result, "Policy:-1:txEffectiveDate");
        var txEffectiveMsg = String.format(
            "Transaction effective date must be later than %s but was %s",
            format("2021-01-01T10:00:00Z"),
            format("2021-01-01T11:00:00Z")
        );
        assertThat(policyTxEffectiveResult.getMessage(), equalTo(txEffectiveMsg));

        var policyStateResult = result(result, "Policy:-1:state");
        var policyStateMsg = String.format(
            "${nothingtoseehere}  true false  string string 10.123 2020-01-01 %s ABC",
            format("2021-01-01T10:00:00Z")
        );
        assertThat(policyStateResult.getMessage(), equalTo(policyStateMsg));
    }

    private String format(String date) {
        return DateFunctions.dateTime(date).format(TEMPLATE_DATE_TIME_FORMAT);
    }

    private AssertionPayloadResult result(EntryPointResult result, String fieldId) {
        return (AssertionPayloadResult) result.getFieldResults().get(fieldId).getRuleResults().get(0).getPayloadResult();
    }
}
