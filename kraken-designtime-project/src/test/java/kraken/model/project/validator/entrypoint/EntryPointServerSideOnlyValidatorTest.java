/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.model.project.validator.entrypoint;

import static kraken.model.project.KrakenProjectMocks.krakenProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import kraken.model.Rule;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationSession;
import kraken.namespace.Namespaced;

/**
 * @author Tomas Dapkunas
 */
public class EntryPointServerSideOnlyValidatorTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    @Test
    public void shouldPassWhenNonServerSideOnlyEntryPointHasOnlyNonServerSideRules() {
        List<Rule> rules = List.of(
            createRule("firstNonSSORule", false),
            createRule("secondNonSSORule", false)
        );
        List<EntryPoint> entryPoints = List.of(
            createEntryPoint("nonSSOEntryPoint", rules, false)
        );

        KrakenProject krakenProject = createProject(entryPoints, rules);

        ValidationSession validationSession = doValidate(entryPoints.get(0), krakenProject);

        assertFalse(validationSession.hasEntryPointError());
        assertThat(validationSession.getValidationMessages(), hasSize(0));
    }

    @Test
    public void shouldPassWhenServerSideOnlyEntryPointHasServerSideRules() {
        List<Rule> rules = List.of(
            createRule("ssoRule", true),
            createRule("nonSSORule", false)
        );
        List<EntryPoint> entryPoints = List.of(
            createEntryPoint("ssoEntryPoint", rules, true)
        );

        KrakenProject krakenProject = createProject(entryPoints, rules);

        ValidationSession validationSession = doValidate(entryPoints.get(0), krakenProject);

        assertFalse(validationSession.hasEntryPointError());
        assertThat(validationSession.getValidationMessages(), hasSize(0));
    }

    @Test
    public void shouldFailWhenNonServerSideOnlyEntryPointHasServerSideRules() {
        List<Rule> rules = List.of(
            createRule("ssoRule", true),
            createRule("nonSSORule", false)
        );
        List<EntryPoint> entryPoints = List.of(
            createEntryPoint("nonSSOEntryPoint", rules, false)
        );

        KrakenProject krakenProject = createProject(entryPoints, rules);

        ValidationSession validationSession = doValidate(entryPoints.get(0), krakenProject);

        assertTrue(validationSession.hasEntryPointError());
        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.ERROR));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(), containsString(
            "Entry point is not annotated as @ServerSideOnly, "
                + "but includes one or more rule annotated as @ServerSideOnly: ssoRule."
        ));
    }

    @Test
    public void shouldFailWhenNonServerSideOnlyEntryPointIncludesEntryPointHavingServerSideRules() {
        List<Rule> rules = List.of(
            createRule("nonSSORule", false),
            createRule("nestedSSORule", true),
            createRule("otherNestedSSORule", true),
            createRule("yetAnotherSSORule", true),
            createRule("anotherNonSSORule", false)
        );
        List<EntryPoint> entryPoints = List.of(
            createEntryPoint("nonSSOEntryPoint", List.of(rules.get(0)), false, "nestedEntryPoint"),
            createEntryPoint("nestedEntryPoint", List.of(rules.get(1), rules.get(2)), false, "anotherNestedEntryPoint"),
            createEntryPoint("anotherNestedEntryPoint", List.of(rules.get(3)), false),
            createEntryPoint("anotherNestedEntryPoint", List.of(rules.get(4)), false)
        );

        KrakenProject krakenProject = createProject(entryPoints, rules);

        ValidationSession validationSession = doValidate(entryPoints.get(0), krakenProject);

        assertTrue(validationSession.hasEntryPointError());
        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.ERROR));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(), containsString(
            "Entry point is not annotated as @ServerSideOnly, but includes one or more rule annotated as @ServerSideOnly: nestedSSORule, otherNestedSSORule, yetAnotherSSORule."
        ));

    }

    private ValidationSession doValidate(EntryPoint entryPoint, KrakenProject krakenProject) {
        EntryPointServerSideOnlyValidator entryPointServerSideOnlyValidator
            = new EntryPointServerSideOnlyValidator(krakenProject);
        ValidationSession validationSession = new ValidationSession();

        entryPointServerSideOnlyValidator.validate(entryPoint, validationSession);

        return validationSession;
    }

    private Rule createRule(String ruleName, boolean serverSideOnly) {
        Rule rule = factory.createRule();

        rule.setName(ruleName);
        rule.setServerSideOnly(serverSideOnly);

        return rule;
    }

    private EntryPoint createEntryPoint(String entryPointName,
                                        List<Rule> rules,
                                        boolean serverSideOnly,
                                        String... includedEpNames) {
        EntryPoint entryPoint = factory.createEntryPoint();

        entryPoint.setName(entryPointName);
        entryPoint.setRuleNames(rules.stream()
            .map(Namespaced::getName)
            .collect(Collectors.toList()));
        entryPoint.setServerSideOnly(serverSideOnly);
        entryPoint.setIncludedEntryPointNames(Arrays.asList(includedEpNames));

        return entryPoint;
    }

    private KrakenProject createProject(List<EntryPoint> entryPoints, List<Rule> rules) {
        return krakenProject(List.of(), entryPoints, rules);
    }

}
