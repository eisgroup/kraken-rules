/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.engine.handlers.trace;

import kraken.runtime.model.rule.payload.validation.AssertionPayload;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace after assert payload evaluation is completed.
 * Describes payload evaluation details and state.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class AssertionPayloadEvaluatedOperation implements VoidOperation {

    private final AssertionPayload assertionPayload;

    private final Object evaluationState;

    public AssertionPayloadEvaluatedOperation(AssertionPayload assertionPayload,
                                              Object evaluationState) {
        this.assertionPayload = assertionPayload;
        this.evaluationState = evaluationState;
    }

    @Override
    public String describe() {
        var template = "Evaluated '%s' expression '%s' to %s.";

        return String.format(template,
            assertionPayload.getType().getTypeName(),
            assertionPayload.getAssertionExpression().getExpressionString(),
            evaluationState);
    }

}
