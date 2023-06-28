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

import static java.lang.String.format;
import static kraken.model.project.KrakenProjectMocks.entryPoint;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import kraken.model.Expression;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.dsl.read.DSLReader;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.ResourceKrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;
import kraken.model.validation.AssertionPayload;

/**
 * @author psurinin@eisgroup.com
 * @since 11.3
 */
public class RuleCrossContextCardinalityValidatorTest {

    private List<ContextDefinition> contextDefinitions;

    @Before
    public void setUp() {
        DSLReader reader = new DSLReader();
        this.contextDefinitions = reader.read("RuleCrossContextCardinalityValidatorTest/").stream()
                .flatMap(r -> r.getContextDefinitions().stream())
                .collect(Collectors.toList());
    }

    @Test
    public void shouldValidateCcrWithoutInheritance() {
        final String ruleContext = "Coverage";
        final String reference = "RiskItem";
        final String namespace = "Auto";
        final Rule rule = rule(namespace, ruleContext, reference);

        KrakenProject krakenProject = toKrakenProject(namespace, List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);
        assertThat(validationMessages, hasSize(1));
        final ValidationMessage failure = validationMessages.iterator().next();
        assertThat(failure.getSeverity(), is(Severity.ERROR));
        assertThat(failure.getMessage(), containsString(format("from '%s' to '%s'", ruleContext, reference)));
        assertThat(failure.getMessage(), containsString("PolicyAuto.CoverageVehicle.Coverage"));
        assertThat(failure.getMessage(), containsString("PolicyAuto.CoverageVehicle.Vehicle.Coverage"));
    }

    @Test
    public void shouldValidateCcrWithInheritance_AUTO_namespace_Vehicle() {
        final String ruleContext = "Coverage";
        final String reference = "Vehicle";
        final String namespace = "Auto";
        final Rule rule = rule(namespace, ruleContext, reference);

        KrakenProject krakenProject = toKrakenProject(namespace, List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(1));
        final ValidationMessage failure = validationMessages.iterator().next();
        assertThat(failure.getSeverity(), is(Severity.ERROR));
        assertThat(failure.getMessage(), containsString(format("from '%s' to '%s'", ruleContext, reference)));
        assertThat(failure.getMessage(), containsString("PolicyAuto.CoverageVehicle.Coverage"));
        assertThat(failure.getMessage(), containsString("PolicyAuto.CoverageVehicle.Vehicle.Coverage"));
    }

    @Test
    public void shouldValidateCcrWithInheritance_AUTO_namespace_RiskItem() {
        final String ruleContext = "Coverage";
        final String reference = "RiskItem";
        final String namespace = "Auto";
        final Rule rule = rule(namespace, ruleContext, reference);

        KrakenProject krakenProject = toKrakenProject(namespace, List.of(rule));

        List<ValidationMessage> validationMessages = validate(rule, krakenProject);

        assertThat(validationMessages, hasSize(1));
        final ValidationMessage failure = validationMessages.iterator().next();
        assertThat(failure.getSeverity(), is(Severity.ERROR));
        assertThat(failure.getMessage(), containsString(format("from '%s' to '%s'", ruleContext, reference)));
        assertThat(failure.getMessage(), containsString("PolicyAuto.CoverageVehicle.Coverage"));
        assertThat(failure.getMessage(), containsString("PolicyAuto.CoverageVehicle.Vehicle.Coverage"));
    }

    private KrakenProject toKrakenProject(String namespace, List<Rule> rules) {
        var ruleNames = rules.stream()
                .map(Rule::getName)
                .collect(Collectors.toList());
        var rootContextDefinition = contextDefinitions.stream()
                .filter(contextDefinition -> contextDefinition.isRoot() && contextDefinition.getPhysicalNamespace().equals(namespace))
                .findFirst()
                .map(contextDefinition -> contextDefinition.getName())
                .orElse(null);

        return new ResourceKrakenProject(
            namespace,
            rootContextDefinition,
            contextDefinitions.stream().collect(Collectors.toMap(ContextDefinition::getName, c -> c)),
            List.of(entryPoint("Validate", ruleNames)),
            rules,
            null,
            Map.of(),
            null,
            List.of(),
            List.of(),
            List.of()
        );
    }

    private Rule rule(String namespace, String ruleContext, String reference) {
        final Rule rule = RulesModelFactory.getInstance().createRule();
        rule.setName("R01");
        rule.setContext(ruleContext);
        rule.setPhysicalNamespace(namespace);
        rule.setTargetPath("code");
        final AssertionPayload assertionPayload = RulesModelFactory.getInstance().createAssertionPayload();
        final Expression expression = RulesModelFactory.getInstance().createExpression();
        expression.setExpressionString(reference + " == null");
        assertionPayload.setAssertionExpression(expression);
        rule.setPayload(assertionPayload);
        return rule;
    }

    private List<ValidationMessage> validate(Rule rule, KrakenProject krakenProject) {
        RuleCrossContextCardinalityValidator validator = new RuleCrossContextCardinalityValidator(krakenProject);
        ValidationSession session = new ValidationSession();
        validator.validate(rule, session);
        return session.getValidationMessages();
    }

}
