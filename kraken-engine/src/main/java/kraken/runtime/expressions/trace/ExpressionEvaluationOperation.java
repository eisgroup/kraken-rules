/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.expressions.trace;

import java.util.stream.Collectors;

import kraken.model.context.Cardinality;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.context.data.DataReference;
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
            .map(expVar -> expVar.getName() + "=" + describe(dataContext.getDataContextReferences().get(expVar.getName())))
            .collect(Collectors.joining("," + System.lineSeparator()));
    }

    private String describe(DataReference ref) {
        if(ref == null) {
            return "null";
        }
        if(ref.getCardinality() == Cardinality.SINGLE) {
            return ref.getDataContext() == null ? "null" : ref.getDataContext().getIdString();
        } else {
            return ref.getDataContexts().stream()
                .map(DataContext::getIdString)
                .collect(Collectors.joining(",", "[", "]"));
        }
    }

}
