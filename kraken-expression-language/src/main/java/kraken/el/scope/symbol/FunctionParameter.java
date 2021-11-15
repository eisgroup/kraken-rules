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

import kraken.el.scope.type.Type;

import java.util.Objects;

/**
 * @author mulevicius
 */
public class FunctionParameter {

    private final int parameterIndex;

    private final Type type;

    public FunctionParameter(int parameterIndex, Type type) {
        this.parameterIndex = parameterIndex;
        this.type = type;
    }

    public int getParameterIndex() {
        return parameterIndex;
    }

    public Type getType() {
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
        FunctionParameter that = (FunctionParameter) o;
        return parameterIndex == that.parameterIndex && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parameterIndex, type);
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
