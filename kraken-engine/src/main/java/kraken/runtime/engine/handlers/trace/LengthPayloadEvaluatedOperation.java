/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.engine.handlers.trace;

import kraken.runtime.utils.TemplateParameterRenderer;
import kraken.runtime.model.rule.payload.validation.LengthPayload;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace after length payload evaluation is completed.
 * Describes payload evaluation details and state.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class LengthPayloadEvaluatedOperation implements VoidOperation {

    private final LengthPayload lengthPayload;

    private final Object fieldValue;
    private final boolean evaluationState;

    public LengthPayloadEvaluatedOperation(LengthPayload lengthPayload,
                                           Object fieldValue,
                                           boolean evaluationState) {
        this.lengthPayload = lengthPayload;
        this.fieldValue = fieldValue;
        this.evaluationState = evaluationState;
    }

    @Override
    public String describe() {
        var template = "Evaluated '%s' to %s. Expected length '%s'. Actual length '%s'";
        var value = TemplateParameterRenderer.render(fieldValue);

        return String.format(template,
            lengthPayload.getType().getTypeName(),
            evaluationState,
            lengthPayload.getLength(),
            value.length());
    }

}
