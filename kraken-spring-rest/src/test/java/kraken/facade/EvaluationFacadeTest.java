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
package kraken.facade;

import kraken.model.Expression;
import kraken.model.Rule;
import kraken.model.factory.RulesModelFactory;
import kraken.model.state.AccessibilityPayload;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.RegExpPayload;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.testproduct.domain.*;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static kraken.utils.TestUtils.toDSL;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

public class EvaluationFacadeTest extends AbstractFacadeTest {

    private final static RulesModelFactory RULES_MODEL_FACTORY = RulesModelFactory.getInstance();

    @Test
    public void shouldEvaluateEntryPointWithRulesOnModel(){
        rest.postForEntity(
                "/dynamic/rule/dsl/QA3",
                toDSL(mockFourDifferentRules()),
                String.class
        );
        ResponseEntity<Map> responseEntity = rest.postForEntity(
                "/evaluation/QA3/raw",
                new Policy(),
                Map.class
        );
        List<RuleEvaluationResult> allRuleResults =
                (List<RuleEvaluationResult>)responseEntity.getBody().get("allRuleResults");
        Map<String, Object> fieldResults =
                (Map<String, Object>)responseEntity.getBody().get("fieldResults");

        assertThat(responseEntity.getStatusCode().value(), is(200));
        assertThat(fieldResults.values(), hasSize(2));
        assertThat(allRuleResults, hasSize(3));
        removeRules("assertionRule", "regExpRule", "presentRule", "defaultRule");
    }

    @Test
    public void shouldEvaluateEmptyEntryPointOnModel(){
        ResponseEntity<Map> responseEntity = rest.postForEntity(
                "/evaluation/QA2/raw", new Policy(), Map.class
        );
        List<RuleEvaluationResult> failedRuleResults =
                (List<RuleEvaluationResult>)responseEntity.getBody().get("allRuleResults");
        Map<String, Object> fieldResults = (Map<String, Object>)responseEntity.getBody().get("fieldResults");

        assertThat(responseEntity.getStatusCode().value(), is(200));
        assertThat(fieldResults.values(), hasSize(0));
        assertThat(failedRuleResults, hasSize(0));
    }

    @Test
    public void shouldEvaluateAndReturnEmptyErrorList(){
        Policy policySummary =
                mockAutoPolicy("Q1254", "state", "John", "White");
        rest.postForEntity("/rule/QA3", mockThreeValidationRules(), String.class);
        ResponseEntity<Map> responseEntity = rest.postForEntity(
                "/evaluation/QA3/validation", policySummary, Map.class
        );
        List<Object> resultErrorMessages =
                (List<Object>)((Map<String, Object>)responseEntity.getBody().get("validationStatus")).get("errorResults");

        assertThat(responseEntity.getStatusCode().value(), is(200));
        assertThat(resultErrorMessages, hasSize(0));
        removeRules("R0003", "R0007", "R0150");
    }

    @Test
    public void shouldEvaluateAndReturnThreeErrorList(){
        Policy policySummary =
                mockAutoPolicy("123456789", "5484","John", "John");
        rest.postForEntity("/dynamic/rule/dsl/QA4", toDSL(mockThreeValidationRules()), String.class);
        ResponseEntity<Map> responseEntity = rest.postForEntity(
                "/evaluation/QA4/validation", policySummary, Map.class
        );
        List<Object> resultErrorMessages =
                (List<Object>)((Map<String, Object>)responseEntity
                        .getBody()
                        .get("validationStatus"))
                        .get("errorResults");

        assertThat(responseEntity.getStatusCode().value(), is(200));
        assertThat(resultErrorMessages, hasSize(3));
        removeRules("R0003", "R0007", "R0150");
    }

    private void removeRules(String... ruleNames){
        Arrays.stream(ruleNames).forEach(name -> rest.delete("/rule/" + name));
    }

    private Policy mockAutoPolicy(
            String policyNumber, String state, String personFirstName, String personLastName
    ){
        Policy policySummary = new Policy();
        policySummary.setPolicyNumber(policyNumber);
        policySummary.setState(state);
        Party party = new Party();
        PersonInfo personInfo = new PersonInfo();
        personInfo.setLastName(personFirstName);
        personInfo.setFirstName(personLastName);
        party.setPersonInfo(personInfo);
        policySummary.setParties(Arrays.asList(party));
        BillingInfo billingInfo = new BillingInfo();
        billingInfo.setCreditCardInfo(new CreditCardInfo());
        policySummary.setBillingInfo(billingInfo);
        return policySummary;
    }

    private List<Rule> mockThreeValidationRules(){
        Rule R0003 = RULES_MODEL_FACTORY.createRule();
        RegExpPayload R0003Payload = RULES_MODEL_FACTORY.createRegExpPayload();
        R0003Payload.setRegExp("^Q[0-9]{4}$");
        R0003Payload.setSeverity(ValidationSeverity.critical);
        R0003.setPayload(R0003Payload);
        R0003.setContext("Policy");
        R0003.setTargetPath("policyNumber");
        R0003.setName("mock-R0003");

        Rule R0007 = RULES_MODEL_FACTORY.createRule();
        RegExpPayload R0007Payload = RULES_MODEL_FACTORY.createRegExpPayload();
        R0007Payload.setRegExp("^[A-Za-z]+$");
        R0007Payload.setSeverity(ValidationSeverity.critical);
        R0007.setPayload(R0007Payload);
        R0007.setContext("Policy");
        R0007.setTargetPath("state");
        R0007.setName("mock-R0007");

        Rule R0150 = RULES_MODEL_FACTORY.createRule();
        AssertionPayload R0150Payload = RULES_MODEL_FACTORY.createAssertionPayload();
        Expression R0150Expression = RULES_MODEL_FACTORY.createExpression();
        R0150Expression.setExpressionString("firstName != lastName");
        R0150Payload.setAssertionExpression(R0150Expression);
        R0150Payload.setSeverity(ValidationSeverity.critical);
        R0150.setPayload(R0150Payload);
        R0150.setContext("PersonInfo");
        R0150.setTargetPath("firstName");
        R0150.setName("mock-R0150");

        return Arrays.asList(R0003, R0007, R0150);
    }

    private List<Rule> mockFourDifferentRules(){
        String contextName = "Policy";

        Rule assertionRule = RULES_MODEL_FACTORY.createRule();
        AssertionPayload assertionPayload = RULES_MODEL_FACTORY.createAssertionPayload();
        Expression assertionExpression = RULES_MODEL_FACTORY.createExpression();
        assertionExpression.setExpressionString("state != 'CA'");
        assertionPayload.setAssertionExpression(assertionExpression);
        assertionPayload.setSeverity(ValidationSeverity.critical);
        assertionRule.setPayload(assertionPayload);
        assertionRule.setContext(contextName);
        assertionRule.setTargetPath("state");
        assertionRule.setName("assertionRule");

        Rule regExpRule = RULES_MODEL_FACTORY.createRule();
        RegExpPayload regExpPayload = RULES_MODEL_FACTORY.createRegExpPayload();
        regExpPayload.setRegExp("^Q[0-9]{4}$");
        regExpPayload.setSeverity(ValidationSeverity.critical);
        regExpRule.setPayload(regExpPayload);
        regExpRule.setContext(contextName);
        regExpRule.setTargetPath("policyNumber");
        regExpRule.setName("regExpRule");

        Rule accessibilityRule = RULES_MODEL_FACTORY.createRule();
        AccessibilityPayload accessibilityPayload = RULES_MODEL_FACTORY.createAccessibilityPayload();
        accessibilityPayload.setAccessible(true);
        accessibilityRule.setPayload(accessibilityPayload);
        accessibilityRule.setContext(contextName);
        accessibilityRule.setTargetPath("policyNumber");
        accessibilityRule.setName("presentRule");

        return Arrays.asList(assertionRule, regExpRule, accessibilityRule);
    }
}
