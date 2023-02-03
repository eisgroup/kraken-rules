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

import kraken.runtime.EvaluationConfig;
import kraken.runtime.engine.EntryPointResult;
import kraken.testproduct.domain.COLLCoverage;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.Insured;
import kraken.testproduct.domain.TransactionDetails;
import kraken.testproduct.domain.Vehicle;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static kraken.testing.matchers.KrakenMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * EntryPoint "ExpressionContextCheck-Condition" {
 * "ec-condition-dimensions",
 * "ec-condition-additional"
 * }
 *
 * EntryPoint "ExpressionContextCheck-Default" {
 * "ec-default-dimensions",
 * "ec-default-additional"
 * }
 *
 * EntryPoint "ExpressionContextCheck-Assert" {
 * "ec-assert-dimensions",
 * "ec-assert-additional"
 * }
 *
 * EntryPoint "ExpressionContextCheck-Assert-CCR" {
 * "ec-assert-additional-ccr"
 * }
 *
 * @author psurinin
 */
public class EngineSanityExpressionContextTest extends SanityEngineBaseTest {

    private static final String PLAN = "Premium";
    private static final String PACKAGE = "Pizza";

    private static class EntryPoint {
        static final String CONDITION = "ExpressionContextCheck-Condition";
        static final String DEFAULT = "ExpressionContextCheck-Default";
        static final String ASSERTION = "ExpressionContextCheck-Assert";
        static final String ASSERTION_CCR = "ExpressionContextCheck-Assert-CCR";
    }

    @Test
    public void shouldCheckExpressionContextCondition() {
        Policy policy = policy();
        EntryPointResult result = engine.evaluate(policy, EntryPoint.CONDITION, config());
        assertThat(result, hasNoIgnoredRules());
        assertThat(policy.getPolicyNumber(), is(PLAN));
        assertThat(policy.getTransactionDetails().getTxReason(), is(PACKAGE));
    }


    @Test
    public void shouldCheckExpressionContextDefault() {
        Policy policy = policy();
        EntryPointResult result = engine.evaluate(policy, EntryPoint.DEFAULT, config());
        assertThat(result, hasNoIgnoredRules());
        assertThat(policy.getPolicyNumber(), is(PLAN));
        assertThat(policy.getTransactionDetails().getTxReason(), is(PACKAGE));
    }

    @Test
    public void shouldCheckExpressionContextAssert() {
        Policy policy = policy();
        policy.setPolicyNumber(PLAN);
        policy.getTransactionDetails().setTxReason(PACKAGE);

        EntryPointResult result = engine.evaluate(policy, EntryPoint.ASSERTION, config());
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasValidationFailures(0));
    }

    @Test
    public void shouldCheckExpressionContextAssertCCR() {
        Policy policy = policy();
        policy.setPolicyNumber(PLAN);
        policy.getTransactionDetails().setTxReason(PACKAGE);
        policy.getInsured().setName(PACKAGE);

        EntryPointResult result = engine.evaluate(policy, EntryPoint.ASSERTION_CCR, config());
        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasValidationFailures(0));
    }

    @Test
    public void shouldEvaluateNestedDynamicContextScope() {
        Policy policy = policyWithPolicyNumber("target-context");

        HashMap<String, Object> context = new HashMap<>();
        context.put("additional", Map.of("policies", List.of(policyWithPolicyNumber("external-context"))));
        EvaluationConfig config = new EvaluationConfig(context, "USD");

        EntryPointResult result = engine.evaluate(policy, "ExpressionContextCheck-NestedScope", config);

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasValidationFailures(0));
    }

    @Test
    public void shouldEvaluateNestedFilterContextScope() {
        Policy policy = policyWithPolicyNumber("policy-make");
        policy.setRiskItems(List.of(vehicleWithModel("vehicle-make")));

        HashMap<String, Object> context = new HashMap<>();
        context.put("additional", Map.of(
            "vehicles", List.of(vehicleWithModel("policy-make"))
        ));
        EvaluationConfig config = new EvaluationConfig(context, "USD");

        EntryPointResult result = engine.evaluate(policy, "ExpressionContextCheck-NestedFilter", config);

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasValidationFailures(0));
    }

    @Test
    public void shouldEvaluateByFlatMappingDynamicContext() {
        Policy policy = policyWithPolicyNumber("last");

        HashMap<String, Object> context = new HashMap<>();
        context.put("additional", Map.of(
            "vehicles", List.of(vehicleWithCoverageCodes("first", "second"), vehicleWithCoverageCodes("third"))
        ));
        EvaluationConfig config = new EvaluationConfig(context, "USD");

        EntryPointResult result = engine.evaluate(policy, "ExpressionContextCheck-FlatMapDynamicContext", config);

        assertThat(result, hasNoIgnoredRules());
        assertThat(result, hasValidationFailures(0));
    }

    private Vehicle vehicleWithCoverageCodes(String... codes) {
        Vehicle vehicle = new Vehicle();
        List<COLLCoverage> collCoverages = new ArrayList<>();
        for(String code : codes) {
            COLLCoverage coverage = new COLLCoverage();
            coverage.setCode(code);
            collCoverages.add(coverage);
        }
        vehicle.setCollCoverages(collCoverages);
        return vehicle;
    }

    private Vehicle vehicleWithModel(String model) {
        Vehicle vehicle = new Vehicle();
        vehicle.setModel(model);
        return vehicle;
    }

    private Policy policyWithPolicyNumber(String policyNumber) {
        Policy policy = new Policy();
        policy.setPolicyNumber(policyNumber);
        return policy;
    }

    private Policy policy() {
        Policy policy = new Policy();
        policy.setTransactionDetails(new TransactionDetails());
        policy.setInsured(new Insured());
        return policy;
    }

    private EvaluationConfig config() {
        return new EvaluationConfig(context(), "USD");
    }

    private Map<String, Object> context() {
        HashMap<String, Object> context = new HashMap<>();
        context.put("dimensions", Map.of("plan", PLAN));
        context.put("additional", Map.of("package", PACKAGE));
        return context;
    }
}
