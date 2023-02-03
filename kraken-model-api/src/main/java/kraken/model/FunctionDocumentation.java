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
package kraken.model;

import java.util.List;

import kraken.annotations.API;

/**
 * Models a documentation of a particular {@link Function}.
 * Documentation must clearly explain what a function does in a business friendly terms.
 * Documentation may be displayed in various design-time specific contexts for the rule developers
 * so that they can understand what the functions do and how to use it.
 *
 * @author mulevicius
 */
@API
public interface FunctionDocumentation {

    String getDescription();

    void setDescription(String description);

    String getSince();

    void setSince(String since);

    List<FunctionExample> getExamples();

    void setExamples(List<FunctionExample> examples);

    List<ParameterDocumentation> getParameterDocumentations();

    void setParameterDocumentations(List<ParameterDocumentation> parameterDocumentations);
}
