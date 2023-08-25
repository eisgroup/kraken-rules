/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.engine.handlers.trace;

import java.util.stream.Collectors;

import kraken.model.context.Cardinality;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.context.data.DataReference;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.expression.ExpressionVariableType;

/**
 * @author Mindaugas Ulevicius
 */
public class ExpressionEvaluationMessageFormatter {

    public static String format(String type, CompiledExpression expression, DataContext dataContext) {
        String message = "Evaluating " + type + " expression: " + expression.getOriginalExpressionString();
        String ccr = describeCrossContextReferences(expression, dataContext);
        if(!ccr.isEmpty()) {
            message += System.lineSeparator() + ccr;
        }

        return message;
    }

    private static String describeCrossContextReferences(CompiledExpression expression, DataContext dataContext) {
        var expressionVars = expression.getExpressionVariables().stream()
            .filter(expressionVariable -> expressionVariable.getType() == ExpressionVariableType.CROSS_CONTEXT)
            .collect(Collectors.toList());
        if(!expressionVars.isEmpty()) {
            var ccrDescription = expressionVars.stream()
                .map(expVar -> expVar.getName() + "=" + describe(dataContext.getDataContextReferences().get(expVar.getName())))
                .collect(Collectors.joining(System.lineSeparator()));
            return "Cross context references:" + System.lineSeparator() + ccrDescription;
        }
        return "";
    }

    private static String describe(DataReference ref) {
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
