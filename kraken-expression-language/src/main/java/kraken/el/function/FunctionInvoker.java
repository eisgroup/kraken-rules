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
package kraken.el.function;

import kraken.el.ExpressionEvaluationException;
import kraken.el.functionregistry.Iterable;
import kraken.el.functionregistry.*;
import org.apache.commons.lang3.ClassUtils;

import javax.money.MonetaryAmount;
import java.lang.reflect.Parameter;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * Invokes functions registered in {@link FunctionRegistry}.
 *
 * @author mulevicius
 */
public class FunctionInvoker {

    private static final String ERROR_INVOKING_FUNCTION = "Error while invoking function {0}";
    private static final String FUNCTION_DOES_NOT_EXIST = "Cannot invoke function {0} with {1} parameters because it does not exist.";
    private static final String INVALID_PARAMETER_TYPE = "Cannot invoke function {0} because type of parameter at index {1} does not match expected type. Expected {2} but found {3}.";
    private static final String INVALID_PARAMETER_NULL = "Cannot invoke function {0} because parameter at index {1} is null.";

    public static Object invoke(String functionName, Object[] parameters) {
        FunctionContext functionContext = new FunctionContext(parameters);
        FunctionDefinition function = resolveFunctionDefinitionOrThrow(functionName, parameters);

        return invokeFunction(function, functionContext);
    }

    public static Object invokeWithIteration(String functionName, Object[] parameters) {
        FunctionContext functionContext = new FunctionContext(parameters);
        FunctionDefinition function = resolveFunctionDefinitionOrThrow(functionName, parameters);

        int iterableParameter = findFirstIterableParameter(function, functionContext);
        if(iterableParameter > -1) {
            return invokeFunctionWithIteration(function, functionContext, iterableParameter);
        }
        return invokeFunction(function, functionContext);
    }

    private static FunctionDefinition resolveFunctionDefinitionOrThrow(String functionName, Object[] parameters) {
        FunctionHeader functionHeader = new FunctionHeader(functionName, parameters.length);
        FunctionDefinition function = FunctionRegistry.getFunctions().get(functionHeader);
        throwIfFunctionDoesNotExist(functionHeader, function);
        return function;
    }

    private static Object invokeFunction(FunctionDefinition function, FunctionContext functionContext) {
        Object[] parameters = validateAndCoerceParameters(function, functionContext.getParameters());
        return doInvokeMethod(function, parameters);
    }

    private static Object doInvokeMethod(FunctionDefinition function, Object[] parameters) {
        try {
            return function.getMethod().invoke(null, parameters);
        } catch (Exception e) {
            if (e.getCause() instanceof ExpressionEvaluationException) {
                throw ((ExpressionEvaluationException) e.getCause());
            }
            String message = MessageFormat.format(ERROR_INVOKING_FUNCTION, function.getFunctionName());
            throw new FunctionInvocationException(message, e);
        }
    }

    private static Object[] validateAndCoerceParameters(FunctionDefinition function, Object[] parameters) {
        for(int i = 0; i < parameters.length; i++) {
            NotNull notNull = function.getMethod().getParameters()[i].getAnnotation(NotNull.class);
            if(notNull != null && parameters[i] == null) {
                String message = MessageFormat.format(INVALID_PARAMETER_NULL, function.getFunctionName(), i);
                throw new ExpressionEvaluationException(message);
            }
            Class<?> functionParameterType = function.getMethod().getParameterTypes()[i];
            parameters[i] = coerced(parameters[i], functionParameterType, function, i);
        }
        return parameters;
    }

    private static Object coerced(Object parameter, Class<?> toType, FunctionDefinition function, int i) {
        if(parameter == null) {
            return null;
        }
        Class<?> providedParameterType = parameter.getClass();
        if(ClassUtils.isAssignable(providedParameterType, toType)) {
            return parameter;
        }
        if(MonetaryAmount.class.isAssignableFrom(providedParameterType) && Number.class.isAssignableFrom(toType)) {
            return ((MonetaryAmount) parameter).getNumber().numberValue(BigDecimal.class);
        }
        String message = MessageFormat.format(INVALID_PARAMETER_TYPE, function.getFunctionName(), i, toType, providedParameterType);
        throw new FunctionInvocationException(message);
    }

    private static void throwIfFunctionDoesNotExist(FunctionHeader functionHeader, FunctionDefinition function) {
        if (function == null) {
            String message = MessageFormat.format(FUNCTION_DOES_NOT_EXIST, functionHeader.getName(), functionHeader.getParameterCount());
            throw new FunctionInvocationException(message);
        }
    }

    private static Object invokeFunctionWithIteration(FunctionDefinition function, FunctionContext functionContext, int iterableParameter) {
        Collection result = new ArrayList();
        Collection c = (Collection) functionContext.getParameters()[iterableParameter];
        for(Object item : c) {
            Object[] parametersWithoutCollection = functionContext.getParameters().clone();
            parametersWithoutCollection[iterableParameter] = item;
            result.add(invokeFunction(function, new FunctionContext(parametersWithoutCollection)));
        }
        return result;
    }

    private static int findFirstIterableParameter(FunctionDefinition function, FunctionContext functionContext) {
        for(int i = 0; i < functionContext.getParameters().length; i++) {
            Parameter parameterDefinition = function.getMethod().getParameters()[i];
            Object parameter = functionContext.getParameters()[i];
            if(isParameterIterable(parameterDefinition)
                    && !Collection.class.isAssignableFrom(parameterDefinition.getType())
                    && parameter instanceof Collection) {
                return i;
            }
        }
        return -1;
    }

    private static boolean isParameterIterable(Parameter parameterDefinition) {
        return !parameterDefinition.isAnnotationPresent(Iterable.class)
                || parameterDefinition.getAnnotation(Iterable.class).value();
    }

    private static class FunctionContext {

        private final Object[] parameters;

        FunctionContext(Object[] parameters) {
            this.parameters = parameters;
        }

        Object[] getParameters() {
            return parameters;
        }
    }
}
