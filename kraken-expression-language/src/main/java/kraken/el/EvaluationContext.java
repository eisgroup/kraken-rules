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
package kraken.el;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import kraken.el.functionregistry.FunctionInvoker;
import kraken.el.interpreter.evaluator.InterpretingExpressionEvaluator;
import kraken.el.scope.type.Type;

/**
 * Contains session specific data for expression evaluation.
 *
 * @author mulevicius
 */
public class EvaluationContext {

    private static final TypeProvider DEFAULT_TYPE_PROVIDER = new TypeProvider() {
        @Override
        public String getTypeOf(Object object) {
            return Type.ANY.getName();
        }

        @Override
        public Collection<String> getInheritedTypesOf(Object object) {
            return List.of();
        }
    };

    private static final FunctionInvoker DEFAULT_FUNCTION_INVOKER = new FunctionInvoker(
        Map.of(),
        new InterpretingExpressionEvaluator(new ExpressionLanguageConfiguration(false, true)),
        DEFAULT_TYPE_PROVIDER
    );

    private final Object dataObject;
    private final Map<String, Object> variables;
    private final TypeProvider typeProvider;
    private final FunctionInvoker functionInvoker;

    public EvaluationContext() {
        this(null);
    }

    public EvaluationContext(@Nullable Object dataObject) {
        this(dataObject, Collections.emptyMap(), DEFAULT_TYPE_PROVIDER, DEFAULT_FUNCTION_INVOKER);
    }

    public EvaluationContext(@Nullable Object dataObject,
                             Map<String, Object> variables,
                             TypeProvider typeProvider,
                             FunctionInvoker functionInvoker) {
        this.dataObject = dataObject;
        this.variables = Objects.requireNonNull(variables);
        this.typeProvider = Objects.requireNonNull(typeProvider);
        this.functionInvoker = Objects.requireNonNull(functionInvoker);
    }

    @Nullable
    public Object getDataObject() {
        return dataObject;
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public TypeProvider getTypeProvider() {
        return typeProvider;
    }

    public FunctionInvoker getFunctionInvoker() {
        return functionInvoker;
    }
}
