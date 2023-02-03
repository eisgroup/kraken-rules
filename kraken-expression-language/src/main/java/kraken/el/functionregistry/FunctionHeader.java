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
package kraken.el.functionregistry;

import java.util.Objects;

/**
 * @author mulevicius
 */
public final class FunctionHeader {

    private final String name;

    private final int parameterCount;

    public FunctionHeader(String name, int parameterCount) {
        this.name = Objects.requireNonNull(name);
        this.parameterCount = parameterCount;
    }

    public String getName() {
        return name;
    }

    public int getParameterCount() {
        return parameterCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FunctionHeader that = (FunctionHeader) o;
        return parameterCount == that.parameterCount &&
                name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, parameterCount);
    }

    @Override
    public String toString() {
        return name + "(" + parameterCount + ")";
    }
}
