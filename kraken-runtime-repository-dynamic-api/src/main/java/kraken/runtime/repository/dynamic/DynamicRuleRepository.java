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

import kraken.annotations.SPI;
import kraken.model.MetadataAware;
import kraken.model.Rule;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Repository that provides dynamically generated instances of {@link Rule} by dimensions during runtime for a specific entryPoint.
 * <p/>
 * Use {@link kraken.model.factory.RulesModelFactory} when creating instance of {@link Rule}.
 * <p/>
 * Repository implementation will be invoked on each evaluation for each entryPoint at runtime.
 * Because of that, performance requirements for the implementation is at the highest level.
 * <p/>
 * Kraken Engine does NOT maintain internal cache of generated rules because they are in essence dynamic
 * and may vary between invocations based on the context and the logic of variance is specific to repository implementation.
 * Therefore, Kraken Engine cannot assume responsibility of performance issues that may arise because of slow repository implementations.
 * In the end, all responsibility of performance issues related to dynamic rules falls on the repository implementation.
 * <p/>
 * However, Kraken Engine will apply optimisations based on {@link Rule#getRuleVariationId()} in dynamically generated Rule definitions.
 *
 * @see Rule#getRuleVariationId()
 * @author mulevicius
 */
@SPI
public interface DynamicRuleRepository {

    /**
     * Resolves rules for specified entry point, namespace and context.
     *
     * @param namespace indicates a project for which repository is being invoked.
     * @param entryPoint a simple name of entryPoint that indicates which rules shall be provided.
     *                   If entryPoint includes other entryPoints then this repository will be independently invoked for each include.
     *                   Note, that entryPoint is a simple name and does NOT have a namespace prefix.
     * @param context is a data used to vary implementations of {@link Rule} logic that are applicable for the given entryPoint.
     *                context usually contains dimensions, but can also contain other data as a key value pairs.
     * @return a stream of dynamically generated rules
     * @deprecated deprecated since 1.40.0 and should not be implemented.
     *             Implement method {@link #resolveDynamicRules(String, String, Map)} instead.
     */
    @Deprecated(forRemoval = true, since = "1.40.0")
    default Stream<Rule> resolveRules(String namespace, String entryPoint, Map<String, Object> context) {
        throw new UnsupportedOperationException("DynamicRuleRepository#resolveRules is deprecated, " +
            "use DynamicRuleRepository#resolveDynamicRules instead.");
    }

    /**
     * Resolves rules for specified entry point, namespace and context. Each rule is grouped with set of dimensions
     * by which it is varied.
     *
     * @since 1.40.0
     * @param namespace indicates a project for which repository is being invoked.
     * @param entryPoint a simple name of entryPoint that indicates which rules shall be provided.
     *                   If entryPoint includes other entryPoints then this repository will be independently invoked for each include.
     *                   Note, that entryPoint is a simple name and does NOT have a namespace prefix.
     * @param context is a data used to vary implementations of {@link Rule} logic that are applicable for the given entryPoint.
     *                   context usually contains dimensions, but can also contain other data as a key value pairs.
     *                   Context will also contain rule execution time zone as a key {@link kraken.context.Context#RULE_TIMEZONE_ID_DIMENSION}.
     *                   Time zone can be used for date specific calculations during rule resolution.
     *                   If your dynamic rule depends on time zone, then you must add a
     *                   {@link kraken.context.Context#RULE_TIMEZONE_ID_DIMENSION} to {@link kraken.dimensions.DimensionSet}
     *                   as a special kind of dimension that the rule depends on.
     *                   Otherwise, rule caching may behave incorrectly when time zone changes between rule evaluations.
     *
     * @return a stream of {@link DynamicRuleHolder}, each containing a dynamically generated rule and set of dimensions
     *                   which affect its variability
     */
    default Stream<DynamicRuleHolder> resolveDynamicRules(String namespace,
                                                          String entryPoint,
                                                          Map<String, Object> context) {
        return resolveRules(namespace, entryPoint, context)
            .map(rule -> rule.isDimensional() || isVersioned(rule) ?
                DynamicRuleHolder.createUnknownDimensions(rule) :
                DynamicRuleHolder.createNonDimensional(rule));
    }

    private static boolean isVersioned(MetadataAware metadataAware) {
        return metadataAware.getMetadata() != null && !metadataAware.getMetadata().asMap().isEmpty();
    }
}
