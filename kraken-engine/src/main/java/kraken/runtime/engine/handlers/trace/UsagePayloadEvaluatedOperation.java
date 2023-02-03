/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.engine.handlers.trace;

import kraken.runtime.utils.TemplateParameterRenderer;
import kraken.runtime.model.rule.payload.validation.UsagePayload;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace after usage payload evaluation is completed.
 * Describes payload evaluation details, field and state.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class UsagePayloadEvaluatedOperation implements VoidOperation {

    private final UsagePayload usagePayload;

    private final Object fieldValue;
    private final boolean evaluationState;

    public UsagePayloadEvaluatedOperation(UsagePayload usagePayload,
                                          Object fieldValue,
                                          boolean evaluationState) {
        this.usagePayload = usagePayload;
        this.fieldValue = fieldValue;
        this.evaluationState = evaluationState;
    }

    @Override
    public String describe() {
        var value = TemplateParameterRenderer.render(fieldValue);
        var template = "Evaluated '%s' to %s. %s";

        switch (usagePayload.getUsageType()) {
            case mandatory:
                var successTemplate = "Mandatory field has value '%s'.";
                var failureTemplate = "Mandatory field value is missing.";

                return String.format(template,
                    usagePayload.getType().getTypeName(),
                    evaluationState,
                    evaluationState ? String.format(successTemplate, value) : failureTemplate);
            case mustBeEmpty:
                var successTemplateEmpty = "Empty field has no value set.";
                var failureTemplateEmpty = "Empty field has value '%s'.";

                return String.format(template,
                    usagePayload.getType().getTypeName(),
                    evaluationState,
                    evaluationState ? successTemplateEmpty : String.format(failureTemplateEmpty, value));
            default:
                return String.format(template, usagePayload.getType().getTypeName(), evaluationState, "");
        }
    }
}
