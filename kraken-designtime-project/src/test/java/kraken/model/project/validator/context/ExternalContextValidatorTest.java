/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.validator.context;

import static kraken.model.project.KrakenProjectMocks.externalContextDefinition;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import kraken.model.context.Cardinality;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinition;
import kraken.model.project.KrakenProject;
import kraken.model.project.KrakenProjectMocks;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

/**
 * Unit tests for {@code ExternalContextValidator} class.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
public class ExternalContextValidatorTest {

    @Test
    public void shouldNotValidateIfExternalContextIsNoSet() {
        KrakenProject krakenProject = krakenProject(null, List.of());

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(0));
    }

    @Test
    public void shouldValidateThatOnlyOneContextIsDefinedUnderRootExternalContext() {
        KrakenProject krakenProject = krakenProject(KrakenProjectMocks.externalContext(List.of("one", "two")), List.of());

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(1));
        ValidationMessage message = validationMessages.iterator().next();
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getMessage(), is("Root external context definition should be empty or have "
            + "ONE element named context, but found: one, two."));
    }

    @Test
    public void shouldAllowEmptyExternalContext() {
        KrakenProject krakenProject = krakenProject(KrakenProjectMocks.externalContext(), List.of());

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, empty());
    }

    @Test
    public void shouldValidateThatContextNamedContextIsDefinedUnderRootExternalContext() {
        KrakenProject krakenProject = krakenProject(KrakenProjectMocks.externalContext(List.of("other")), List.of());

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(1));
        ValidationMessage message = validationMessages.iterator().next();
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getMessage(), is("Root external context definition should be empty or have "
            + "ONE element named context, but found: other."));
    }

    @Test
    public void shouldValidateReferencedContextDefinitionExistsInKrakenProject() {
        ExternalContext childContext = KrakenProjectMocks.externalContext();
        childContext.setName("context");
        childContext.setExternalContextDefinitions(Map.of("unknown",
                KrakenProjectMocks.createExternalContextDefinitionReference("unknown")));

        ExternalContext rootContext = KrakenProjectMocks.externalContext();
        rootContext.getContexts().put("context", childContext);

        KrakenProject krakenProject = krakenProject(rootContext, List.of());

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(1));
        ValidationMessage message = validationMessages.iterator().next();
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getMessage(), is("Referenced external context definition must exist in kraken project, "
            + "but the following referenced contexts are not found: unknown."));
    }

    @Test
    public void shouldValidateNameClashesBetweenExternalContextsAndDefinitions() {
        ExternalContext grandChildContext = KrakenProjectMocks.externalContext();
        ExternalContext childContext = KrakenProjectMocks.externalContext();
        childContext.setName("context");
        childContext.setExternalContextDefinitions(Map.of("type",
                KrakenProjectMocks.createExternalContextDefinitionReference("type")));
        childContext.setContexts(Map.of("type", grandChildContext));

        ExternalContext rootContext = KrakenProjectMocks.externalContext();
        rootContext.getContexts().put("context", childContext);

        KrakenProject krakenProject = krakenProject(rootContext,
                List.of(externalContextDefinition("type", List.of())));

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(1));
        ValidationMessage message = validationMessages.iterator().next();
        assertThat(message.getSeverity(), is(Severity.ERROR));
        assertThat(message.getMessage(), is("Naming clash between external context definitions and child external "
            + "context found, clashing values: type."));
    }

    @Test
    public void shouldNotReturnsAnyValidationMessagesForValidDefinition() {
        ExternalContext grandChildContext = KrakenProjectMocks.externalContext();
        ExternalContext childContext = KrakenProjectMocks.externalContext();
        childContext.setName("context");
        childContext.setExternalContextDefinitions(Map.of("type",
                KrakenProjectMocks.createExternalContextDefinitionReference("type")));
        childContext.setContexts(Map.of("grandCtx", grandChildContext));

        ExternalContext rootContext = KrakenProjectMocks.externalContext();
        rootContext.getContexts().put("context", childContext);

        KrakenProject krakenProject = krakenProject(rootContext,
            List.of(externalContextDefinition("type", List.of())));

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(0));
    }

    @Test
    public void shouldValidateNonExistingTypeAttribute() {
        ExternalContextDefinition externalContextDefinition = KrakenProjectMocks.externalContextDefinition("typeName",
            List.of(KrakenProjectMocks.externalContextDefinitionAttribute("attributeName",
                KrakenProjectMocks.externalContextDefinitionAttributeType("SomeType", false, Cardinality.SINGLE))));

        List<ValidationMessage> messages = validate(krakenProject(null, List.of(externalContextDefinition)));

        assertThat(messages, hasSize(1));

        ValidationMessage message = messages.iterator().next();

        assertThat(message.getMessage(), is("Type 'SomeType' of field 'attributeName' is unknown or not supported."));
        assertThat(message.getSeverity(), is(Severity.ERROR));
    }

    @Test
    public void shouldReturnNoValidationMessagesFromCorrectPrimitiveType() {
        ExternalContextDefinition externalContextDefinition = KrakenProjectMocks.externalContextDefinition("TypeName",
            List.of(KrakenProjectMocks.externalContextDefinitionAttribute("attributeName",
                KrakenProjectMocks.externalContextDefinitionAttributeType("STRING", true, Cardinality.SINGLE))));

        List<ValidationMessage> messages = validate(krakenProject(null, List.of(externalContextDefinition)));

        assertThat(messages, hasSize(0));
    }

    @Test
    public void shouldReturnNoValidationMessagesFromCorrectComplexType() {
        ExternalContextDefinition externalContextDefinition = KrakenProjectMocks.externalContextDefinition("TypeName",
            List.of(KrakenProjectMocks.externalContextDefinitionAttribute("attributeName",
                KrakenProjectMocks.externalContextDefinitionAttributeType("STRING", true, Cardinality.SINGLE))));
        ExternalContextDefinition otherExternalContextDefinition = KrakenProjectMocks.externalContextDefinition("OtherName",
            List.of(KrakenProjectMocks.externalContextDefinitionAttribute("attributeName",
                KrakenProjectMocks.externalContextDefinitionAttributeType("OtherName", true, Cardinality.SINGLE))));

        List<ValidationMessage> messages =
            validate(krakenProject(null,
                List.of(externalContextDefinition, otherExternalContextDefinition)));

        assertThat(messages, hasSize(0));
    }

    @Test
    public void shouldReturnNoValidationMessagesFromCorrectSystemType() {
        ExternalContextDefinition externalContextDefinition = KrakenProjectMocks.externalContextDefinition("TypeName",
            List.of(KrakenProjectMocks.externalContextDefinitionAttribute("attributeName",
                KrakenProjectMocks.externalContextDefinitionAttributeType("UNKNOWN", true, Cardinality.SINGLE))));

        List<ValidationMessage> messages = validate(krakenProject(null, List.of(externalContextDefinition)));

        assertThat(messages, hasSize(0));
    }

    private KrakenProject krakenProject(ExternalContext externalContext,
                                        List<ExternalContextDefinition> externalContextDefinitions) {
        return KrakenProjectMocks.krakenProject(
            List.of(),
            externalContext,
            externalContextDefinitions,
            List.of(),
            List.of()
        );
    }

    private List<ValidationMessage> validate(KrakenProject krakenProject) {
        ValidationSession session = new ValidationSession();
        new ExternalContextValidator(krakenProject).validate(session);
        return session.getValidationMessages();
    }
}
