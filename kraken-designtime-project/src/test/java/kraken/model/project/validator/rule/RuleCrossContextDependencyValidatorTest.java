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
package kraken.model.project.validator.rule;

import static kraken.model.project.KrakenProjectMocks.entryPoint;
import static kraken.model.project.KrakenProjectMocks.rule;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import kraken.model.Condition;
import kraken.model.Expression;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.dsl.read.DSLReader;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.ResourceKrakenProject;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

/**
 *
 * @author kbublys
 */
public class RuleCrossContextDependencyValidatorTest {

    private static RulesModelFactory factory = RulesModelFactory.getInstance();

    private List<ContextDefinition> contextDefinitions;

    @Before
    public void setUp() {
        DSLReader reader = new DSLReader();
        this.contextDefinitions = reader.read("RuleCrossContextDependencyValidatorTest/").stream()
                .flatMap(r -> r.getContextDefinitions().stream())
                .collect(Collectors.toList());
    }

    @Test
    public void shouldValidateRuleWithCCRInTheSameLevel() {
        Rule rule = rule("R01", "VehicleInfo", "info");
        Expression expression = factory.createExpression();
        expression.setExpressionString("AddressLine.addressLine = 'B'");
        Condition condition = factory.createCondition();
        condition.setExpression(expression);
        rule.setCondition(condition);

        KrakenProject krakenProject = toKrakenProject("Base", List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(1));
        assertThat(
                validationMessages.get(0).getMessage(),
                is(
                "Found ambiguities for cross-context component:'AddressLine'.\n" +
                        "Cannot distinguish between cross-context references: [\n" +
                        "\tFrom: AutoPolicySummary.Vehicle.VehicleInfo to AutoPolicySummary.Vehicle.AddressInfo.AddressLine3,\n" +
                        "\tFrom: AutoPolicySummary.Vehicle.VehicleInfo to AutoPolicySummary.Vehicle.AddressInfo.AddressLine2,\n" +
                        "\tFrom: AutoPolicySummary.Vehicle.VehicleInfo to AutoPolicySummary.Vehicle.AddressInfo.AddressLine1\n" +
                        "]")
                );
    }

    @Test
    public void shouldValidateAndFoundAmbiguities() {
        Rule rule = rule("R01", "AutoPolicySummary", "policyNumber");
        Expression expression = factory.createExpression();
        expression.setExpressionString("AddressLine.addressLine = 'B'");
        Condition condition = factory.createCondition();
        condition.setExpression(expression);
        rule.setCondition(condition);

        KrakenProject krakenProject = toKrakenProject("Base", List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(1));
        assertThat(
                validationMessages.get(0).getMessage(),
                is(
                "Found ambiguities for cross-context component:'AddressLine'.\n" +
                    "Cannot distinguish between cross-context references: [\n" +
                        "\tFrom: AutoPolicySummary to AutoPolicySummary.Vehicle.AddressInfo.AddressLine3,\n" +
                        "\tFrom: AutoPolicySummary to AutoPolicySummary.Vehicle.AddressInfo.AddressLine2,\n" +
                        "\tFrom: AutoPolicySummary to AutoPolicySummary.Vehicle.AddressInfo.AddressLine1,\n" +
                        "\tFrom: AutoPolicySummary to AutoPolicySummary.Party.DriverInfo.AddressInfo.AddressLine3,\n" +
                        "\tFrom: AutoPolicySummary to AutoPolicySummary.Party.DriverInfo.AddressInfo.AddressLine2,\n" +
                        "\tFrom: AutoPolicySummary to AutoPolicySummary.Party.DriverInfo.AddressInfo.AddressLine1\n" +
                    "]")
        );
    }

    private KrakenProject toKrakenProject(String namespace, List<Rule> rules) {
        List<String> ruleNames = rules.stream().map(Rule::getName).collect(Collectors.toList());
        return new ResourceKrakenProject(
            namespace,
            contextDefinitions.isEmpty() ? null : contextDefinitions.get(0).getName(),
            contextDefinitions.stream().collect(Collectors.toMap(ContextDefinition::getName, c -> c)),
            List.of(entryPoint("Validate", ruleNames)),
            rules,
            null,
            Map.of(),
            null,
            List.of(),
            List.of()
        );
    }

    private List<ValidationMessage> validate(Rule rule, KrakenProject krakenProject) {
        RuleCrossContextDependencyValidator validator = new RuleCrossContextDependencyValidator(krakenProject);
        ValidationSession session = new ValidationSession();
        validator.validate(rule, session);
        return session.getValidationMessages();
    }


}
