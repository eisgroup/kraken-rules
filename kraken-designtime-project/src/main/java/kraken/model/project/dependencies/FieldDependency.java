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
package kraken.model.project.dependencies;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * Represents a dependency on context or context field
 *
 * @author rimas
 * @since 1.0
 */
public class FieldDependency {

    private final String contextName;

    private final String fieldName;

    private final boolean ccrDependency;

    private final boolean selfDependency;

    public FieldDependency(String contextName, @Nullable String fieldName, boolean ccrDependency, boolean selfDependency) {
        this.contextName = contextName;
        this.fieldName = fieldName;
        this.ccrDependency = ccrDependency;
        this.selfDependency = selfDependency;
    }

    public String getContextName() {
        return contextName;
    }

    @Nullable
    public String getFieldName() {
        return fieldName;
    }

    /**
     *
     * @return true if dependency is resolved from a cross context reference.
     * Reference to rule target context is NOT a cross context reference but rather a {@link #isSelfDependency()}
     */
    public boolean isCcrDependency() {
        return ccrDependency;
    }

    /**
     *
     * @return true if dependency is resolved from a reference to target context or a field of target context
     */
    public boolean isSelfDependency() {
        return selfDependency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FieldDependency that = (FieldDependency) o;
        return contextName.equals(that.contextName)
            && Objects.equals(fieldName, that.fieldName)
            && ccrDependency == that.ccrDependency
            && selfDependency == that.selfDependency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextName, fieldName, ccrDependency, selfDependency);
    }

    @Override
    public String toString() {
        return fieldName != null
            ? contextName + '.' + fieldName
            : contextName;
    }
}
