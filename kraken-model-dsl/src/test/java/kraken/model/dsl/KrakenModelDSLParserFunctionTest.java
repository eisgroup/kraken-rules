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
package kraken.model.dsl;

import static kraken.model.dsl.KrakenDSLModelParser.parseResource;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.junit.Test;

import kraken.el.scope.type.Type;
import kraken.model.Function;
import kraken.model.FunctionDocumentation;
import kraken.model.dsl.error.LineParseCancellationException;
import kraken.model.resource.Resource;

/**
 * @author mulevicius
 */
public class KrakenModelDSLParserFunctionTest {

    @Test
    public void shouldParseFunctions() {
        Resource model = parseResource(""
            + "Namespace functions "
            + "Function First(Coverage[] coverages, Number index) : Coverage {"
            + "   coverages[index]"
            + "}"
            + "Function USD() : String {"
            + "   'USD'"
            + "}"
        );

        assertFunction(
            model.getFunctions().get(0),
            "First",
            "Coverage",
            List.of(
                new FunctionParameter("coverages", "Coverage[]"),
                new FunctionParameter("index", "Number")
            ),
            "coverages[index]"
        );
        assertFunction(
            model.getFunctions().get(1),
            "USD",
            Type.STRING.toString(),
            List.of(),
            "'USD'"
        );
    }

    @Test
    public void shouldParseFunctionWithGenericBounds() {
        Resource model = parseResource(""
            + "Namespace functions "
            + "Function <T is Date | DateTime, N is Number> First(<T>[] dates, <N> index) : <T> {"
            + "   dates[index]"
            + "}"
        );

        assertFunction(
            model.getFunctions().get(0),
            "First",
            "<T>",
            List.of(
                new FunctionParameter("dates", "<T>[]"),
                new FunctionParameter("index", "<N>")
            ),
            "dates[index]",
            List.of("T is Date|DateTime", "N is Number")
        );
    }

    @Test
    public void shouldParseFunctionWithDocumentation() {
        Resource model = parseResource(""
            + "Namespace functions "
            + "/** "
            + "Returns coverage by index. " + System.lineSeparator() + " Index starts at 0." + System.lineSeparator()
            + "@since 1.0.0 "
            + "@example Get(coverages, 1) @result Coverage "
            + "@example Get(vehicles.coverages, 1) @result Coverage "
            + "@invalidExample Get(vehicles, 1) "
            + "@parameter coverages - A list of coverages. " + System.lineSeparator()
            + "                       Must be at least as index." + System.lineSeparator()
            + "@parameter index - which coverage to return "
            + "*/"
            + "Function Get(Coverage[] coverages, Number index) : Coverage {"
            + "   coverages[index]"
            + "}"
        );

        assertFunction(
            model.getFunctions().get(0),
            "Get",
            "Coverage",
            List.of(
                new FunctionParameter("coverages", "Coverage[]"),
                new FunctionParameter("index", "Number")
            ),
            "coverages[index]"
        );
        assertFunctionDocumentation(
            model.getFunctions().get(0).getDocumentation(),
            "Returns coverage by index. Index starts at 0.",
            "1.0.0",
            List.of(
                new FunctionExample("Get(coverages, 1)", "Coverage", true),
                new FunctionExample("Get(vehicles.coverages, 1)", "Coverage", true),
                new FunctionExample("Get(vehicles, 1)", null, false)
            ),
            List.of(
                new FunctionParameterDocumentation("coverages", "A list of coverages. Must be at least as index."),
                new FunctionParameterDocumentation("index", "which coverage to return")
            )
        );
    }

    @Test
    public void shouldThrowOnMultipleSinceSections() {
        LineParseCancellationException exception = assertThrows(LineParseCancellationException.class, () ->
            parseResource(""
                + "Namespace functions "
                + "/** "
                + "Description. "
                + "@since 1.0.0 "
                + "@since 1.0.0 "
                + "*/"
                + "Function Get() : Boolean {"
                + "   true"
                + "}"
            )
        );
        assertThat(exception.getLine(), is(1));
        assertThat(exception.getColumn(), is(50));
    }

    @Test
    public void shouldThrowOnUnknownSections() {
        LineParseCancellationException exception = assertThrows(LineParseCancellationException.class, () ->
            parseResource(""
                + "Namespace functions "
                + "/** "
                + "Description. "
                + "@slince 1.0.0 "
                + "@since 1.0.0 "
                + "*/"
                + "Function Get() : Boolean {"
                + "   true"
                + "}"
            )
        );

        assertThat(exception.getLine(), is(1));
        assertThat(exception.getColumn(), is(37));
    }

    @Test
    public void shouldThrowOnUnknownSectionsAndAdjustCursor() {
        LineParseCancellationException exception = assertThrows(LineParseCancellationException.class, () ->
            parseResource(""
                + "Namespace functions\n"
                + "/**\n"
                + "Description.\n"
                + "@slince 1.0.0\n"
                + "@since 1.0.0\n"
                + "*/\n"
                + "Function Get() : Boolean {"
                + "   true"
                + "}"
            )
        );

        assertThat(exception.getLine(), is(4));
        assertThat(exception.getColumn(), is(0));
    }

    private void assertFunctionDocumentation(FunctionDocumentation functionDocumentation,
                                             String description,
                                             String since,
                                             List<FunctionExample> examples,
                                             List<FunctionParameterDocumentation> parameterDocumentations) {
        assertThat(functionDocumentation.getDescription(), equalTo(description));
        assertThat(functionDocumentation.getSince(), equalTo(since));
        for(int i = 0; i < examples.size(); i++) {
            var example = functionDocumentation.getExamples().get(i);
            assertThat(example.getExample(), equalTo(examples.get(i).example));
            assertThat(example.getResult(), equalTo(examples.get(i).result));
            assertThat(example.isValid(), equalTo(examples.get(i).valid));
        }
        for(int i = 0; i < parameterDocumentations.size(); i++) {
            var p = functionDocumentation.getParameterDocumentations().get(i);
            assertThat(p.getParameterName(), equalTo(parameterDocumentations.get(i).name));
            assertThat(p.getDescription(), equalTo(parameterDocumentations.get(i).description));
        }
    }

    private void assertFunction(Function f,
                                String name,
                                String returnType,
                                List<FunctionParameter> parameters,
                                String body) {
        assertFunction(f, name, returnType, parameters, body, List.of());
    }

    private void assertFunction(Function f,
                                String name,
                                String returnType,
                                List<FunctionParameter> parameters,
                                String body,
                                List<String> bounds) {
        assertThat(f.getName(), equalTo(name));
        assertThat(f.getReturnType(), equalTo(returnType));
        assertThat(f.getParameters(), hasSize(parameters.size()));
        for(int i = 0; i < parameters.size(); i++) {
            var p = f.getParameters().get(i);
            assertThat(p.getName(), equalTo(parameters.get(i).name));
            assertThat(p.getType(), equalTo(parameters.get(i).type));
        }
        assertThat(f.getBody().getExpressionString(), equalTo(body));
        for(int i = 0; i < bounds.size(); i++) {
            var p = f.getGenericTypeBounds().get(i);
            var bound = p.getGeneric() + " is " + p.getBound();
            assertThat(bound, equalTo(bounds.get(i)));
        }
    }

    static class FunctionParameter {
        String name;
        String type;

        FunctionParameter(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    static class FunctionParameterDocumentation {
        String name;
        String description;

        FunctionParameterDocumentation(String name, String description) {
            this.name = name;
            this.description = description;
        }
    }

    static class FunctionExample {
        String example;
        String result;
        boolean valid;

        FunctionExample(String example, String result, boolean valid) {
            this.example = example;
            this.result = result;
            this.valid = valid;
        }
    }

}
