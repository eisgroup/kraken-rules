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
package kraken.el.functionregistry;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;

import kraken.el.EvaluationContext;
import kraken.el.ExpressionEvaluationException;
import kraken.el.TypeProvider;
import kraken.el.coercer.KelCoercer;
import kraken.el.coercer.KelCoercionException;
import kraken.el.interpreter.evaluator.InterpretingExpressionEvaluator;

/**
 *
 * Invokes functions registered in {@link FunctionRegistry}.
 *
 * @author mulevicius
 */
public class FunctionInvoker {

    private static final String ERROR_INVOKING_FUNCTION = "Error while invoking function ''{0}''";
    private static final String ERROR_INVOKING_FUNCTION_WITH_REASON = "Error while invoking function {0}. Reason: {1}";
    private static final String FUNCTION_DOES_NOT_EXIST = "Cannot invoke function ''{0}'' with {1} parameters because it does not exist.";
    private static final String INVALID_PARAMETER_TYPE = "Cannot invoke function ''{0}'' because type of parameter at index {1} does not match expected type. Expected {2} but found {3}.";
    private static final String INVALID_PARAMETER_NULL = "Cannot invoke function ''{0}'' because parameter at index {1} is null.";

    private final Map<FunctionHeader, KelFunction> kelFunctions;
    private final InterpretingExpressionEvaluator functionEvaluator;
    private final TypeProvider typeProvider;
    private final String expressionTarget;
    private final ZoneId zoneId;

    public FunctionInvoker(Map<FunctionHeader, KelFunction> kelFunctions,
                           InterpretingExpressionEvaluator functionEvaluator,
                           TypeProvider typeProvider) {
        this(kelFunctions, functionEvaluator, typeProvider, null, ZoneId.systemDefault());
    }

    public FunctionInvoker(Map<FunctionHeader, KelFunction> kelFunctions,
                           InterpretingExpressionEvaluator functionEvaluator,
                           TypeProvider typeProvider,
                           String expressionTarget,
                           ZoneId zoneId) {
        this.kelFunctions = kelFunctions;
        this.functionEvaluator = functionEvaluator;
        this.typeProvider = typeProvider;
        this.expressionTarget = expressionTarget;
        this.zoneId = zoneId;
    }

    public Object invoke(String functionName, Object[] arguments) {
        return invoke(functionName, arguments, this::invokeJavaFunction);
    }

    public Object invokeWithIteration(String functionName, Object[] arguments) {
        return invoke(functionName, arguments, this::invokeJavaFunctionWithIteration);
    }

    private Object invoke(String functionName, Object[] arguments,
                          BiFunction<JavaFunction, FunctionContext, Object> javaFunctionInvoker) {
        FunctionHeader functionHeader = new FunctionHeader(functionName, arguments.length);
        FunctionContext functionContext = new FunctionContext(arguments);

        if(kelFunctions.containsKey(functionHeader)) {
            KelFunction kelFunction = kelFunctions.get(functionHeader);
            return invokeKelFunction(kelFunction, functionContext);
        }

        Map<FunctionHeader, JavaFunction> javaFunctions = expressionTarget != null
            ? FunctionRegistry.getFunctions(expressionTarget)
            : FunctionRegistry.getFunctions();

        if(javaFunctions.containsKey(functionHeader)) {
            JavaFunction javaFunction = javaFunctions.get(functionHeader);
            return javaFunctionInvoker.apply(javaFunction, functionContext);
        }

        throw new FunctionInvocationException(functionDoesNotExistMessage(functionHeader));
    }

    private String functionDoesNotExistMessage(FunctionHeader functionHeader) {
        return MessageFormat.format(FUNCTION_DOES_NOT_EXIST, functionHeader.getName(), functionHeader.getParameterCount());
    }

    private Object invokeKelFunction(KelFunction function, FunctionContext functionContext) {
        Map<String, Object> argumentContext = new HashMap<>();
        for(int i = 0; i < function.getParameters().size(); i++) {
            argumentContext.put(function.getParameters().get(i).getName(), functionContext.getArguments()[i]);
        }
        EvaluationContext evaluationContext = new EvaluationContext(argumentContext, Map.of(), typeProvider, this, zoneId);
        return functionEvaluator.evaluate(function.getBody(), evaluationContext);
    }

    private Object invokeJavaFunctionWithIteration(JavaFunction function, FunctionContext functionContext) {
        int iterableParameter = findFirstIterableParameter(function, functionContext);
        if(iterableParameter > -1) {
            return invokeJavaFunctionWithIteration(function, functionContext, iterableParameter);
        }
        return invokeJavaFunction(function, functionContext);
    }

    private Object invokeJavaFunction(JavaFunction function, FunctionContext functionContext) {
        Object[] parameters = validateAndCoerceParameters(function, functionContext.getArguments());
        return doInvokeMethod(function, parameters);
    }

    private Object doInvokeMethod(JavaFunction function, Object[] parameters) {
        try {
            return function.getMethod().invoke(null, parameters);
        } catch (Exception e) {
            if (e.getCause() instanceof ExpressionEvaluationException) {
                throw (ExpressionEvaluationException) e.getCause();
            }

            String message = resolveMessage(e, function);
            throw new FunctionInvocationException(message, e);
        }
    }

    private String resolveMessage(Exception error, JavaFunction function) {
        if (error instanceof InvocationTargetException && error.getCause() instanceof RuntimeException) {
            var underlyingMessage = error.getCause().getMessage();

            if (StringUtils.isNotEmpty(underlyingMessage)) {
                return MessageFormat.format(
                    ERROR_INVOKING_FUNCTION_WITH_REASON,
                    function.getFunctionName(),
                    underlyingMessage);
            }
        }

        return MessageFormat.format(ERROR_INVOKING_FUNCTION, function.getFunctionName());
    }

    private Object[] validateAndCoerceParameters(JavaFunction function, Object[] parameters) {
        for(int i = 0; i < parameters.length; i++) {
            var parameter = function.getMethod().getParameters()[i];
            NotNull notNull = parameter.getAnnotation(NotNull.class);
            if(notNull != null && parameters[i] == null) {
                String message = MessageFormat.format(INVALID_PARAMETER_NULL, function.getFunctionName(), i);
                throw new ExpressionEvaluationException(message);
            }
            parameters[i] = coerced(parameters[i], parameter.getParameterizedType(), function, i);
        }
        return parameters;
    }

    private Object coerced(Object parameter, Type toType, JavaFunction function, int i) {
        try {
            return KelCoercer.coerce(parameter, toType);
        } catch (KelCoercionException e) {
            String message = MessageFormat.format(INVALID_PARAMETER_TYPE, function.getFunctionName(), i, toType,
                parameter.getClass());
            throw new ExpressionEvaluationException(message, e);
        }
    }

    private Object invokeJavaFunctionWithIteration(JavaFunction function, FunctionContext functionContext, int iterableParameter) {
        Collection result = new ArrayList();
        Collection c = (Collection) functionContext.getArguments()[iterableParameter];
        for(Object item : c) {
            Object[] argumentsWithoutCollection = functionContext.getArguments().clone();
            argumentsWithoutCollection[iterableParameter] = item;
            result.add(invokeJavaFunction(function, new FunctionContext(argumentsWithoutCollection)));
        }
        return result;
    }

    private int findFirstIterableParameter(JavaFunction function, FunctionContext functionContext) {
        for(int i = 0; i < functionContext.getArguments().length; i++) {
            Parameter parameterDefinition = function.getMethod().getParameters()[i];
            Object parameter = functionContext.getArguments()[i];
            if(isParameterIterable(parameterDefinition)
                    && !Collection.class.isAssignableFrom(parameterDefinition.getType())
                    && parameter instanceof Collection) {
                return i;
            }
        }
        return -1;
    }

    private boolean isParameterIterable(Parameter parameterDefinition) {
        return !parameterDefinition.isAnnotationPresent(Iterable.class)
                || parameterDefinition.getAnnotation(Iterable.class).value();
    }

    private static class FunctionContext {

        private final Object[] arguments;

        FunctionContext(Object[] arguments) {
            this.arguments = arguments;
        }

        Object[] getArguments() {
            return arguments;
        }
    }
}
