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

import static kraken.model.context.PrimitiveFieldDataType.DECIMAL;
import static kraken.model.project.KrakenProjectMocks.bound;
import static kraken.model.project.KrakenProjectMocks.contextDefinition;
import static kraken.model.project.KrakenProjectMocks.field;
import static kraken.model.project.KrakenProjectMocks.function;
import static kraken.model.project.KrakenProjectMocks.krakenProjectWithFunctions;
import static kraken.model.project.KrakenProjectMocks.parameter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.collection.IsEmptyCollection.empty;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

import kraken.el.scope.type.Type;
import kraken.model.Function;
import kraken.model.context.ContextDefinition;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

/**
 * @author mulevicius
 */
public class FunctionBodyValidatorTest {

    @Test
    public void shouldAllowToUseVariables() {
        ContextDefinition contextDefinition = contextDefinition("Party", List.of(
            field("name"),
            field("age", DECIMAL)
        ));

        KrakenProject krakenProject = krakenProject(
            contextDefinition,
            function("Age", "Number", List.of(parameter("party", "Party")),
                "if party.age >= 18 then party.age else 18")
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);
        assertThat(validationMessages, empty());
    }

    @Test
    public void shouldAllowToInvokeItself() {
        KrakenProject krakenProject = krakenProject(
            function("Fibonacci", "Number", List.of(parameter("n", "Number")),
                "if n=0 or n=1 then n else Fibonacci(n-2) + Fibonacci(n-1)")
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);
        assertThat(validationMessages, empty());
    }

    @Test
    public void shouldAllowToInvokeOtherFunctions() {
        KrakenProject krakenProject = krakenProject(
            function("DelegateCount", "Number", List.of(parameter("collection", "Any[]")),
                "Count(collection)")
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);
        assertThat(validationMessages, empty());
    }

    @Test
    public void shouldAllowToInvokeFunctionsWithGenerics() {
        ContextDefinition contextDefinition = contextDefinition("Party", List.of(
            field("name")
        ));

        KrakenProject krakenProject = krakenProject(
            contextDefinition,
            function("Calculate",
                "String",
                List.of(parameter("partyArray", "Party[]")),
                "First({partyArray[0]}, First(partyArray, (partyArray.name)[0], {''}).name, partyArray.name).name"),
            function("First",
                "<T>",
                List.of(parameter("arr", "<T>[]"), parameter("n1", "<S>"), parameter("n2", "<S>[]")),
                "arr[0]",
                List.of(bound("T", "Party"), bound("S", "String")))
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);
        assertThat(validationMessages, empty());
    }

    @Test
    public void shouldFailIfWrongGenericTypeIsReturned() {
        ContextDefinition contextDefinition = contextDefinition("Party", List.of(
            field("name")
        ));

        KrakenProject krakenProject = krakenProject(
            contextDefinition,
            function("Return",
                "<T>",
                List.of(parameter("arr1", "<T>[]"),  parameter("arr2", "<S>[]")),
                "arr2[0]",
                List.of(bound("T", "Party"), bound("S", "Party")))
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);
        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldFailIfBodyEvaluationTypeViolatesGenericBound() {
        ContextDefinition contextDefinition = contextDefinition("Party", List.of(
            field("name")
        ));

        KrakenProject krakenProject = krakenProject(
            contextDefinition,
            function("First",
                "<T>",
                List.of(parameter("arr", "<T>[]")),
                "arr[0].name",
                List.of(bound("T", "Party")))
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);
        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldFailIfBodyIsNotParseable() {
        ContextDefinition contextDefinition = contextDefinition("Party", List.of(
            field("name"),
            field("age", DECIMAL)
        ));

        KrakenProject krakenProject = krakenProject(
            contextDefinition,
            function("Age", "Number", List.of(parameter("party", "Party")),
                "if party.age >= 18 ? party.age : 18")
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);
        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldFailIfBodyHasSyntaxErrors() {
        ContextDefinition contextDefinition = contextDefinition("Party", List.of(
            field("name"),
            field("age", DECIMAL)
        ));

        KrakenProject krakenProject = krakenProject(
            contextDefinition,
            function("Age", "Number", List.of(parameter("party", "Party")),
                "if party.age >= 18 then party.age else party.defaultAge")
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);
        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldFailIfBodyIsLogicallyEmpty() {
        KrakenProject krakenProject = krakenProject(function("Age", "Number", List.of(),"// aa"));

        List<ValidationMessage> validationMessages = validate(krakenProject);
        assertThat(validationMessages, hasSize(1));
    }

    @Test
    public void shouldFailIfBodyHasSyntaxErrorsDueToGenericBounds() {
        ContextDefinition contextDefinition = contextDefinition("Party", List.of(
            field("name"),
            field("age", DECIMAL)
        ));

        KrakenProject krakenProject = krakenProject(
            contextDefinition,
            function("Age", "Number", List.of(parameter("party", "Party")),
                "if party.age >= 18 then party.age else party.defaultAge")
        );

        List<ValidationMessage> validationMessages = validate(krakenProject);
        assertThat(validationMessages, hasSize(1));
    }

    private KrakenProject krakenProject(Function... functions) {
        return krakenProjectWithFunctions(List.of(), List.of(), Arrays.asList(functions));
    }

    private KrakenProject krakenProject(ContextDefinition contextDefinition, Function... functions) {
        return krakenProjectWithFunctions(List.of(contextDefinition), List.of(), Arrays.asList(functions));
    }

    private List<ValidationMessage> validate(KrakenProject krakenProject) {
        ValidationSession session = new ValidationSession();
        new FunctionBodyValidator(krakenProject).validate(session);
        return session.getValidationMessages();
    }
}
