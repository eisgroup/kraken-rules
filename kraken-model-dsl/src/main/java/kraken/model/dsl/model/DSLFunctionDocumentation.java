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
package kraken.model.dsl.model;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

/**
 * @author mulevicius
 */
public class DSLFunctionDocumentation {

    private final String description;
    private final String since;
    private final List<DSLFunctionExample> examples;
    private final List<DSLParameterDocumentation> parameterDocumentations;

    public DSLFunctionDocumentation(String description,
                                    @Nullable String since,
                                    List<DSLFunctionExample> examples,
                                    List<DSLParameterDocumentation> parameterDocumentations) {
        this.description = Objects.requireNonNull(description);
        this.since = since;
        this.examples = Objects.requireNonNull(examples);
        this.parameterDocumentations = Objects.requireNonNull(parameterDocumentations);
    }

    public String getDescription() {
        return description;
    }

    @Nullable
    public String getSince() {
        return since;
    }

    public List<DSLFunctionExample> getExamples() {
        return examples;
    }

    public List<DSLParameterDocumentation> getParameterDocumentations() {
        return parameterDocumentations;
    }
}
