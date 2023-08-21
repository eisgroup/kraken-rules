/*
 * Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.validator.rule;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.context.ContextNavigation;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.KrakenProjectMocks;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

/**
 * @author Tomas Dapkunas
 * @since 1.42.0
 */
public class RuleTargetContextValidatorTest {

    private ValidationSession validationSession;
    @Before
    public void setUp() {
        validationSession = new ValidationSession();
    }

    @Test
    public void shouldAddValidationMessageWhenRuleDefinedOnNonExistingContext() {
        Rule rule = KrakenProjectMocks.rule("ruleName", "ContextDefOne", "targetAttr");
        KrakenProject krakenProject = createProject(rule, createDefinition("ContextDefTwo"));

        doValidate(rule, krakenProject);

        assertThat(validationSession.getValidationMessages(), hasSize(1));

        ValidationMessage message = validationSession.getValidationMessages().get(0);

        assertThat(message.getItem().getName(), is(rule.getName()));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getMessage(), containsString("Missing context definition with name 'ContextDefOne'."));
    }

    @Test
    public void shouldAddValidationMessageWhenRuleDefinedOnSystemContext() {
        Rule rule = KrakenProjectMocks.rule("ruleName", "SystemContextDef", "targetAttr");
        KrakenProject krakenProject = createProject(rule, createSystemDefinition("SystemContextDef"));

        doValidate(rule, krakenProject);

        assertThat(validationSession.getValidationMessages(), hasSize(1));

        ValidationMessage message = validationSession.getValidationMessages().get(0);

        assertThat(message.getItem().getName(), is(rule.getName()));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getMessage(),
            containsString("Cannot be applied on system context definition 'SystemContextDef'."));
    }

    @Test
    public void shouldAddValidationMessageWhenRuleRuleDefinedOnNonExistingField() {
        Rule rule = KrakenProjectMocks.rule("ruleName", "ContextDefTwo", "targetAttr");
        KrakenProject krakenProject = createProject(
            rule,
            createStrictDefinition("ContextDefTwo", createField("otherTargetAttr"))
        );

        doValidate(rule, krakenProject);

        assertThat(validationSession.getValidationMessages(), hasSize(1));

        ValidationMessage message = validationSession.getValidationMessages().get(0);

        assertThat(message.getItem().getName(), is(rule.getName()));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getMessage(),
            containsString("Context definition 'ContextDefTwo' doesn't have field 'targetAttr'."));
    }

    @Test
    public void shouldAddValidationMessageWhenRuleRuleDefinedOnExternalField() {
        Rule rule = KrakenProjectMocks.rule("ruleName", "ContextDefTwo", "targetAttr");
        KrakenProject krakenProject = createProject(
            rule,
            createStrictDefinition("ContextDefTwo", createFieldForbiddenAsTarget("targetAttr"))
        );

        doValidate(rule, krakenProject);

        assertThat(validationSession.getValidationMessages(), hasSize(1));

        ValidationMessage message = validationSession.getValidationMessages().get(0);

        assertThat(message.getItem().getName(), is(rule.getName()));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getMessage(),
            containsString("Cannot be applied on a field 'targetAttr' because it is forbidden to be a rule target."));
    }

    private KrakenProject createProject(Rule rule, ContextDefinition... contextDefinitions) {
        EntryPoint  entryPoint = KrakenProjectMocks.entryPoint("entryPointName", rule.getName());
        return KrakenProjectMocks.krakenProject(Arrays.asList(contextDefinitions), List.of(entryPoint), List.of(rule));
    }

    private void doValidate(Rule rule, KrakenProject krakenProject) {
        RuleTargetContextValidator validator = new RuleTargetContextValidator(krakenProject);
        validator.validate(rule, validationSession);
    }

    private ContextField createField(String name) {
        return KrakenProjectMocks.field(name);
    }

    private ContextField createFieldForbiddenAsTarget(String name) {
        return KrakenProjectMocks.fieldForbiddenAsTarget(name);
    }

    private ContextDefinition createStrictDefinition(String name, ContextField... fields) {
        return KrakenProjectMocks
            .contextDefinition(name, Arrays.asList(fields), List.of(), List.of());
    }

    private ContextDefinition createDefinition(String name, String... childContextNames) {
        List<ContextNavigation> childContexts = Arrays.stream(childContextNames)
            .map(KrakenProjectMocks::child)
            .collect(Collectors.toList());

        return KrakenProjectMocks
            .contextDefinition(name, List.of(), List.of(), childContexts);
    }

    private ContextDefinition createSystemDefinition(String name) {
        return KrakenProjectMocks.toSystemContextDefinition(name);
    }

}
