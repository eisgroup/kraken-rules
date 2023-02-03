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
package kraken.documentation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import kraken.el.functionregistry.FunctionHeader;
import kraken.el.functionregistry.documentation.ExampleDoc;
import kraken.el.functionregistry.documentation.FunctionDoc;
import kraken.el.functionregistry.documentation.GenericTypeDoc;
import kraken.el.functionregistry.documentation.LibraryDoc;
import kraken.el.functionregistry.documentation.ParameterDoc;

/**
 * Unit tests for {@code FunctionMarkdownDocumentCreator} class.
 *
 * @author mulevicius
 */
public class FunctionDocumentationMarkdownGeneratorTest {

    @Test
    public void shouldGenerateLibraryFunctionDocs() throws IOException {
        String expected = IOUtils.toString(
            Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("FunctionDocumentationMarkdownGeneratorTest/expected.md"),
            StandardCharsets.UTF_8.name()
        );

        var functionDoc = new FunctionDoc(
                new FunctionHeader("Function", 1),
                "Function description",
                "> Additional info in block",
                "1.0",
                List.of(new ExampleDoc("Function(true)", "result", true),
                        new ExampleDoc("Function(null)", null, false)),
                List.of(
                    new ParameterDoc("p1", "Boolean", "Boolean parameter description"),
                    new ParameterDoc("p2", "String", "String parameter description")
                ),
                "Boolean",
                "If error happens",
                List.of()
        );

        var functionDocWithGenerics = new FunctionDoc(
            new FunctionHeader("GenericFunction", 2),
            "Function description",
            null,
            "1.0",
            List.of(),
            List.of(
                new ParameterDoc("p1", "<T>", "Generic parameter description"),
                new ParameterDoc("p2", "<N>", "Generic parameter description")
            ),
            "<T>",
            null,
            List.of(
                new GenericTypeDoc("T", "Date | DateTime"),
                new GenericTypeDoc("N", "Number")

            )
        );

        var firstLibraryDocs = new LibraryDoc(
                "Library",
                "Library description",
                "0.0-beta",
                List.of(functionDoc, functionDocWithGenerics)
        );

        var functionDocWithoutDescriptions = new FunctionDoc(
            new FunctionHeader("SimpleFunction", 0),
            null,
            null,
            null,
            List.of(),
            List.of(),
            "String",
            null,
            List.of()
        );

        var secondLibraryDocs = new LibraryDoc(
            "Simple Library",
            null,
            null,
            List.of(functionDocWithoutDescriptions)
        );

        var result = new FunctionDocumentationMarkdownGenerator(Map.of("0.0-beta", "1.0"))
            .generateLibraryPage(List.of(firstLibraryDocs, secondLibraryDocs));

        expected = expected.lines().collect(Collectors.joining(System.lineSeparator()));
        result = result.lines().collect(Collectors.joining(System.lineSeparator()));

        assertThat(result, equalTo(expected));
    }

}
