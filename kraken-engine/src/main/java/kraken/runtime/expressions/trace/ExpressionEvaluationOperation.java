/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.expressions.trace;

import java.util.Optional;
import java.util.stream.Collectors;

import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.expression.ExpressionVariableType;
import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace before expression is evaluated.
 * Describes expression and referenced data contexts.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class ExpressionEvaluationOperation implements VoidOperation {

    private final CompiledExpression expression;

    private final DataContext dataContext;

    public ExpressionEvaluationOperation(CompiledExpression expression, DataContext dataContext) {
        this.expression = expression;
        this.dataContext = dataContext;
    }

    @Override
    public String describe() {
        var template = "Evaluating expression '%s'.%s";
        var ccrTemplate = " Cross context references: %s";

        var ccrDescription = describeCrossContexts();

        return String.format(template,
            expression.getExpressionString(),
            ccrDescription.isEmpty()
                ? ""
                : String.format(ccrTemplate, ccrDescription));
    }

    private String describeCrossContexts() {
        var expressionVars = expression.getExpressionVariables().stream()
            .filter(expressionVariable -> expressionVariable.getType() == ExpressionVariableType.CROSS_CONTEXT)
            .collect(Collectors.toList());

        if (expressionVars.isEmpty()) {
            return "";
        }

        return System.lineSeparator() + expressionVars.stream()
            .map(expVar -> Optional.ofNullable(dataContext.getExternalReferences().get(expVar.getName()))
                .map(extDataRef -> extDataRef.getName() + ": [ " + extDataRef.getDataContexts().stream()
                    .map(dataContext -> dataContext.getContextName() + ":" + dataContext.getContextId())
                    .collect(Collectors.joining(", ")) + " ]")
                .orElse("No cross contexts resolved for '" + expVar.getName() + "'"))
            .collect(Collectors.joining("," + System.lineSeparator()));
    }

}
