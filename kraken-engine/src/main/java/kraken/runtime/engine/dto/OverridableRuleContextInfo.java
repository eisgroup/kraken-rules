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
package kraken.runtime.engine.dto;

import kraken.annotations.API;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Overridable rule context information provided for
 * {@link kraken.runtime.engine.result.reducers.validation.RuleOverrideStatusResolver}
 * to resolve if rule is overridden or not by provided context and effectiveness date
 */
@API
public class OverridableRuleContextInfo {

    /**
     *
     * @return namespace that indicates KrakenProject of this evaluation
     */
    private final String namespace;

    /**
     * Context on which overridden rule should apply
     */
    private final String contextId;

    /**
     * Root context id
     */
    private final String rootContextId;

    /**
     * Name of context that rule was evaluated on
     */
    private final String contextName;

    /**
     * Context attribute value on which rule override is applied
     */
    private final Object contextAttributeValue;

    /**
     * {@link kraken.runtime.RuleEngine} invocation timestamp
     */
    private final LocalDateTime ruleEvaluationTimeStamp;

    /**
     * A set of dependencies that overridable evaluation result depends on.
     */
    private final Map<String, OverrideDependency> overrideDependencies;

    public OverridableRuleContextInfo(
            String namespace,
            String contextId,
            String rootContextId,
            String contextName,
            Object contextAttributeValue,
            LocalDateTime ruleEvaluationTimeStamp,
            Map<String, OverrideDependency> overrideDependencies
    ) {
        this.namespace = namespace;
        this.contextId = contextId;
        this.rootContextId = rootContextId;
        this.contextName = contextName;
        this.contextAttributeValue = contextAttributeValue;
        this.ruleEvaluationTimeStamp = ruleEvaluationTimeStamp;
        this.overrideDependencies = Objects.requireNonNull(overrideDependencies);
    }

    public String getNamespace() {
        return namespace;
    }

    public String getContextId() {
        return contextId;
    }

    public String getRootContextId() {
        return rootContextId;
    }

    public String getContextName() {
        return contextName;
    }

    public Object getContextAttributeValue() {
        return contextAttributeValue;
    }

    public LocalDateTime getRuleEvaluationTimeStamp() {
        return ruleEvaluationTimeStamp;
    }

    public Map<String, OverrideDependency> getOverrideDependencies() {
        return overrideDependencies;
    }
}
