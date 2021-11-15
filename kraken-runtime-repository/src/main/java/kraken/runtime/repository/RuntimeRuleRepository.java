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
package kraken.runtime.repository;

import java.util.Map;

import kraken.runtime.model.entrypoint.RuntimeEntryPoint;
import kraken.runtime.model.rule.RuntimeRule;

/**
 * @author mulevicius
 */
public interface RuntimeRuleRepository {

    /**
     * Provides implementation of rules included in requested entryPoint and filtered by context data
     *
     * @param entryPoint simple name of entryPoint that indicates which rules shall be provided;
     *                   if entryPoint includes other entryPoints then rules will be resolved from included entryPoints as well
     * @param context is data used to determine implementations of {@link RuntimeEntryPoint} and {@link RuntimeRule}
     *                that are applicable for the given entryPoint;
     *                context usually contains dimensions, but can also contain other data
     * @return all rules that shall be executed for the given entryPoint
     */
    Map<String, RuntimeRule> resolveRules(String entryPoint, Map<String, Object> context);
}
