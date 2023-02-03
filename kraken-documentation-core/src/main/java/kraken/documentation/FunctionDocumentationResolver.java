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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import kraken.el.functionregistry.FunctionHeader;
import kraken.el.functionregistry.FunctionRegistry;
import kraken.el.functionregistry.documentation.AdditionalInfoReader;
import kraken.el.functionregistry.documentation.ExampleDoc;
import kraken.el.functionregistry.documentation.FunctionDoc;
import kraken.el.functionregistry.documentation.GenericTypeDoc;
import kraken.el.functionregistry.documentation.LibraryDoc;
import kraken.el.functionregistry.documentation.ParameterDoc;
import kraken.model.Function;
import kraken.model.FunctionDocumentation;
import kraken.model.ParameterDocumentation;
import kraken.model.project.KrakenProject;
import kraken.namespace.Namespaced;

/**
 * Resolves {@link LibraryDoc} for all available functions.
 * Consider functions implemented on the classpath as well as functions
 * implemented in Kraken DLS that are resolved from {@link KrakenProject}
 *
 * @author mulevicius
 */
public class FunctionDocumentationResolver {

    public List<LibraryDoc> resolveLibraryDocumentations() {
        List<LibraryDoc> docs = new ArrayList<>(FunctionRegistry.getLibraryDocs());
        return sorted(docs);
    }

    public List<LibraryDoc> resolveLibraryDocumentations(KrakenProject krakenProject) {
        Map<String, LibraryDoc> dslLibraryDocs = krakenProject.getFunctions().stream()
            .collect(Collectors.groupingBy(
                f -> f.getPhysicalNamespace().equals(Namespaced.GLOBAL) ? "Global" : f.getPhysicalNamespace(),
                Collectors.collectingAndThen(
                    Collectors.toList(),
                    libraryFunctions -> new LibraryDoc(
                        libraryFunctions.get(0).getPhysicalNamespace(),
                        null,
                        null,
                        libraryFunctions.stream()
                            .map(this::resolveFunctionDoc)
                            .collect(Collectors.toList())
                    )
                )
            ));

        List<LibraryDoc> docs = new ArrayList<>(FunctionRegistry.getLibraryDocs());
        docs.addAll(dslLibraryDocs.values());
        return sorted(docs);
    }

    private List<LibraryDoc> sorted(List<LibraryDoc> docs) {
        docs.sort(Comparator.comparing(LibraryDoc::getName));
        return docs;
    }

    private FunctionDoc resolveFunctionDoc(Function function) {
        FunctionHeader header = new FunctionHeader(function.getName(), function.getParameters().size());
        return new FunctionDoc(
            header,
            description(function),
            AdditionalInfoReader.read(header),
            since(function),
            examples(function),
            parameters(function),
            function.getReturnType(),
            null,
            generics(function)
        );
    }

    private String description(Function function) {
        return function.getDocumentation() != null ? function.getDocumentation().getDescription() : null;
    }

    private String since(Function function) {
        return function.getDocumentation() != null ? function.getDocumentation().getSince() : null;
    }

    private List<ExampleDoc> examples(Function function) {
        if(function.getDocumentation() != null) {
            return function.getDocumentation().getExamples().stream()
                .map(e -> new ExampleDoc(e.getExample(), e.getResult(), e.isValid()))
                .collect(Collectors.toList());
        }
        return List.of();
    }

    private List<ParameterDoc> parameters(Function function) {
        var parameterDocs = Optional.ofNullable(function.getDocumentation())
            .map(FunctionDocumentation::getParameterDocumentations)
            .map(parameterDocumentations -> parameterDocumentations.stream()
                .collect(Collectors.toMap(ParameterDocumentation::getParameterName, p -> p)))
            .orElse(Map.of());

        return function.getParameters().stream()
                .map(p -> new ParameterDoc(
                    p.getName(),
                    p.getType(),
                    Optional.ofNullable(parameterDocs.get(p.getName()))
                        .map(ParameterDocumentation::getDescription).orElse(null)
                ))
                .collect(Collectors.toList());
    }

    private List<GenericTypeDoc> generics(Function function) {
        return function.getGenericTypeBounds().stream()
            .map(b -> new GenericTypeDoc(b.getGeneric(), b.getBound()))
            .collect(Collectors.toList());
    }
}
