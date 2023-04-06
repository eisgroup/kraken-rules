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
package kraken.model.project.validator.context;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextNavigation;
import kraken.model.project.KrakenProject;
import kraken.model.project.KrakenProjectMocks;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

/**
 * @author Tomas Dapkunas
 * @since 1.42.0
 */
public class ContextDefinitionChildrenValidatorTest {

    private ValidationSession validationSession;

    @Before
    public void setUp() {
        validationSession = new ValidationSession();
    }

    @Test
    public void shouldAddValidationMessageToSessionWhenChildContextDoesNotExist() {
        KrakenProject krakenProject = createProject(createDefinition("ContextDefOne", "ContextDefTwo"));
        doValidate(krakenProject);

        assertThat(validationSession.getValidationMessages(), hasSize(1));

        ValidationMessage message = validationSession.getValidationMessages().get(0);

        assertThat(message.getItem().getName(), is("ContextDefOne"));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getMessage(),
            containsString("child 'ContextDefTwo' is not valid because such context does not exist"));
    }

    @Test
    public void shouldAddValidationMessageToSessionWhenSystemContextIsUsedAsAChild() {
        KrakenProject krakenProject = createProject(
            createDefinition("ContextDefOne", "SystemContextDef"),
            createSystemDefinition("SystemContextDef"));

        doValidate(krakenProject);

        assertThat(validationSession.getValidationMessages(), hasSize(1));

        ValidationMessage message = validationSession.getValidationMessages().get(0);

        assertThat(message.getItem().getName(), is("ContextDefOne"));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getMessage(),
            containsString("child 'SystemContextDef' is not valid because it is a system context."
                + " System context cannot be used as a child in another context"));
    }

    @Test
    public void shouldNotAddAnyValidationMessagesForValidContextDefinition() {
        KrakenProject krakenProject = createProject(
            createDefinition("ContextDefOne", "ContextDefTwo"),
            createDefinition("SystemContextDef"),
            createDefinition("ContextDefTwo"));

        doValidate(krakenProject);

        assertThat(validationSession.getValidationMessages(), hasSize(0));
    }

    private void doValidate(KrakenProject krakenProject) {
        ContextDefinitionChildrenValidator validator = new ContextDefinitionChildrenValidator(krakenProject);
        validator.validate(validationSession);
    }

    private KrakenProject createProject(ContextDefinition... contextDefinitions) {
        return KrakenProjectMocks.krakenProject(Arrays.asList(contextDefinitions), List.of(), List.of());
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
