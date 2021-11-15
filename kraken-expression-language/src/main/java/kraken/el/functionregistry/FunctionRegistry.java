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

import static kraken.el.scope.type.Type.toArrayToken;
import static kraken.el.scope.type.Type.toGenericsToken;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import javax.money.MonetaryAmount;

import kraken.el.scope.type.Type;

/**
 * Loads all functions available in {@link FunctionLibrary} implementations.
 * Considers only static methods that are annotated with {@link ExpressionFunction}.
 *
 * @author mulevicius
 */
public final class FunctionRegistry {

    private static final ReadWriteLock functionLock = new ReentrantReadWriteLock();

    private static Map<FunctionHeader, FunctionDefinition> ALL_FUNCTIONS = new HashMap<>();
    private static Map<String, Map<FunctionHeader, FunctionDefinition>> FUNCTIONS_BY_EXP_TARGET = new HashMap<>();

    static {
        reload();
    }

    private FunctionRegistry() {
    }

    /**
     * A map containing all native functions applicable for given expression target. If no native functions are
     * applicable an empty map is returned.
     *
     * @param expressionTarget Expression target.
     * @return Applicable native functions for expression target.
     */
    public static Map<FunctionHeader, FunctionDefinition> getNativeFunctions(String expressionTarget) {
        functionLock.readLock().lock();

        try {
            return FUNCTIONS_BY_EXP_TARGET.computeIfAbsent(expressionTarget, FunctionRegistry::getFunctionsForExpressionTarget)
                .entrySet()
                .stream()
                .filter(e -> e.getValue().isNativeFunction())
                .collect(Collectors.toMap(e -> e.getKey(), e-> e.getValue()));
        } finally {
            functionLock.readLock().unlock();
        }
    }

    /**
     * A map containing all functions applicable for given expression target. If no functions are
     * applicable an empty map is returned.
     *
     * @param expressionTarget Expression target.
     * @return Applicable functions for expression target.
     */
    public static Map<FunctionHeader, FunctionDefinition> getFunctions(String expressionTarget) {
        functionLock.readLock().lock();

        try {
            return FUNCTIONS_BY_EXP_TARGET.computeIfAbsent(expressionTarget, FunctionRegistry::getFunctionsForExpressionTarget);
        } finally {
            functionLock.readLock().unlock();
        }
    }

    private static Map<FunctionHeader, FunctionDefinition> getFunctionsForExpressionTarget(String expressionTarget) {
        return ALL_FUNCTIONS.entrySet().stream()
                .filter(e -> e.getValue().getExpressionTargets().isEmpty() || e.getValue().getExpressionTargets().contains(expressionTarget))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Reloads function registry by clearing previously loaded functions and re-loading all
     * {@code FunctionLibrary} implementations.
     */
    public static void reload() {
        functionLock.writeLock().lock();

        try {
            ALL_FUNCTIONS = new HashMap<>();
            FUNCTIONS_BY_EXP_TARGET = new HashMap<>();

            ServiceLoader.load(FunctionLibrary.class).forEach(FunctionRegistry::importFunctionLibrary);
        } finally {
            functionLock.writeLock().unlock();
        }
    }

    /**
     * A map containing all registered functions for all expression targets.
     *
     * @return All registered functions.
     */
    public static Map<FunctionHeader, FunctionDefinition> getFunctions() {
        return ALL_FUNCTIONS;
    }

    private static void importFunctionLibrary(FunctionLibrary functionLibrary) {
        for (Method m : functionLibrary.getClass().getMethods()) {
            ExpressionFunction expressionFunction = m.getAnnotation(ExpressionFunction.class);

            if (expressionFunction != null) {
                String name = expressionFunction.value().isEmpty()
                        ? m.getName()
                        : expressionFunction.value();

                validateFunction(m, name);

                boolean nativeFunction = isNativeFunction(functionLibrary, m);
                Set<String> expressionTargets = determineExpressionTargets(functionLibrary, m);
                String returnType = getReturnType(m);
                List<String> parameterTypes = Arrays.stream(m.getParameters())
                    .map(FunctionRegistry::getParameterType)
                    .collect(Collectors.toList());

                FunctionDefinition functionDefinition = new FunctionDefinition(
                    name,
                    returnType,
                    parameterTypes,
                    m,
                    expressionTargets,
                    nativeFunction
                );
                FunctionHeader functionHeader = new FunctionHeader(
                    functionDefinition.getFunctionName(),
                    functionDefinition.getParameterCount()
                );

                if (ALL_FUNCTIONS.containsKey(functionHeader)) {
                    throw new IllegalStateException("Error while initializing Kraken Expression Language. " +
                            "There exists multiple functions with same name: '" + name + "' and parameter count: " + functionHeader.getParameterCount());
                }

                ALL_FUNCTIONS.put(functionHeader, functionDefinition);
            }
        }
    }

    private static String getReturnType(Method method) {
        ReturnType returnType = method.getAnnotation(ReturnType.class);
        if(returnType != null) {
            return returnType.value();
        }
        return fromJavaType(method.getGenericReturnType());
    }

    private static String getParameterType(Parameter parameter) {
        ParameterType parameterType = parameter.getAnnotation(ParameterType.class);
        if(parameterType != null) {
            return parameterType.value();
        }
        return fromJavaType(parameter.getParameterizedType());
    }

    private static String fromJavaType(java.lang.reflect.Type type) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;

            if (Number.class.isAssignableFrom(clazz)) {
                return Type.NUMBER.getName();
            } else if (String.class.isAssignableFrom(clazz)) {
                return Type.STRING.getName();
            } else if (MonetaryAmount.class.isAssignableFrom(clazz)) {
                return Type.MONEY.getName();
            } else if (Boolean.class.isAssignableFrom(clazz)) {
                return Type.BOOLEAN.getName();
            } else if (LocalDate.class.isAssignableFrom(clazz)) {
                return Type.DATE.getName();
            } else if (LocalDateTime.class.isAssignableFrom(clazz)) {
                return Type.DATETIME.getName();
            } else if (Collection.class.isAssignableFrom(clazz)) {
                return toArrayToken(Type.ANY.getName());
            } else if(Map.class.isAssignableFrom(clazz)) {
                return Type.ANY.getName();
            } else if(Object.class.equals(clazz)) {
                return Type.ANY.getName();
            }

            return clazz.getSimpleName();
        }

        if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) type;

            if (((TypeVariable) type).getBounds()[0].equals(Object.class)) {
                return toGenericsToken(type.getTypeName());
            } else {
                return fromJavaType(typeVariable.getBounds()[0]);
            }
        }

        if (type instanceof WildcardType) {
            if (((WildcardType) type).getUpperBounds().length > 0) {
                return fromJavaType(((WildcardType) type).getUpperBounds()[0]);
            }
            if (((WildcardType) type).getLowerBounds().length > 0) {
                return fromJavaType(((WildcardType) type).getLowerBounds()[0]);
            }
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;

            if (pType.getRawType() instanceof Class<?> && Collection.class.isAssignableFrom((Class<?>) pType.getRawType())) {
                return toArrayToken(fromJavaType(pType.getActualTypeArguments()[0]));
            }
        }

        return type.getTypeName();
    }

    private static boolean isNativeFunction(FunctionLibrary functionLibrary, Method m) {
        Native globalNative = functionLibrary.getClass().getAnnotation(Native.class);
        Native localNative = m.getAnnotation(Native.class);

        return globalNative != null || localNative != null;
    }

    private static Set<String> determineExpressionTargets(FunctionLibrary functionLibrary, Method m) {
        ExpressionTarget globalExpressionTarget = functionLibrary.getClass().getAnnotation(ExpressionTarget.class);
        ExpressionTarget localExpressionTarget = m.getAnnotation(ExpressionTarget.class);

        Set<String> expressionTargets = new HashSet<>();
        if(globalExpressionTarget != null) {
            expressionTargets.addAll(Arrays.asList(globalExpressionTarget.value()));
        }
        if(localExpressionTarget != null) {
            expressionTargets.addAll(Arrays.asList(localExpressionTarget.value()));
        }
        return expressionTargets;
    }

    private static void validateFunction(Method m, String name) {
        if(!Modifier.isStatic(m.getModifiers())) {
            throw new IllegalStateException("Error while initializing Kraken Expression Language. " +
                    "Cannot import function '" + name + "' into Kraken Expression Language because it is not static.");
        }
    }

}
