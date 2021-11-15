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

import java.util.Collection;
import java.util.Objects;

import kraken.runtime.repository.dynamic.DynamicRuleRepository;
import kraken.runtime.repository.dynamic.DynamicRuleRepositoryCacheConfig;
import kraken.runtime.repository.filter.DimensionFilter;

/**
 * Configuration that needs to be provided when initializing {@link RuntimeProjectRepository}
 *
 * @author mulevicius
 */
public class RuntimeProjectRepositoryConfig {

    private DynamicRuleRepositoryCacheConfig dynamicRuleRepositoryCacheConfig;

    private Collection<DimensionFilter> dimensionFilters;

    private Collection<DynamicRuleRepository> dynamicRuleRepositories;

    public RuntimeProjectRepositoryConfig(DynamicRuleRepositoryCacheConfig dynamicRuleRepositoryCacheConfig,
                                          Collection<DimensionFilter> dimensionFilters,
                                          Collection<DynamicRuleRepository> dynamicRuleRepositories) {
        this.dimensionFilters = Objects.requireNonNull(dimensionFilters);
        this.dynamicRuleRepositories = Objects.requireNonNull(dynamicRuleRepositories);
        this.dynamicRuleRepositoryCacheConfig = Objects.requireNonNull(dynamicRuleRepositoryCacheConfig);
    }

    public DynamicRuleRepositoryCacheConfig getDynamicRuleRepositoryCacheConfig() {
        return dynamicRuleRepositoryCacheConfig;
    }

    public Collection<DimensionFilter> getDimensionFilters() {
        return dimensionFilters;
    }

    public Collection<DynamicRuleRepository> getDynamicRuleRepositories() {
        return dynamicRuleRepositories;
    }
}
