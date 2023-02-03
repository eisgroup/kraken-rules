/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.validator.function;

import static kraken.model.project.KrakenProjectMocks.function;
import static kraken.model.project.KrakenProjectMocks.krakenProjectWithFunctions;
import static kraken.model.project.KrakenProjectMocks.parameter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

import java.util.List;

import org.junit.Test;

import kraken.model.FunctionDocumentation;
import kraken.model.ParameterDocumentation;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

/**
 * @author mulevicius
 */
public class FunctionDocumentationValidatorTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    @Test
    public void shouldPassIfDocumentationISValid() {
        ParameterDocumentation parameterDocumentation1 = factory.createParameterDocumentation();
        parameterDocumentation1.setParameterName("party");
        parameterDocumentation1.setDescription("description");
        FunctionDocumentation documentation = factory.createFunctionDocumentation();
        documentation.setDescription("description");
        documentation.setParameterDocumentations(List.of(parameterDocumentation1));

        KrakenProject krakenProject = krakenProjectWithFunctions(List.of(
            function(
                "Age",
                "Number",
                List.of(parameter("party", "Party")),
                "true",
                List.of(),
                documentation
            )
        ));

        var messages = validate(krakenProject);

        assertThat(messages, hasSize(1));
    }

    @Test
    public void shouldFailIfDuplicateParameterDescriptions() {
        ParameterDocumentation parameterDocumentation1 = factory.createParameterDocumentation();
        parameterDocumentation1.setParameterName("party");
        parameterDocumentation1.setDescription("description");
        ParameterDocumentation parameterDocumentation2 = factory.createParameterDocumentation();
        parameterDocumentation2.setParameterName("party");
        parameterDocumentation2.setDescription("description");
        FunctionDocumentation documentation = factory.createFunctionDocumentation();
        documentation.setDescription("description");
        documentation.setParameterDocumentations(List.of(parameterDocumentation1, parameterDocumentation2));

        KrakenProject krakenProject = krakenProjectWithFunctions(List.of(
            function(
                "Age",
                "Number",
                List.of(parameter("party", "Party")),
                "true",
                List.of(),
                documentation
            )
        ));

        var messages = validate(krakenProject);

        assertThat(messages, hasSize(1));
    }

    @Test
    public void shouldFailIfParameterDoesNotExistDescriptions() {
        ParameterDocumentation parameterDocumentation1 = factory.createParameterDocumentation();
        parameterDocumentation1.setParameterName("unknownParameter");
        parameterDocumentation1.setDescription("description");
        FunctionDocumentation documentation = factory.createFunctionDocumentation();
        documentation.setDescription("description");
        documentation.setParameterDocumentations(List.of(parameterDocumentation1));

        KrakenProject krakenProject = krakenProjectWithFunctions(List.of(
            function(
                "Age",
                "Number",
                List.of(parameter("party", "Party")),
                "true",
                List.of(),
                documentation
            )
        ));

        var messages = validate(krakenProject);

        assertThat(messages, hasSize(1));
    }

    private List<ValidationMessage> validate(KrakenProject krakenProject) {
        ValidationSession session = new ValidationSession();
        new FunctionBodyValidator(krakenProject).validate(session);
        return session.getValidationMessages();
    }
}
