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

import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.tracer.VoidOperation;

/**
 * @author Mindaugas Ulevicius
 */
public class AssertionExpressionEvaluationOperation implements VoidOperation {

    private final CompiledExpression expression;
    private final DataContext dataContext;

    public AssertionExpressionEvaluationOperation(CompiledExpression expression, DataContext dataContext) {
        this.expression = expression;
        this.dataContext = dataContext;
    }

    @Override
    public String describe() {
        return ExpressionEvaluationMessageFormatter.format("assertion", expression, dataContext);
    }

}
