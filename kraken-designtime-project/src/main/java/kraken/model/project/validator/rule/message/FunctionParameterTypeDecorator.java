/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other
 * media without EIS Group prior written consent.
 */
package kraken.model.project.validator.rule.message;

import kraken.el.ast.validation.AstMessage;
import kraken.el.ast.validation.details.AstDetailsType;
import kraken.el.ast.validation.details.FunctionParameterTypeDetails;
import kraken.el.scope.type.ArrayType;
import kraken.model.Rule;
import kraken.model.project.KrakenProject;
import kraken.model.project.ccr.CrossContextService;

/**
 * Implementation which decorates original error message with additional details
 * when parameter value type is a CCR and cardinality mismatches function signature parameter type
 *
 * @author Tomas Dapkunas
 * @since 1.29.0
 */
public final class FunctionParameterTypeDecorator
    extends AbstractTypeMessageDecorator<FunctionParameterTypeDetails> {

    public FunctionParameterTypeDecorator(KrakenProject krakenProject,
                                          CrossContextService crossContextService) {
        super(krakenProject, crossContextService);
    }

    @Override
    public String decorate(AstMessage message, Rule rule) {
        FunctionParameterTypeDetails details = getDetails(message);

        if (!isSameScalarType(details) || !isDifferentCardinality(details)) {
            return message.getMessage();
        }

        String ccrPathMessage = describeIfExpressionIsSimplePathAndCcr(details.getFunctionExpression(), rule);

        return ccrPathMessage.isEmpty()
            ? message.getMessage()
            : message.getMessage() + ccrPathMessage;
    }

    private boolean isSameScalarType(FunctionParameterTypeDetails details) {
        return details.getFunctionExpression().getEvaluationType().unwrapArrayType()
            .equals(details.getFunctionParameterType().unwrapArrayType());
    }

    private boolean isDifferentCardinality(FunctionParameterTypeDetails details) {
        return isSimplePathAndCcrIsArray(details.getFunctionExpression())
            ^ details.getFunctionParameterType() instanceof ArrayType;
    }

    @Override
    public AstDetailsType getSupportedType() {
        return AstDetailsType.FUNCTION_TYPE_ERROR;
    }

}
