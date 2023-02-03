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
package kraken.runtime.repository.dynamic;

import kraken.annotations.API;
import kraken.dimensions.DimensionSet;
import kraken.model.Rule;

import java.util.Optional;
import java.util.Set;

/**
 * Holds instance of dynamically generated rule and a set of dimensions by which this rule varies.
 * Used in implementation of {@link DynamicRuleRepository} to return dynamically generated rules.
 *
 * @since 1.40.0
 * @author rimas
 */
@API
public final class DynamicRuleHolder {

    private final Rule rule;

    private final DimensionSet dimensionSet;

    /**
     * Create holder instance for rule, when set of dimensions, by which this rule is varied is known.
     * Note, that while the rule is dynamically generated, an implementation of {@link DynamicRuleRepository}
     * must ensure that dimension set is the same between all possible variations of this rule.
     *
     * @param rule          dynamically generated rule instance
     * @param dimensions    set of dimension names, by which this rule is varied
     * @return
     */
    public static DynamicRuleHolder createForDimensions(Rule rule, Set<String> dimensions) {
        var dimensionSet = DimensionSet.createForDimensions(dimensions);
        return new DynamicRuleHolder(rule, dimensionSet);
    }

    /**
     * Create holder instance for rule when rule is static and does not vary by any dimensions
     *
     * @param rule      dynamically generated rule instance
     * @return
     */
    public static DynamicRuleHolder createNonDimensional(Rule rule) {
        return new DynamicRuleHolder(rule, DimensionSet.createStatic());
    }

    /**
     * Create holder instance for rule when rule is varied by dimensions, but the set of these dimensions is
     * not known.
     *
     * @param rule      dynamically generated rule instance
     * @return
     */
    public static DynamicRuleHolder createUnknownDimensions(Rule rule) {
        return new DynamicRuleHolder(rule, DimensionSet.createForUnknownDimensions());
    }

    public DynamicRuleHolder(Rule rule, DimensionSet dimensionSet) {
        this.rule = rule;
        this.dimensionSet = dimensionSet;
    }

    public Rule getRule() {
        return rule;
    }

    public DimensionSet getDimensionSet() {
        return dimensionSet;
    }
}
