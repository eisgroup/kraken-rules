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
package kraken.model.impl;

import java.util.ArrayList;
import java.util.List;

import kraken.model.Expression;
import kraken.model.Function;
import kraken.model.FunctionDocumentation;
import kraken.model.FunctionExample;
import kraken.model.FunctionParameter;
import kraken.model.GenericTypeBound;

/**
 * @author mulevicius
 */
public class FunctionImpl implements Function {

    private String name;

    private String physicalNamespace;

    private String returnType;

    private List<FunctionParameter> parameters;

    private List<GenericTypeBound> genericTypeBounds;

    private Expression body;

    private FunctionDocumentation documentation;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPhysicalNamespace() {
        return physicalNamespace;
    }

    @Override
    public void setPhysicalNamespace(String physicalNamespace) {
        this.physicalNamespace = physicalNamespace;
    }

    @Override
    public String getReturnType() {
        return returnType;
    }

    @Override
    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    @Override
    public List<FunctionParameter> getParameters() {
        if(this.parameters == null) {
            this.parameters = new ArrayList<>();
        }
        return parameters;
    }

    @Override
    public void setParameters(List<FunctionParameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public List<GenericTypeBound> getGenericTypeBounds() {
        if(this.genericTypeBounds == null) {
            this.genericTypeBounds = new ArrayList<>();
        }
        return genericTypeBounds;
    }

    @Override
    public void setGenericTypeBounds(List<GenericTypeBound> genericTypeBounds) {
        this.genericTypeBounds = genericTypeBounds;
    }

    @Override
    public Expression getBody() {
        return body;
    }

    @Override
    public void setBody(Expression body) {
        this.body = body;
    }

    @Override
    public FunctionDocumentation getDocumentation() {
        return documentation;
    }

    @Override
    public void setDocumentation(FunctionDocumentation documentation) {
        this.documentation = documentation;
    }
}
