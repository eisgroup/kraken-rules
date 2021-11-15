/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
import static kraken.model.project.KrakenProjectMocks.krakenProject;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

import java.util.List;

import org.junit.Test;

import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;
import kraken.model.project.validator.context.ExternalContextValidator;

/**
 * @author mulevicius
 */
public class FunctionSignatureValidatorTest {

    @Test
    public void shouldValidateFunctionSignatures() {
        KrakenProject krakenProject = krakenProject(List.of(), List.of(), List.of(), List.of(
            function("SQRT", "Number", List.of("Number"))
        ));

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(0));
    }

    @Test
    public void shouldFailWhenReturnTypeDoesNotExist() {
        KrakenProject krakenProject = krakenProject(List.of(), List.of(), List.of(), List.of(
            function("SQRT", "MissingEntity[]", List.of("Number"))
        ));

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldFailWhenParameterTypesDoesNotExist() {
        KrakenProject krakenProject = krakenProject(List.of(), List.of(), List.of(), List.of(
            function("SQRT", "Number", List.of("MissingEntity[]", "MissingOtherEntity"))
        ));

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(2));
    }

    private List<ValidationMessage> validate(KrakenProject krakenProject) {
        ValidationSession session = new ValidationSession();
        new FunctionSignatureValidator(krakenProject).validate(session);
        return session.getValidationMessages();
    }
}
