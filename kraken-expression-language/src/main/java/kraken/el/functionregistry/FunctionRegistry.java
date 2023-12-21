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
import static kraken.el.scope.type.Type.toType;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.money.MonetaryAmount;

import kraken.el.functionregistry.documentation.AdditionalInfoReader;
import kraken.el.functionregistry.documentation.ExampleDoc;
import kraken.el.functionregistry.documentation.FunctionDoc;
import kraken.el.functionregistry.documentation.GenericTypeDoc;
import kraken.el.functionregistry.documentation.LibraryDoc;
import kraken.el.functionregistry.documentation.ParameterDoc;
import kraken.el.scope.type.Type;

/**
 * Loads all functions available in {@link FunctionLibrary} implementations.
 * Considers only static methods that are annotated with {@link ExpressionFunction}.
 *
 * @author mulevicius
 */
public final class FunctionRegistry {

    private static final ReadWriteLock functionLock = new ReentrantReadWriteLock();
    private static List<LibraryDoc> LIBRARY_DOCS = new ArrayList<>();
    private static Map<FunctionHeader, JavaFunction> ALL_FUNCTIONS = new HashMap<>();
    private static Map<FunctionHeader, JavaFunction> FUNCTIONS_NO_EXP_TARGET = new HashMap<>();
    private static Map<String, Map<FunctionHeader, JavaFunction>> FUNCTIONS_BY_EXP_TARGET = new HashMap<>();

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
    public static Map<FunctionHeader, JavaFunction> getNativeFunctions(String expressionTarget) {
        functionLock.readLock().lock();

        try {
            return FUNCTIONS_BY_EXP_TARGET.getOrDefault(expressionTarget, FUNCTIONS_NO_EXP_TARGET)
                .entrySet()
                .stream()
                .filter(e -> e.getValue().isNativeFunction())
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
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
    public static Map<FunctionHeader, JavaFunction> getFunctions(String expressionTarget) {
        functionLock.readLock().lock();

        try {
            return FUNCTIONS_BY_EXP_TARGET.getOrDefault(expressionTarget, FUNCTIONS_NO_EXP_TARGET);
        } finally {
            functionLock.readLock().unlock();
        }
    }

    /**
     * Reloads function registry by clearing previously loaded functions and re-loading all
     * {@code FunctionLibrary} implementations.
     */
    public static void reload() {
        functionLock.writeLock().lock();

        try {
            ALL_FUNCTIONS = new HashMap<>();
            FUNCTIONS_NO_EXP_TARGET = new HashMap<>();
            FUNCTIONS_BY_EXP_TARGET = new HashMap<>();
            LIBRARY_DOCS = new ArrayList<>();

            for (FunctionLibrary functionLibrary : ServiceLoader.load(FunctionLibrary.class)) {
                importFunctionLibrary(functionLibrary);
            }

            groupFunctionsByExpTarget();
        } finally {
            functionLock.writeLock().unlock();
        }
    }

    private static void groupFunctionsByExpTarget() {
        FUNCTIONS_NO_EXP_TARGET = ALL_FUNCTIONS.entrySet()
            .stream()
            .filter(entry -> entry.getValue().getExpressionTargets().isEmpty())
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        for (Map.Entry<FunctionHeader, JavaFunction> entry : ALL_FUNCTIONS.entrySet()) {
            for (String expTarget : entry.getValue().getExpressionTargets()) {
                Map<FunctionHeader, JavaFunction> expTargetFunctions = FUNCTIONS_BY_EXP_TARGET.computeIfAbsent(
                    expTarget, key -> new HashMap<>(FUNCTIONS_NO_EXP_TARGET));

                expTargetFunctions.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * @return documentation for functions registered in this registry
     */
    public static Collection<LibraryDoc> getLibraryDocs() {
        return LIBRARY_DOCS;
    }

    /**
     * A map containing all registered functions for all expression targets.
     *
     * @return All registered functions.
     */
    public static Map<FunctionHeader, JavaFunction> getFunctions() {
        return ALL_FUNCTIONS;
    }

    private static void importFunctionLibrary(FunctionLibrary functionLibrary) {
        var functionDocs = new ArrayList<FunctionDoc>();
        for (Method method : functionLibrary.getClass().getMethods()) {
            ExpressionFunction expressionFunction = method.getAnnotation(ExpressionFunction.class);

            if (expressionFunction != null) {
                String name = expressionFunction.value().isEmpty()
                    ? method.getName()
                    : expressionFunction.value();

                validateThatMethodIsStatic(method, name);

                boolean nativeFunction = isNativeFunction(functionLibrary, method);
                Set<String> expressionTargets = determineExpressionTargets(functionLibrary, method);
                String returnType = getReturnType(method);
                List<String> parameterTypes = Arrays.stream(method.getParameters())
                    .map(parameter -> getParameterType(parameter, method))
                    .collect(Collectors.toList());

                List<GenericTypeInfo> genericTypes = introspectGenericTypes(expressionFunction, method);

                JavaFunction javaFunction = new JavaFunction(
                    name,
                    returnType,
                    parameterTypes,
                    method,
                    expressionTargets,
                    nativeFunction,
                    genericTypes
                );
                FunctionHeader functionHeader = javaFunction.header();
                if (ALL_FUNCTIONS.containsKey(functionHeader)) {
                    throw new IllegalStateException("Error while initializing Kraken Expression Language. " +
                        "There exists multiple functions with same name: '" + name + "' and parameter count: "
                        + functionHeader.getParameterCount());
                }
                ALL_FUNCTIONS.put(functionHeader, javaFunction);

                functionDocs.add(createFunctionDoc(javaFunction, method, nativeFunction));
            }
        }
        LIBRARY_DOCS.add(createLibraryDoc(functionLibrary, functionDocs));
    }

    private static List<GenericTypeInfo> introspectGenericTypes(ExpressionFunction expressionFunction, Method method) {
        Map<String, GenericTypeInfo> annotationGenericTypes = Arrays.stream(expressionFunction.genericTypes())
            .map(g -> new GenericTypeInfo(g.generic(), g.bound()))
            .collect(Collectors.toMap(
                GenericTypeInfo::getGeneric,
                g -> g,
                (v1, v2) -> throwDuplicateGenerics(method, v1),
                LinkedHashMap::new
            ));

        Map<String, GenericTypeInfo> nativeGenericTypes = Arrays.stream(method.getTypeParameters())
            .map(t -> toGenericTypeInfo(t, method))
            .collect(Collectors.toMap(
                GenericTypeInfo::getGeneric,
                g -> g,
                // cannot be key clashes because Java guarantees that there no duplicate generic type parameters
                (v1, v2) -> v1,
                LinkedHashMap::new
            ));

        for(GenericTypeInfo nativeGenericType : nativeGenericTypes.values()) {
            var nativeBound = nativeGenericType.getBound();
            var annotationBound = annotationGenericTypes.get(nativeGenericType.getGeneric());
            if(!nativeBound.equals(Type.ANY.getName())
                && annotationBound != null
                && !annotationBound.getBound().equals(nativeBound)) {
                throwDuplicateGenericWithNative(method, nativeGenericType);
            }
        }

        // annotation generic types takes precedence and overrides native generic types
        Map<String, GenericTypeInfo> mergedGenericTypes = new LinkedHashMap<>();
        mergedGenericTypes.putAll(nativeGenericTypes);
        mergedGenericTypes.putAll(annotationGenericTypes);
        return new ArrayList<>(mergedGenericTypes.values());
    }

    private static void throwDuplicateGenericWithNative(Method m, GenericTypeInfo genericTypeInfo) {
        throw new IllegalStateException("Error while initializing Kraken Expression Language. " +
            "Cannot import function '" + m.getName() + "' into Kraken Expression Language "
            + "because method signature defines both native Java "
            + "generic type bound and annotation specific generic type bound for '"
            + genericTypeInfo.getGeneric() + "'. Please use only one generic type bound definition.");
    }

    private static GenericTypeInfo throwDuplicateGenerics(Method m, GenericTypeInfo genericTypeInfo) {
        throw new IllegalArgumentException("Error while initializing Kraken Expression Language. " +
            "Cannot import function '" + m.getName() + "' into Kraken Expression Language "
            + "because method signature defines duplicate generics with the same generic type name: '"
            + genericTypeInfo.getGeneric() + "'. Please remove one of the duplicate generic definition.");
    }

    private static GenericTypeInfo toGenericTypeInfo(TypeVariable<Method> t, Method method) {
        if(t.getBounds().length > 1) {
            throwMultipleBounds(method);
        }
        if(t.getBounds().length == 1) {
            String typeToken = fromJavaType(t.getBounds()[0], method);
            if(toType(typeToken, Map.of()).isGeneric()) {
                throwNestedBounds(method, typeToken);
            }
            return new GenericTypeInfo(t.getName(), typeToken);
        }
        return new GenericTypeInfo(t.getName(), Type.ANY.getName());
    }

    private static void throwMultipleBounds(Method m) {
        throw new IllegalStateException("Error while initializing Kraken Expression Language. " +
            "Cannot import function '" + m.getName() + "' into Kraken Expression Language "
            + "because method signature uses multiple bounds. "
            + "Multiple generic bounds is not supported, use single bound instead.");
    }

    private static void throwNestedBounds(Method m, String typeToken) {
        throw new IllegalStateException("Error while initializing Kraken Expression Language. " +
            "Cannot import function '" + m.getName() + "' into Kraken Expression Language "
            + "because method signature uses nested generic bound: '" + typeToken + "' "
            + "Nested generic bounds is not supported.");
    }

    private static LibraryDoc createLibraryDoc(FunctionLibrary library, List<FunctionDoc> functionDocs) {
        LibraryDocumentation libraryDocumentation = library.getClass().getAnnotation(LibraryDocumentation.class);
        boolean hasDocumentation = libraryDocumentation != null;
        String since = hasDocumentation ? libraryDocumentation.since() : null;
        return new LibraryDoc(
            hasDocumentation ? libraryDocumentation.name() : library.getClass().getSimpleName(),
            hasDocumentation ? libraryDocumentation.description() : null,
            since,
            functionDocs.stream()
                .map(d -> new FunctionDoc(
                    d.getFunctionHeader(),
                    d.getDescription(),
                    d.getAdditionalInfo(),
                    d.getSince() != null && !d.getSince().equals("") ? d.getSince() : since,
                    d.getExamples(),
                    d.getParameters(),
                    d.getReturnType(),
                    d.getThrowsError(),
                    d.getGenericTypes()))
                .collect(Collectors.toList())
        );
    }

    private static FunctionDoc createFunctionDoc(JavaFunction javaFunction, Method method, boolean isNative) {
        Supplier<List<ParameterDoc>> parameters = () -> {
            java.lang.reflect.Parameter[] methodParameters = method.getParameters();
            ArrayList<ParameterDoc> parameterDocs = new ArrayList<>();
            for (int i = 0; i < methodParameters.length; i++) {
                ParameterDocumentation parameterDocumentation = methodParameters[i]
                    .getAnnotation(ParameterDocumentation.class);
                String parameterName =
                    parameterDocumentation != null ? parameterDocumentation.name() : methodParameters[i].getName();
                Pattern pattern = Pattern.compile("[A-Za-z_]+[A-Za-z0-9_]");
                if (!pattern.matcher(parameterName).matches()) {
                    throw new IllegalArgumentException(
                        "parameter name " + parameterName + " cannot contain spaces and special chars. "
                            + "Read more in ParameterDocumentation javadocs");
                }

                parameterDocs.add(
                    new ParameterDoc(
                        parameterName,
                        javaFunction.getParameterTypes().get(i),
                        parameterDocumentation != null
                            ? emptyToNull(parameterDocumentation.description())
                            : null
                    )
                );
            }
            return parameterDocs;
        };

        FunctionDocumentation doc = method.getAnnotation(FunctionDocumentation.class);
        if (isNative && doc == null) {
            throw new IllegalStateException(
                String.format("Native function must be annotated with @FunctionDocumentation. Method: %s", method.getName()));
        }
        FunctionHeader functionHeader = javaFunction.header();
        return new FunctionDoc(
            functionHeader,
            doc != null ? doc.description() : null,
            AdditionalInfoReader.read(functionHeader),
            doc != null
                ? emptyToNull(doc.since())
                : null,
            doc != null
                ? Arrays.stream(doc.example())
                .map(e -> new ExampleDoc(e.value(), emptyToNull(e.result()), e.validCall()))
                .collect(Collectors.toList())
                : List.of(),
            parameters.get(),
            javaFunction.getReturnType(),
            doc != null
                ? emptyToNull(doc.throwsError())
                : null,
            javaFunction.getGenericTypes().stream()
                .map(g -> new GenericTypeDoc(g.getGeneric(), g.getBound()))
                .collect(Collectors.toList())
        );
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private static String getReturnType(Method method) {
        ReturnType returnType = method.getAnnotation(ReturnType.class);
        if (returnType != null) {
            return returnType.value();
        }
        return fromJavaType(method.getGenericReturnType(), method);
    }

    private static String getParameterType(java.lang.reflect.Parameter parameter, Method method) {
        ParameterType parameterType = parameter.getAnnotation(ParameterType.class);
        if (parameterType != null) {
            return parameterType.value();
        }
        return fromJavaType(parameter.getParameterizedType(), method);
    }

    private static String fromJavaType(java.lang.reflect.Type type, Method method) {
        if (type instanceof Class) {
            Class<?> clazz = (Class<?>) type;

            if(clazz.isArray()) {
                throw new IllegalStateException("Error while initializing Kraken Expression Language. " +
                    "Cannot import function '" + method.getName() + "' into Kraken Expression Language "
                    + "because method signature uses array type. "
                    + "Array type is not supported, use Collection instead.");
            }

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
            } else if (Map.class.isAssignableFrom(clazz)) {
                return Type.ANY.getName();
            } else if (Object.class.equals(clazz)) {
                return Type.ANY.getName();
            }

            return clazz.getSimpleName();
        }

        if (type instanceof TypeVariable) {
            TypeVariable typeVariable = (TypeVariable) type;
            return toGenericsToken(typeVariable.getName());
        }

        if (type instanceof WildcardType) {
            if (((WildcardType) type).getUpperBounds().length > 1) {
                throw new IllegalStateException("Error while initializing Kraken Expression Language. " +
                    "Cannot import function '" + method.getName() + "' into Kraken Expression Language "
                    + "because method signature uses multiple bounds. "
                    + "Multiple generic bounds is not supported, use single bound instead.");
            }
            if (((WildcardType) type).getUpperBounds().length == 1) {
                return fromJavaType(((WildcardType) type).getUpperBounds()[0], method);
            }
            return Type.ANY.getName();
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            var rawType = pType.getRawType();
            if (rawType instanceof Class<?> && Collection.class.isAssignableFrom((Class<?>) rawType)) {
                if(pType.getActualTypeArguments().length > 1) {
                    throw new IllegalStateException("Error while initializing Kraken Expression Language. " +
                        "Cannot import function '" + method.getName() + "' into Kraken Expression Language "
                        + "because method signature uses multiple bounds. "
                        + "Multiple generic bounds is not supported, use single bound instead.");
                }
                if(pType.getActualTypeArguments().length == 1) {
                    return toArrayToken(fromJavaType(pType.getActualTypeArguments()[0], method));
                }
                return toArrayToken(Type.ANY.getName());
            }
            return Type.ANY.getName();
        }

        if(type instanceof GenericArrayType) {
            throw new IllegalStateException("Error while initializing Kraken Expression Language. " +
                "Cannot import function '" + method.getName() + "' into Kraken Expression Language "
                + "because method signature uses generic array type. "
                + "Generic array type is not supported, use Collection instead.");
        }

        return Type.ANY.getName();
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
        if (globalExpressionTarget != null) {
            expressionTargets.addAll(Arrays.asList(globalExpressionTarget.value()));
        }
        if (localExpressionTarget != null) {
            expressionTargets.addAll(Arrays.asList(localExpressionTarget.value()));
        }
        return expressionTargets;
    }

    private static void validateThatMethodIsStatic(Method m, String name) {
        if (!Modifier.isStatic(m.getModifiers())) {
            throw new IllegalStateException("Error while initializing Kraken Expression Language. " +
                "Cannot import function '" + name + "' into Kraken Expression Language because it is not static.");
        }
    }
}
