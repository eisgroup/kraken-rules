/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.engine.handlers.trace;

import kraken.runtime.utils.TemplateParameterRenderer;
import kraken.runtime.model.rule.payload.validation.RegExpPayload;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace after regular expression payload evaluation is completed.
 * Describes payload evaluation details and state.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class RegExpPayloadEvaluatedOperation implements VoidOperation {

    private final RegExpPayload regExpPayload;
    private final Object fieldValue;
    private final boolean matchResult;

    public RegExpPayloadEvaluatedOperation(RegExpPayload regExpPayload,
                                           Object fieldValue,
                                           boolean matchResult) {
        this.regExpPayload = regExpPayload;
        this.fieldValue = fieldValue;
        this.matchResult = matchResult;
    }

    @Override
    public String describe() {
        var template = "Evaluated '%s' to %s. Value '%s' %s regular expression '%s'.";
        var value = TemplateParameterRenderer.render(fieldValue);

        return String.format(template,
            regExpPayload.getType().getTypeName(),
            matchResult,
            value.isEmpty() ? "NULL" : value,
            matchResult ? "matches" : "does not match",
            regExpPayload.getRegExp());
    }

}
