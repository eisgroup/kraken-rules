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
     * @param namespace indicates a project for which repository is being invoked.
     * @param entryPoint a simple name of entryPoint that indicates which rules shall be provided.
     *                   If entryPoint includes other entryPoints then this repository will be independently invoked for each include.
     *                   Note, that entryPoint is a simple name and does NOT have a namespace prefix.
     * @param context is a data used to vary implementations of {@link Rule} logic that are applicable for the given entryPoint.
     *                context usually contains dimensions, but can also contain other data as a key value pairs.
     * @return a stream of dynamically generated rules
     */
    Stream<Rule> resolveRules(String namespace, String entryPoint, Map<String, Object> context);
}
