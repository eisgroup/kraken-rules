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

package kraken.testing.reducer;

import kraken.model.Rule;

/**
 * Results data that is common for all types of {@link kraken.model.Rule} payload.
 * Is is included in all reduced results in {@link KrakenReducers}.
 *
 * @author psurinin
 * @see KrakenReducers
 * @since 1.0.38
 */
public class ResultMetadata {
    private final String ruleName;
    private final String id;
    private final String contextName;
    private final String attribute;
    private final String conditionEvaluation;

    public ResultMetadata(String ruleName, String id, String contextName, String attribute, String conditionEvaluation) {
        this.ruleName = ruleName;
        this.id = id;
        this.contextName = contextName;
        this.attribute = attribute;
        this.conditionEvaluation = conditionEvaluation;
    }

    /**
     * @return evaluated rule name
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     * @return entity/data context id
     */
    public String getId() {
        return id;
    }

    /**
     * Resolved entity type/data context id. This id must be unique across all context definition instances.
     * Resolved By {@link kraken.runtime.engine.context.info.DataObjectInfoResolver#resolveContextIdForObject(Object)}
     *
     * @return entity type/ data context id
     * @see kraken.runtime.engine.context.data.DataContext
     * @see kraken.runtime.engine.context.info.DataObjectInfoResolver
     */
    public String getContextName() {
        return contextName;
    }

    /**
     * @return attribute name, onw which rule is defined
     */
    public String getAttribute() {
        return attribute;
    }

    /**
     * @return result of condition evaluation.
     * If condition {@link Rule#getCondition()} is not present or is
     * evaluated to {@code true}, rule is applicable. otherwise is not applicable.
     * @return is rule evaluated
     */
    public String getConditionEvaluation() {
        return conditionEvaluation;
    }
}