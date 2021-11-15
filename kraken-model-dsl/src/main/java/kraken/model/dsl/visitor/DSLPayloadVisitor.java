/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl.visitor;

import kraken.el.ast.builder.Literals;
import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.KrakenDSL;
import kraken.model.dsl.model.*;

import static java.util.Objects.nonNull;
import static kraken.el.ast.builder.Literals.escape;
import static kraken.el.ast.builder.Literals.stripQuotes;

/**
 * @author mulevicius
 */
public class DSLPayloadVisitor extends KrakenDSLBaseVisitor<DSLPayload> {

    @Override
    public DSLPayload visitSizeRangePayload(KrakenDSL.SizeRangePayloadContext ctx) {
        return new DSLSizeRangePayload(
                code(ctx.payloadMessage()),
                message(ctx.payloadMessage()),
                validationSeverity(ctx.payloadMessage()),
                isOverridable(ctx.override()),
                overrideGroup(ctx.override()),
                Literals.getInteger(ctx.sizeFrom.getText()),
                Literals.getInteger(ctx.sizeTo.getText())
        );
    }

    @Override
    public DSLPayload visitSizePayload(KrakenDSL.SizePayloadContext ctx) {
        return new DSLSizePayload(
                code(ctx.payloadMessage()),
                message(ctx.payloadMessage()),
                validationSeverity(ctx.payloadMessage()),
                isOverridable(ctx.override()),
                overrideGroup(ctx.override()),
                collectionSize(ctx),
                sizeAssertOrientation(ctx)
        );
    }

    private DSLSizeOrientation sizeAssertOrientation(KrakenDSL.SizePayloadContext ctx) {
        if (nonNull(ctx.MAX())) {
            return DSLSizeOrientation.MAX;
        }
        if (nonNull(ctx.MIN())) {
            return DSLSizeOrientation.MIN;
        }
        return DSLSizeOrientation.EQUALS;
    }

    private int collectionSize(KrakenDSL.SizePayloadContext ctx) {
        return Literals.getInteger(ctx.size.getText());
    }

    @Override
    public DSLPayload visitAssertionPayload(KrakenDSL.AssertionPayloadContext ctx) {
        DSLSeverity severity = validationSeverity(ctx.payloadMessage());
        DSLExpression assertionExpression = ExpressionReader.read(ctx.inlineExpression());
        String message = message(ctx.payloadMessage());
        String code = code(ctx.payloadMessage());
        return new DSLAssertionValidationPayload(code, message, severity, assertionExpression, isOverridable(ctx.override()), overrideGroup(ctx.override()));
    }

    @Override
    public DSLPayload visitRegExpPayload(KrakenDSL.RegExpPayloadContext ctx) {
        DSLSeverity severity = validationSeverity(ctx.payloadMessage());
        String regExp = stripQuotes(ctx.regExp.getText());
        String message = message(ctx.payloadMessage());
        String code = code(ctx.payloadMessage());
        return new DSLRegExpValidationPayload(code, message, severity, regExp, isOverridable(ctx.override()), overrideGroup(ctx.override()));
    }

    @Override
    public DSLPayload visitLengthPayload(KrakenDSL.LengthPayloadContext ctx) {
        DSLSeverity severity = validationSeverity(ctx.payloadMessage());
        int length = Integer.parseInt(ctx.length.getText());
        String message = message(ctx.payloadMessage());
        String code = code(ctx.payloadMessage());
        return new DSLLengthValidationPayload(code, message, severity, length, isOverridable(ctx.override()), overrideGroup(ctx.override()));
    }

    @Override
    public DSLPayload visitDefault(KrakenDSL.DefaultContext ctx) {
        DSLExpression defaultValueExpression = ExpressionReader.read(ctx.inlineExpression());
        return new DSLDefaultValuePayload(defaultValueExpression, DSLDefaultingType.DEFAULT);
    }

    @Override
    public DSLPayload visitReset(KrakenDSL.ResetContext ctx) {
        DSLExpression resetValueExpression = ExpressionReader.read(ctx.inlineExpression());
        return new DSLDefaultValuePayload(resetValueExpression, DSLDefaultingType.RESET);
    }

    @Deprecated(since = "1.10.0", forRemoval = true)
    @Override
    public DSLPayload visitResetToNull(KrakenDSL.ResetToNullContext ctx) {
        throw new IllegalStateException("Unsupported Defaulting Rule encountered in DSL: " + ctx.getText()
                + ". Defaulting Rules without expresion are no longer supported"
                + ". Rules should be rewritten from 'Reset' to 'Reset To null'");
    }

    @Override
    public DSLPayload visitAccessibilityPayload(KrakenDSL.AccessibilityPayloadContext ctx) {
        return new DSLAccessibilityPayload(ctx.TODISABLED() != null);
    }

    @Override
    public DSLPayload visitVisibilityPayload(KrakenDSL.VisibilityPayloadContext ctx) {
        return new DSLVisibilityPayload(ctx.TOHIDDEN() != null);
    }

    @Override
    public DSLPayload visitMandatory(KrakenDSL.MandatoryContext ctx) {
        DSLSeverity severity = validationSeverity(ctx.payloadMessage());
        String message = message(ctx.payloadMessage());
        String code = code(ctx.payloadMessage());
        return new DSLUsageValidationPayload(code, message, severity, DSLUsageType.MANDATORY, isOverridable(ctx.override()), overrideGroup(ctx.override()));
    }

    @Override
    public DSLPayload visitEmpty(KrakenDSL.EmptyContext ctx) {
        DSLSeverity severity = validationSeverity(ctx.payloadMessage());
        String message = message(ctx.payloadMessage());
        String code = code(ctx.payloadMessage());
        return new DSLUsageValidationPayload(code, message, severity, DSLUsageType.EMPTY, isOverridable(ctx.override()), overrideGroup(ctx.override()));
    }

    private String code(KrakenDSL.PayloadMessageContext ctx) {
        return ctx != null && ctx.code != null
                ? escape(stripQuotes(ctx.code.getText()))
                : null;
    }

    private String message(KrakenDSL.PayloadMessageContext ctx) {
        return ctx != null && ctx.message != null
                ? escape(stripQuotes(ctx.message.getText()))
                : null;
    }

    private DSLSeverity validationSeverity(KrakenDSL.PayloadMessageContext ctx) {
        if (ctx == null) {
            return DSLSeverity.ERROR;
        }
        if (ctx.INFO() != null) {
            return DSLSeverity.INFO;
        }
        if (ctx.WARN() != null) {
            return DSLSeverity.WARN;
        }
        if (ctx.ERROR() != null) {
            return DSLSeverity.ERROR;
        }
        throw new IllegalStateException("Unsupported DSLSeverity encountered in DSL: " + ctx.getText());
    }

    private boolean isOverridable(KrakenDSL.OverrideContext overrideContext) {
        return nonNull(overrideContext);
    }

    private String overrideGroup(KrakenDSL.OverrideContext overrideContext) {
        return nonNull(overrideContext) && nonNull(overrideContext.group)
                ? escape(stripQuotes(overrideContext.group.getText()))
                : null;
    }

}
