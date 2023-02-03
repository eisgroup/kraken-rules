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
package kraken.el.ast.dependency;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * A known reference in expression.
 *
 * @author mulevicius
 */
public class Reference {

    private final String typeName;

    private final String referenceName;

    private final boolean global;

    public Reference(@Nullable String typeName, String referenceName, boolean global) {
        this.typeName = typeName;
        this.referenceName = referenceName;
        this.global = global;
    }

    /**
     * @return name of the type that this is referenced within.
     * For example, if the reference is limitAmount, then the type name is Coverage.
     * Will be null if the reference {@link #isGlobal()}.
     */
    @Nullable
    public String getTypeName() {
        return typeName;
    }

    /**
     * @return name of the reference as defined in expression
     */
    public String getReferenceName() {
        return referenceName;
    }

    /**
     * @return if true, then the reference is a variable in global scope
     */
    public boolean isGlobal() {
        return global;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Reference that = (Reference) o;
        return Objects.equals(typeName, that.typeName) &&
                Objects.equals(referenceName, that.referenceName) &&
                Objects.equals(global, that.global);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeName, referenceName, global);
    }

    @Override
    public String toString() {
        return typeName != null
            ? typeName + '.' + referenceName
            : referenceName;
    }
}
