/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.engine.result.reducers.validation.trace;

import com.google.gson.Gson;

import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.tracer.Operation;
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

    private static final String TEMPLATE = "Rule '%s' validation result is overridden. "
        + "Original validation result will be ignored. Override info: %s";

    private final Gson gson = GsonUtils.gson();
    private final RuleEvaluationResult ruleEvaluationResult;

    public RuleResultOverriddenOperation(RuleEvaluationResult ruleEvaluationResult) {
        this.ruleEvaluationResult = ruleEvaluationResult;
    }

    @Override
    public String describe() {
        return String.format(TEMPLATE,
            ruleEvaluationResult.getRuleInfo().getRuleName(),
            gson.toJson(ruleEvaluationResult.getOverrideInfo()));
    }

}
