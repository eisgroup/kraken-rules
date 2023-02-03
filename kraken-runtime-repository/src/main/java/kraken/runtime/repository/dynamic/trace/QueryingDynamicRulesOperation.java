/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
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
