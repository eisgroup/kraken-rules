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

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import kraken.model.context.ContextDefinition;
import kraken.model.project.KrakenProject;
import kraken.model.project.KrakenProjectMocks;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

/**
 * @author Tomas Dapkunas
 * @since 1.42.0
 */
public class ContextDefinitionInheritanceValidatorTest {

    private ValidationSession validationSession;

    @Before
    public void setUp() {
        validationSession = new ValidationSession();
    }

    @Test
    public void shouldAddValidationMessageWhenParentContextIsNotPresent() {
        KrakenProject krakenProject = createProject(
            createDefinition("BaseContext", "AnotherContext"),
            createDefinition("OtherContext"));

        doValidate(krakenProject);

        assertThat(validationSession.getValidationMessages(), hasSize(1));

        ValidationMessage message = validationSession.getValidationMessages().get(0);

        assertThat(message.getItem().getName(), is("BaseContext"));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getMessage(), containsString(
            "parent 'AnotherContext' is not valid because such ContextDefinition does not exist"));
    }

    @Test
    public void shouldAddValidationMessageWhenSystemContextIsUsedAsAParent() {
        KrakenProject krakenProject = createProject(
            createDefinition("BaseContext", "SystemContext"),
            createSystemDefinition("SystemContext"));

        doValidate(krakenProject);

        assertThat(validationSession.getValidationMessages(), hasSize(1));

        ValidationMessage message = validationSession.getValidationMessages().get(0);

        assertThat(message.getItem().getName(), is("BaseContext"));
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getMessage(), containsString(
            "parent 'SystemContext' is not valid because such system ContextDefinition cannot be inherited"));
    }

    private void doValidate(KrakenProject krakenProject) {
        ContextDefinitionInheritanceValidator validator = new ContextDefinitionInheritanceValidator(krakenProject);
        validator.validate(validationSession);
    }

    private KrakenProject createProject(ContextDefinition... contextDefinitions) {
        return KrakenProjectMocks.krakenProject(Arrays.asList(contextDefinitions), List.of(), List.of());
    }

    private ContextDefinition createDefinition(String name, String... parentNames) {
        return KrakenProjectMocks.contextDefinition(name, List.of(), Arrays.asList(parentNames), List.of());
    }
    private ContextDefinition createSystemDefinition(String name) {
        return KrakenProjectMocks.toSystemContextDefinition(name);
    }

}
