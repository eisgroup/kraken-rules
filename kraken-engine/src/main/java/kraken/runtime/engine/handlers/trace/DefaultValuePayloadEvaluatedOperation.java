/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.engine.handlers.trace;

import kraken.runtime.utils.TemplateParameterRenderer;
import kraken.runtime.model.rule.payload.derive.DefaultValuePayload;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace after default value payload evaluation is completed.
 * Describes payload evaluation details.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class DefaultValuePayloadEvaluatedOperation implements VoidOperation {

    private final DefaultValuePayload defaultValuePayload;

    private final Object originalValue;
    private final Object updatedValue;

    public DefaultValuePayloadEvaluatedOperation(DefaultValuePayload defaultValuePayload,
                                                 Object originalValue,
                                                 Object updatedValue) {
        this.defaultValuePayload = defaultValuePayload;
        this.originalValue = originalValue;
        this.updatedValue = updatedValue;
    }

    @Override
    public String describe() {
        var template = "Evaluated '%s'. Before value: '%s'. After value: '%s'.";
        var before = originalValue == null ? "NULL" : TemplateParameterRenderer.render(originalValue);
        var after = updatedValue == null ? "NULL" : TemplateParameterRenderer.render(updatedValue);

        return String.format(template, defaultValuePayload.getType().getTypeName(), before, after);
    }

}
