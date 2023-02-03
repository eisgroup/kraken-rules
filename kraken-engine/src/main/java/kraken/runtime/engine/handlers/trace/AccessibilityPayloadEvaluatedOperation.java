/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.engine.handlers.trace;

import kraken.runtime.model.rule.payload.ui.AccessibilityPayload;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace after accessibility payload evaluation is completed.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class AccessibilityPayloadEvaluatedOperation implements VoidOperation {

    private final AccessibilityPayload accessibilityPayload;

    public AccessibilityPayloadEvaluatedOperation(AccessibilityPayload accessibilityPayload) {
        this.accessibilityPayload = accessibilityPayload;
    }

    @Override
    public String describe() {
        var template = "Evaluated '%s'. The field is set to be disabled.";

        return String.format(template, accessibilityPayload.getType().getTypeName());
    }

}
