/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.repository.dynamic.trace;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kraken.runtime.model.rule.RuntimeRule;
import kraken.tracer.Operation;

/**
 * Operation to be added to trace to wrap querying of dynamic rules from dynamic
 * repository. States details of dynamic repository to be queried and result
 * returned from dynamic rules repository.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class QueryingDynamicRulesOperation implements Operation<Map<String, List<RuntimeRule>>> {

    private final String dynamicRepositoryName;

    public QueryingDynamicRulesOperation(String dynamicRepositoryName) {
        this.dynamicRepositoryName = dynamicRepositoryName;
    }

    @Override
    public String describe() {
        var template = "Querying '%s' repository for dynamic rules.";

        return String.format(template, dynamicRepositoryName);
    }

    @Override
    public String describeAfter(Map<String, List<RuntimeRule>> result) {
        var template = "Repository '%s' returned %s dynamic rules: %s";

        return String.format(template,
            dynamicRepositoryName,
            result.values().size(),
            describeRules(result));
    }

    private String describeRules(Map<String, List<RuntimeRule>> result) {
        if (result.size() > 0) {
            return System.lineSeparator() + result.keySet().stream()
                .map(runtimeRules -> "'" + runtimeRules + "'")
                .collect(Collectors.joining("," + System.lineSeparator()));
        }

        return "";
    }

}
