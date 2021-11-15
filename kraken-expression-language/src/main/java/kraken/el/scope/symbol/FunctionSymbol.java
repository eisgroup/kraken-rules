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
package kraken.el.scope.symbol;

import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.GenericType;
import kraken.el.scope.type.Type;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author mulevicius
 */
public class FunctionSymbol extends Symbol {

    private final List<FunctionParameter> parameters;

    public FunctionSymbol(String name, Type type, List<FunctionParameter> parameters) {
        super(name, type);

        this.parameters = Collections.unmodifiableList(Objects.requireNonNull(parameters));
    }

    public List<FunctionParameter> getParameters() {
        return parameters;
    }

    public FunctionParameter findGenericParameter(GenericType genericType) {
        return parameters.stream()
                .filter(functionParameter -> unwrapScalarType(functionParameter.getType()).getName().equals(genericType.getName()))
                .findFirst()
                .orElseThrow();
    }

    private Type unwrapScalarType(Type type) {
        if(type instanceof ArrayType) {
            return unwrapScalarType(((ArrayType) type).getElementType());
        }
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        FunctionSymbol that = (FunctionSymbol) o;
        return Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), parameters);
    }

    @Override
    public String toString() {
        String parameterString = parameters.stream().map(Object::toString).collect(Collectors.joining(", ", "(", ")"));
        return getName() + parameterString + " : " + getType();
    }
}
