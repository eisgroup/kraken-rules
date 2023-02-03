/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.scope;

import static kraken.el.scope.type.Type.toType;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kraken.context.path.ContextPath;
import kraken.cross.context.path.CrossContextPath;
import kraken.el.KrakenKel;
import kraken.el.functionregistry.FunctionHeader;
import kraken.el.functionregistry.FunctionRegistry;
import kraken.el.functionregistry.JavaFunction;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;
import kraken.el.scope.SymbolTable;
import kraken.el.scope.symbol.FunctionParameter;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.symbol.VariableSymbol;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;
import kraken.el.scope.type.UnionType;
import kraken.model.Function;
import kraken.model.FunctionSignature;
import kraken.model.GenericTypeBound;
import kraken.model.context.Cardinality;
import kraken.model.context.ContextDefinition;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinitionReference;
import kraken.model.project.KrakenProject;
import kraken.model.project.ccr.CrossContextService;
import kraken.model.project.ccr.CrossContextServiceProvider;

/**
 * @author mulevicius
 */
public class ScopeBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ScopeBuilder.class);

    private static final String CONTEXT = "context";

    private final KrakenProject krakenProject;

    private final TypeRegistry typeRegistry;

    private final Map<FunctionHeader, FunctionSymbol> functionSymbols;

    private final Map<String, VariableSymbol> contextSymbols;

    private final Map<String, Scope> scopeCache;

    private final Map<String, Scope> functionScopeCache;

    private final Map<FunctionInvocationKey, Scope> functionInvocationScopeCache;

    private final CrossContextService crossContextService;

    private final SymbolTable allTypesSymbolTable;

    private final Scope globalFunctionScope;

    /**
     * Creates and initializes a new independent instance of ScopeBuilder
     *
     * @param krakenProject Kraken project.
     */
    public ScopeBuilder(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
        this.typeRegistry = new TypeRegistry(krakenProject);
        this.functionSymbols = resolveFunctionSymbols(krakenProject.getFunctionSignatures(),
            krakenProject.getFunctions(), typeRegistry.getAllTypes());
        this.contextSymbols = krakenProject.getExternalContext() == null ?
                Map.of(CONTEXT, new VariableSymbol(CONTEXT, Type.ANY)) :
                buildExternalContextObject(krakenProject.getExternalContext());
        this.crossContextService = CrossContextServiceProvider.forProject(krakenProject);
        this.scopeCache = new ConcurrentHashMap<>();
        this.functionScopeCache = new ConcurrentHashMap<>();
        this.functionInvocationScopeCache = new ConcurrentHashMap<>();

        Map<String, VariableSymbol> contextVarSymbols = typeRegistry.getAllTypes()
                .entrySet()
                .stream()
                .map(type -> new VariableSymbol(type.getKey(), new UnionType(type.getValue(), ArrayType.of(type.getValue()))))
                .collect(Collectors.toMap(VariableSymbol::getName, symbol -> symbol));

        this.allTypesSymbolTable = new SymbolTable(functionSymbols.values(), withContext(contextVarSymbols));

        this.globalFunctionScope = new Scope(
            new Type(
                krakenProject.getIdentifier().toString() + "_" + krakenProject.getNamespace() + "_FUNCTIONGLOBAL",
                new SymbolTable(functionSymbols.values(), Map.of())
            ),
            typeRegistry.getAllTypes()
        );
    }

    public Type resolveTypeOf(String typeToken) {
        return resolveTypeOf(typeToken, Map.of());
    }

    public Type resolveTypeOf(String typeToken, Map<String, Type> bounds) {
        return toType(typeToken, typeRegistry.getAllTypes(), bounds);
    }

    public FunctionSymbol buildFunctionSymbol(Function function) {
        return toSymbol(function, typeRegistry.getAllTypes());
    }

    public FunctionSymbol buildFunctionSymbol(JavaFunction function) {
        return toSymbol(function, typeRegistry.getAllTypes());
    }

    public FunctionSymbol buildFunctionSymbol(FunctionSignature function) {
        return toSymbol(function, typeRegistry.getAllTypes());
    }

    /**
     *
     * @param contextDefinition Context Definition to build scope for.
     * @return scope for expression execution of rules that are applied on provided {@link ContextDefinition}.
     */
    public Scope buildScope(ContextDefinition contextDefinition) {
        return scopeCache.computeIfAbsent(contextDefinition.getName(), c -> doBuildScope(contextDefinition));
    }

    /**
     * @param function to build scope for using defined signature parameter types as argument type map
     * @return scope for function body expression
     */
    public Scope buildFunctionScope(Function function) {
        return functionScopeCache.computeIfAbsent(function.getName(), c -> {
            var bounds = buildBounds(function.getGenericTypeBounds(), typeRegistry.getAllTypes());
            Map<String, Type> parameterTypes = function.getParameters().stream()
                .collect(Collectors.toMap(p -> p.getName(), p -> resolveTypeOf(p.getType(), bounds)));
            return buildFunctionScope(function, parameterTypes);
        });
    }

    /**
     * @param function to build scope for using provided types as argument type map
     * @param parameterTypes a map of argument types
     * @return scope for function body expression
     */
    public Scope buildFunctionScope(Function function, Map<String, Type> parameterTypes) {
        FunctionInvocationKey key = new FunctionInvocationKey(function, parameterTypes);
        return functionInvocationScopeCache.computeIfAbsent(
            key,
            f -> doBuildFunctionScope(f.getFunction(), f.getParameterTypes())
        );
    }

    private Map<String, VariableSymbol> buildExternalContextObject(ExternalContext externalContext) {
        Map<String, VariableSymbol> varSymbols = new HashMap<>();

        for (Map.Entry<String, ExternalContext> entry : externalContext.getContexts().entrySet()) {
            Type type = createInlineAnonymousType(entry.getValue());

            String variableName = entry.getKey();
            varSymbols.put(variableName , new VariableSymbol(variableName , type));
        }

        for (Map.Entry<String, ExternalContextDefinitionReference> entry : externalContext.getExternalContextDefinitions().entrySet()) {
            Type type = Optional.ofNullable(typeRegistry.getAllTypes().get(entry.getValue().getName()))
                    .orElse(new Type(entry.getValue().getName()));

            String variableName = entry.getKey();
            varSymbols.put(variableName, new VariableSymbol(variableName, type));
        }

        return varSymbols;
    }

    private Type createInlineAnonymousType(ExternalContext externalContext) {
        return new Type(externalContext.getName(), new SymbolTable(List.of(), buildExternalContextObject(externalContext)));
    }

    private Scope doBuildScope(ContextDefinition contextDefinition) {
        SymbolTable globalSymbolTable = determineSymbolTable(contextDefinition);

        String uniqueGlobalScopeName = krakenProject.getIdentifier().toString()
            + "_" + krakenProject.getNamespace() + "_GLOBAL";

        Scope globalScope = new Scope(new Type(uniqueGlobalScopeName, globalSymbolTable), typeRegistry.getAllTypes());

        Type contextDefinitionType = typeRegistry.get(contextDefinition.getName());
        return new Scope(ScopeType.LOCAL, globalScope, contextDefinitionType);
    }

    private Scope doBuildFunctionScope(Function function, Map<String, Type> actualParameterTypes) {
        Map<String, VariableSymbol> argumentSymbols = actualParameterTypes.entrySet().stream()
            .collect(Collectors.toMap(e -> e.getKey(), e -> new VariableSymbol(e.getKey(), e.getValue())));

        String argumentString = function.getParameters().stream()
            .map(p -> actualParameterTypes.get(p.getName()).getName())
            .collect(Collectors.joining(", "));

        String uniqueFunctionHeader = String.format("%s(%s)", function.getName(), argumentString);
        Type arguments = new Type(
            uniqueFunctionHeader + "_ARGUMENTS_MAP",
            new SymbolTable(List.of(), argumentSymbols)
        );
        return new Scope(ScopeType.VARIABLES_MAP, globalFunctionScope, arguments);
    }

    private SymbolTable determineSymbolTable(ContextDefinition contextDefinition) {
        if(typeRegistry.getGlobalTypes().containsKey(contextDefinition.getName())) {
            return buildSymbolTable(contextDefinition);
        }
        logFallBackToApproximateScope(contextDefinition);
        return allTypesSymbolTable;
    }

    private void logFallBackToApproximateScope(ContextDefinition contextDefinition) {
        String template = "Cannot build exact Scope of {0} in KrakenProject for namespace {1}, " +
                "because such context is not accessible from root {2}. An approximate Scope will be used.";
        String message = MessageFormat.format(
                template,
                contextDefinition.getName(),
                krakenProject.getNamespace(),
                krakenProject.getRootContextName()
        );
        logger.warn(message);
    }

    private SymbolTable buildSymbolTable(ContextDefinition contextDefinition) {
        Map<String, TypeCardinality> ccrCardinalities = resolveCrossContextCardinalities(contextDefinition);

        Map<String, VariableSymbol> contextVarSymbols = typeRegistry.getGlobalTypes()
                .entrySet()
                .stream()
                .map(type -> toVariableSymbol(type.getKey(), type.getValue(), ccrCardinalities))
                .collect(Collectors.toMap(VariableSymbol::getName, symbol -> symbol));

        return new SymbolTable(functionSymbols.values(), withContext(contextVarSymbols));
    }

    private VariableSymbol toVariableSymbol(String name, Type type, Map<String, TypeCardinality> ccrCardinalities) {
        Type variableType = wrapToArrayIfMultipleCcr(name, type, ccrCardinalities);
        return new VariableSymbol(name, variableType);
    }

    private Type wrapToArrayIfMultipleCcr(String name, Type type, Map<String, TypeCardinality> ccrCardinalities) {
        if(!ccrCardinalities.containsKey(name)) {
            return Type.UNKNOWN;
        }
        TypeCardinality cardinality = ccrCardinalities.get(name);
        return cardinality == TypeCardinality.SINGLE_OR_MULTIPLE
            ? new UnionType(type, ArrayType.of(type))
            : cardinality == TypeCardinality.MULTIPLE
                ? ArrayType.of(type)
                : type;
    }

    private Map<String, TypeCardinality> resolveCrossContextCardinalities(ContextDefinition contextDefinition) {
        Map<String, TypeCardinality> ccrCardinalities = new HashMap<>();
        List<ContextPath> pathsToRuleContext = crossContextService.getPathsFor(contextDefinition.getName());

        for(ContextDefinition ccr : krakenProject.getContextDefinitions().values()) {
            String ccrName = ccr.getName();
            if(!crossContextService.hasPathTo(ccrName)) {
                continue;
            }
            for(ContextPath pathToRuleContext : pathsToRuleContext) {
                List<CrossContextPath> pathsToCcr = crossContextService.resolvePaths(pathToRuleContext, ccrName);
                TypeCardinality cardinality = determineCommonCardinality(pathsToCcr);
                if (!ccrCardinalities.containsKey(ccrName)) {
                    ccrCardinalities.put(ccrName, cardinality);
                } else {
                    TypeCardinality originalCardinality = ccrCardinalities.get(ccrName);
                    if(originalCardinality == TypeCardinality.SINGLE && cardinality == TypeCardinality.MULTIPLE
                        || originalCardinality == TypeCardinality.MULTIPLE && cardinality == TypeCardinality.SINGLE
                        || originalCardinality == TypeCardinality.SINGLE_OR_MULTIPLE
                        || cardinality == TypeCardinality.SINGLE_OR_MULTIPLE) {
                        ccrCardinalities.put(ccrName, TypeCardinality.SINGLE_OR_MULTIPLE);
                    }
                }
            }
        }

        return ccrCardinalities;
    }

    private TypeCardinality determineCommonCardinality(List<CrossContextPath> pathsToCcr) {
        Cardinality cardinality = pathsToCcr.get(0).getCardinality();
        for(CrossContextPath pathToCcr : pathsToCcr) {
            if(cardinality != pathToCcr.getCardinality()) {
                return TypeCardinality.SINGLE_OR_MULTIPLE;
            }
        }
        return cardinality == Cardinality.SINGLE ? TypeCardinality.SINGLE : TypeCardinality.MULTIPLE;
    }

    private Map<String, VariableSymbol> withContext(Map<String, VariableSymbol> symbols) {
        Map<String, VariableSymbol> variablesWithContext = new HashMap<>(symbols);
        variablesWithContext.putAll(contextSymbols);

        return variablesWithContext;
    }

    private static Map<FunctionHeader, FunctionSymbol> resolveFunctionSymbols(
        List<FunctionSignature> functionSignatures,
        List<Function> functions,
        Map<String, Type> allTypes
    ) {
        var declaredFunctionSymbols = functionSignatures.stream()
            .map(functionSignature -> toSymbol(functionSignature, allTypes))
            .collect(Collectors.toMap(
                FunctionSymbol::header, f -> f, (v1, v2) -> v1
            ));

        var implementedFunctionSymbols = functions.stream()
            .map(function -> toSymbol(function, allTypes))
            .collect(Collectors.toMap(
                FunctionSymbol::header, f -> f, (v1, v2) -> v1
            ));

        var nativeFunctionSymbols = FunctionRegistry.getNativeFunctions(KrakenKel.EXPRESSION_TARGET)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> toSymbol(entry.getValue(), allTypes)
            ));

        Map<FunctionHeader, FunctionSymbol> functionSymbols = new HashMap<>(nativeFunctionSymbols);
        functionSymbols.putAll(implementedFunctionSymbols);
        functionSymbols.putAll(declaredFunctionSymbols);
        return functionSymbols;
    }

    private static FunctionSymbol toSymbol(FunctionSignature functionSignature, Map<String, Type> types) {
        var bounds = buildBounds(functionSignature.getGenericTypeBounds(), types);

        return new FunctionSymbol(
            functionSignature.getName(),
            toType(functionSignature.getReturnType(), types, bounds),
            toParameters(functionSignature.getParameterTypes(), types, bounds)
        );
    }

    private static FunctionSymbol toSymbol(Function function, Map<String, Type> types) {
        var bounds = buildBounds(function.getGenericTypeBounds(), types);

        return new FunctionSymbol(
            function.getName(),
            toType(function.getReturnType(), types, bounds),
            toParameters(
                function.getParameters().stream()
                    .map(p -> p.getType())
                    .collect(Collectors.toList()),
                types,
                bounds
            )
        );
    }

    private static FunctionSymbol toSymbol(JavaFunction javaFunction, Map<String, Type> types) {
        return javaFunction.toFunctionSymbol(types);
    }

    private static List<FunctionParameter> toParameters(List<String> parameterTypes,
                                                        Map<String, Type> types,
                                                        Map<String, Type> bounds) {
        List<FunctionParameter> parameters = new ArrayList<>();
        for(int i = 0; i < parameterTypes.size(); i++) {
            Type parameterType = toType(parameterTypes.get(i), types, bounds);
            parameters.add(new FunctionParameter(i, parameterType));
        }
        return parameters;
    }

    private static Map<String, Type> buildBounds(List<GenericTypeBound> genericTypeBounds, Map<String, Type> types) {
        return genericTypeBounds.stream()
            .collect(Collectors.toMap(
                GenericTypeBound::getGeneric,
                g -> toType(g.getBound(), types),
                (v1, v2) -> v1
            ));
    }

    enum TypeCardinality {
        SINGLE,
        MULTIPLE,
        SINGLE_OR_MULTIPLE
    }

}
