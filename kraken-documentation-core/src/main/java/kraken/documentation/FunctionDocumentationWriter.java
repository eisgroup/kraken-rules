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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import kraken.el.functionregistry.documentation.LibraryDoc;
import kraken.model.project.KrakenProjectBuilder;
import kraken.namespace.Namespaced;

/**
 * Writes kraken function documentation to markdown file for every accessible function in scope of a namespace.
 *
 * @author mulevicius
 */
public class FunctionDocumentationWriter {

    private final KrakenProjectBuilder krakenProjectBuilder;

    private final Map<String, String> sinceVersionMapping;

    public FunctionDocumentationWriter(KrakenProjectBuilder krakenProjectBuilder,
                                       Map<String, String> sinceVersionMapping) {
        this.krakenProjectBuilder = krakenProjectBuilder;
        this.sinceVersionMapping = sinceVersionMapping;
    }

    public void write(String namespace, Path outputDirectory) {
        FunctionDocumentationResolver functionDocumentationResolver = new FunctionDocumentationResolver();

        List<LibraryDoc> libraryDocumentations = functionDocumentationResolver.resolveLibraryDocumentations(
            krakenProjectBuilder.buildKrakenProject(namespace));

        var generator = new FunctionDocumentationMarkdownGenerator(sinceVersionMapping);
        String documentation = generator.generateLibraryPage(libraryDocumentations);

        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot create directory: " + outputDirectory, e);
        }

        String fileName = Namespaced.GLOBAL.equals(namespace) ? "Global" : namespace;
        Path outputFile = outputDirectory.resolve(fileName + ".md");

        try {
            Files.writeString(outputFile, documentation);
        } catch (IOException e) {
            throw new IllegalArgumentException("Cannot write function documentation to file: " + outputFile, e);
        }
    }

}
