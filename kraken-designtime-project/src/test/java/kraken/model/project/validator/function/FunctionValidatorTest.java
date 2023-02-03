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

import static kraken.model.project.KrakenProjectMocks.bound;
import static kraken.model.project.KrakenProjectMocks.function;
import static kraken.model.project.KrakenProjectMocks.functionSignature;
import static kraken.model.project.KrakenProjectMocks.krakenProject;
import static kraken.model.project.KrakenProjectMocks.krakenProjectWithFunctions;
import static kraken.model.project.KrakenProjectMocks.parameter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.util.Arrays;
import java.util.List;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.Test;

import kraken.model.Function;
import kraken.model.FunctionSignature;
import kraken.model.context.ContextDefinition;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

/**
 * @author mulevicius
 */
public class FunctionValidatorTest {

    @Test
    public void shouldValidateFunctionSignatures() {
        KrakenProject krakenProject = krakenProject(
            function("USD", "String", List.of(),"'USD'")
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, empty());
    }

    @Test
    public void shouldFailWhenReturnTypeDoesNotExist() {
        KrakenProject krakenProject = krakenProject(
            function("MyEntity", "MissingEntity", List.of(),"'USD'")
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldFailWhenParameterTypeDoesNotExist() {
        KrakenProject krakenProject = krakenProject(
            function("SQRT", "Number", List.of(parameter("number", "Numberrr")), "5")
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldAllowGenericFunctionSignature() {
        KrakenProject krakenProject = krakenProject(
            function("GetFirst", "<T>", List.of(parameter("entities", "<T>[]")), "entities[0]")
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(0));
    }

    @Test
    public void shouldFailWhenThereAreFunctionDuplicates() {
        KrakenProject krakenProject = krakenProject(
            function("USD", "String", List.of(),"'USD'"),
            function("USD", "String", List.of(),"'USD'")
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldFailWhenFunctionHasDuplicateParameters() {
        KrakenProject krakenProject = krakenProject(
            function("Function",
                "Number",
                List.of(parameter("number", "Number"), parameter("number", "Number")),
                "number + number"
            )
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldFailWhenFunctionNameClashesWithFunctionSignature() {
        KrakenProject krakenProject = krakenProject(
            functionSignature("Function", "Number", List.of("Number", "Number")),
            function(
                "Sum",
                "Number",
                List.of(parameter("number1", "Number"), parameter("number2", "Number")),
                "number1 + number2"
            )
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldFailIfFunctionNameIsNative() {
        KrakenProject krakenProject = krakenProject(function("Count", "Number", List.of(), "1"));

        List<ValidationMessage> validationMessages = validate(krakenProject);
        assertThat(validationMessages, IsCollectionWithSize.hasSize(1));
    }

    @Test
    public void shouldFailWhenGenericsAreDuplicated() {
        KrakenProject krakenProject = krakenProject(
            function("First", "<T>", List.of(parameter("c", "<T>[]")), "c[0]",
                List.of(bound("T", "Number"), bound("T", "String")))
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldFailWhenGenericsAreMixedWithUnion() {
        KrakenProject krakenProject = krakenProject(
            function("First", "<T> | String", List.of(parameter("p", "<T>[] | String")), "p[0]",
                List.of(bound("T", "Number")))
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);

        assertThat(validationMessages, hasSize(2));
    }

    private KrakenProject krakenProject(Function... functions) {
        return krakenProjectWithFunctions(List.of(), List.of(), Arrays.asList(functions));
    }

    private KrakenProject krakenProject(FunctionSignature functionSignature, Function function) {
        return krakenProjectWithFunctions(List.of(), List.of(functionSignature), List.of(function));
    }

    private List<ValidationMessage> validate(KrakenProject krakenProject) {
        ValidationSession session = new ValidationSession();
        new FunctionValidator(krakenProject).validate(session);
        return session.getValidationMessages();
    }
}
