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
package kraken.dimensions;

import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import kraken.annotations.API;

/**
 *  Represents information about rule variability by dimensions. Holds the set of dimension names,
 *  by which rule is varied, as well as variability type.
 *  If rule varies by rule execution timezone then the timezone must also be provided as a special kind of dimension
 *  with name {@link kraken.context.Context#RULE_TIMEZONE_ID_DIMENSION}.
 *  Otherwise, rule caching may behave incorrectly when timezone changes between rule evaluations.
 *
 * @author rimas
 * @since 1.40.0
 */
@API
public class DimensionSet {

    /**
     * Represents rule or entry point variability type - when artifact is static, varied by known set
     * of dimensions, varied by unknown set of dimensions
     */
    public enum Variability {
        STATIC, KNOWN, UNKNOWN
    }

    private final Set<String> dimensions;

    private final Variability variability;

    /**
     * Use this to when set of dimensions by which rule or entry point is varied is known
     *
     * @param dimensions
     * @return
     */
    public static DimensionSet createForDimensions(Set<String> dimensions) {
        if (dimensions == null) return createForUnknownDimensions();
        if (dimensions.size() == 0) return createStatic();
        return new DimensionSet(dimensions, Variability.KNOWN);
    }

    /**
     * Use this to instantiate dimension set for static rules or entry points
     *
     * @return
     */
    public static DimensionSet createStatic() {
        return new DimensionSet(Set.of(), Variability.STATIC);
    }

    /**
     * Use this to instantiate dimension set for rules or entry points, varied by uknown set
     * of dimensions
     *
     * @return
     */
    public static DimensionSet createForUnknownDimensions() {
        return new DimensionSet(null, Variability.UNKNOWN);
    }

    private DimensionSet(Set<String> dimensions, Variability variability) {
        this.dimensions = dimensions;
        this.variability = variability;
    }

    public Variability getVariability() {
        return variability;
    }

    @Nullable
    public Set<String> getDimensions() {
        return dimensions;
    }

    public boolean isDimensional() {
        return variability == Variability.UNKNOWN || variability == Variability.KNOWN;
    }

    public boolean isStatic() {
        return variability == Variability.STATIC;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DimensionSet that = (DimensionSet) o;
        return Objects.equals(dimensions, that.dimensions) && variability == that.variability;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dimensions, variability);
    }

    @Override
    public String toString() {
        if (dimensions == null) {
            return "null";
        }
        var sb = new StringBuilder();
        sb.append("[");
        sb.append(String.join(", ", dimensions));
        sb.append("]");
        return sb.toString();
    }
}

