/*
 *  Copyright © 2019 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 *  CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.
 *
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
