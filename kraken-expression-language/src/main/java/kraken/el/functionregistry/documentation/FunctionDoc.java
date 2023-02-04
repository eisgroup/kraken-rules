/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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

package kraken.el.functionregistry.documentation;

import java.util.List;

import kraken.el.functionregistry.FunctionHeader;

public class FunctionDoc {

    private final FunctionHeader functionHeader;
    private final String description;
    private final String additionalInfo;
    private final String since;
    private final List<ExampleDoc> examples;
    private final List<ParameterDoc> parameters;
    private final String returnType;
    private final String throwsError;
    private final List<GenericTypeDoc> genericTypes;

    public FunctionDoc(FunctionHeader functionHeader,
                       String description,
                       String additionalInfo,
                       String since,
                       List<ExampleDoc> examples,
                       List<ParameterDoc> parameters,
                       String returnType,
                       String throwsError,
                       List<GenericTypeDoc> genericTypes) {
        this.functionHeader = functionHeader;
        this.description = description;
        this.additionalInfo = additionalInfo;
        this.since = since;
        this.examples = examples;
        this.parameters = parameters;
        this.returnType = returnType;
        this.throwsError = throwsError;
        this.genericTypes = genericTypes;
    }

    public FunctionHeader getFunctionHeader() {
        return functionHeader;
    }

    public String getDescription() {
        return description;
    }

    public String getSince() {
        return since;
    }

    public List<ExampleDoc> getExamples() {
        return examples;
    }

    public List<ParameterDoc> getParameters() {
        return parameters;
    }

    @Override
    public String toString() {
        return functionHeader.toString();
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getThrowsError() {
        return throwsError;
    }

    public List<GenericTypeDoc> getGenericTypes() {
        return genericTypes;
    }
}
