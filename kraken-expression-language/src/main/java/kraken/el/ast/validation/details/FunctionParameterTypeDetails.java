/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other
 * media without EIS Group prior written consent.
 */
package kraken.el.ast.validation.details;

import kraken.el.ast.Expression;
import kraken.el.scope.type.Type;

/**
 * Details to be added to {@code AstError} when {@code Function} validation fails
 * due to type incompatibility of expression and function type parameter.
 *
 * @author Tomas Dapkunas
 * @since 1.29.0
 */
public final class FunctionParameterTypeDetails extends AstDetails {

    private final Expression functionExpression;
    private final Type functionParameterType;

    public FunctionParameterTypeDetails(Expression functionExpression, Type functionParameterType) {
        super(AstDetailsType.FUNCTION_TYPE_ERROR);
        this.functionExpression = functionExpression;
        this.functionParameterType = functionParameterType;
    }

    public Expression getFunctionExpression() {
        return functionExpression;
    }

    public Type getFunctionParameterType() {
        return functionParameterType;
    }

}
