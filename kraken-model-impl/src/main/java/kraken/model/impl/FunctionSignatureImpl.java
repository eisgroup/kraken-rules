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
package kraken.model.impl;

import java.util.ArrayList;
import java.util.List;

import kraken.model.FunctionSignature;
import kraken.model.GenericTypeBound;

/**
 * @author mulevicius
 */
public class FunctionSignatureImpl implements FunctionSignature {

    private String name;
    private String physicalNamespace;
    private String returnType;
    private List<String> parameterTypes;
    private List<GenericTypeBound> genericTypeBounds;

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
    public List<String> getParameterTypes() {
        if(this.parameterTypes == null) {
            this.parameterTypes = new ArrayList<>();
        }
        return parameterTypes;
    }

    @Override
    public void setParameterTypes(List<String> parameterTypes) {
        this.parameterTypes = parameterTypes;
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
}
