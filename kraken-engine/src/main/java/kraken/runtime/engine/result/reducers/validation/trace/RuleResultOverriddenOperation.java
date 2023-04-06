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
package kraken.runtime.engine.result.reducers.validation.trace;

import com.google.gson.Gson;

import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.tracer.VoidOperation;
import kraken.utils.GsonUtils;

/**
 * Operation to be added to trace if rule is overridable and override is
 * applicable.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class RuleResultOverriddenOperation implements VoidOperation {

    private static final Gson gson = GsonUtils.prettyGson();
    private final RuleEvaluationResult ruleEvaluationResult;

    public RuleResultOverriddenOperation(RuleEvaluationResult ruleEvaluationResult) {
        this.ruleEvaluationResult = ruleEvaluationResult;
    }

    @Override
    public String describe() {
        var contextInfo = ruleEvaluationResult.getOverrideInfo().getOverridableRuleContextInfo();
        return String.format("Rule '%s' validation result applied on '%s' is overridden and will be ignored. Override info: %s",
            ruleEvaluationResult.getRuleInfo().getRuleName(),
            contextInfo.getContextName() + ":" + contextInfo.getContextId(),
            System.lineSeparator() + gson.toJson(ruleEvaluationResult.getOverrideInfo())
        );
    }

}
