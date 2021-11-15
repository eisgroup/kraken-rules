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
import kraken.el.functionregistry.FunctionDefinition;
import kraken.el.functionregistry.FunctionHeader;
import kraken.el.functionregistry.FunctionRegistry;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;
import kraken.el.scope.SymbolTable;
import kraken.el.scope.symbol.FunctionParameter;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.symbol.VariableSymbol;
import kraken.el.scope.type.ArrayType;
import kraken.el.scope.type.Type;
import kraken.el.scope.type.UnionType;
import kraken.model.FunctionSignature;
import kraken.model.context.Cardinality;
import kraken.model.context.ContextDefinition;
import kraken.model.context.external.ExternalContext;
import kraken.model.context.external.ExternalContextDefinitionReference;
import kraken.model.project.KrakenProject;
import kraken.model.project.ccr.CrossContextService;
import kraken.model.project.ccr.CrossContextServiceProvider;
import kraken.utils.Namespaces;

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

    private final CrossContextService crossContextService;

    private final SymbolTable allTypesSymbolTable;

    /**
     * Creates and initializes a new independent instance of ScopeBuilder
     *
     * @param krakenProject Kraken project.
     */
    public ScopeBuilder(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
        this.typeRegistry = new TypeRegistry(krakenProject);
        this.functionSymbols = resolveFunctionSymbols(krakenProject.getFunctionSignatures(), typeRegistry.getAllTypes());
        this.contextSymbols = krakenProject.getExternalContext() == null ?
                Map.of(CONTEXT, new VariableSymbol(CONTEXT, Type.ANY)) :
                buildExternalContextObject(krakenProject.getExternalContext());
        this.crossContextService = CrossContextServiceProvider.forProject(krakenProject);
        this.scopeCache = new ConcurrentHashMap<>();

        Map<String, VariableSymbol> contextVarSymbols = typeRegistry.getAllTypes()
                .entrySet()
                .stream()
                .map(type -> new VariableSymbol(type.getKey(), new UnionType(type.getValue(), ArrayType.of(type.getValue()))))
                .collect(Collectors.toMap(VariableSymbol::getName, symbol -> symbol));

        this.allTypesSymbolTable = new SymbolTable(functionSymbols.values(), withContext(contextVarSymbols));
    }

    public Type resolveTypeOf(String typeToken) {
        return Type.toType(typeToken, typeRegistry.getAllTypes());
    }

    /**
     *
     * @param contextDefinition Context Definition to build scope for.
     * @return scope for expression execution of rules that are applied on provided {@link ContextDefinition}.
     */
    public Scope buildScope(ContextDefinition contextDefinition) {
        return scopeCache.computeIfAbsent(contextDefinition.getName(), c -> doBuildScope(contextDefinition));
    }

    private Map<String, VariableSymbol> buildExternalContextObject(ExternalContext externalContext) {
        Map<String, VariableSymbol> varSymbols = new HashMap<>();

        for (Map.Entry<String, ExternalContext> entry : externalContext.getContexts().entrySet()) {
            Type type = createInlineAnonymousType(entry.getValue());

            String variableName = entry.getKey();
            varSymbols.put(variableName , new VariableSymbol(variableName , type));
        }

        for (Map.Entry<String, ExternalContextDefinitionReference> entry : externalContext.getExternalContextDefinitions().entrySet()) {
            Type type = typeRegistry.getExternalType(entry.getValue().getName())
                    .orElse(new Type(entry.getValue().getName(), false, false));

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

        Scope globalScope = new Scope(
                new Type(Namespaces.toFullName(krakenProject.getNamespace(), "GLOBAL"), globalSymbolTable),
                typeRegistry.getAllTypes()
        );

        Type contextDefinitionType = typeRegistry.get(contextDefinition.getName());
        return new Scope(ScopeType.LOCAL, globalScope, contextDefinitionType);
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
        TypeCardinality cardinality = Optional.ofNullable(ccrCardinalities.get(name))
            .orElse(TypeCardinality.SINGLE_OR_MULTIPLE);

        return cardinality == TypeCardinality.SINGLE_OR_MULTIPLE
            ? new UnionType(type, ArrayType.of(type))
            : cardinality == TypeCardinality.MULTIPLE
                ? ArrayType.of(type)
                : type;
    }

    private Map<String, TypeCardinality> resolveCrossContextCardinalities(ContextDefinition contextDefinition) {
        Map<String, TypeCardinality> ccrCardinalities = new HashMap<>();
        List<ContextPath> pathsToRuleContext =
            crossContextService.getPathsFor(contextDefinition.getName());

        crossContextService.getAllPaths()
                .forEach((key, value) -> pathsToRuleContext.forEach(contextPath -> {
                    List<ContextPath> depPaths = crossContextService.getPathsFor(key);

                    depPaths.forEach(depPath -> {
                        List<CrossContextPath> crossContextPath = crossContextService
                                .resolvePaths(contextPath, depPath.getLastElement());

                        if (crossContextPath.size() == 1) {
                            CrossContextPath candidatePath = crossContextPath.get(0);

                            if (!ccrCardinalities.containsKey(key)) {
                                ccrCardinalities.put(
                                        key,
                                        candidatePath.getCardinality() == Cardinality.MULTIPLE
                                                ? TypeCardinality.MULTIPLE
                                                : TypeCardinality.SINGLE
                                );
                            } else {
                                TypeCardinality originalCardinality = ccrCardinalities.get(key);
                                Cardinality cardinality = candidatePath.getCardinality();

                                if (originalCardinality == TypeCardinality.MULTIPLE && cardinality != Cardinality.MULTIPLE
                                        || originalCardinality == TypeCardinality.SINGLE && cardinality != Cardinality.SINGLE) {
                                    ccrCardinalities.put(key, TypeCardinality.SINGLE_OR_MULTIPLE);
                                }
                            }
                        }
                    });
                }));

        return ccrCardinalities;
    }

    private Map<String, VariableSymbol> withContext(Map<String, VariableSymbol> symbols) {
        Map<String, VariableSymbol> variablesWithContext = new HashMap<>(symbols);
        variablesWithContext.putAll(contextSymbols);

        return variablesWithContext;
    }

    private static Map<FunctionHeader, FunctionSymbol> resolveFunctionSymbols(
        List<FunctionSignature> functionSignatures,
        Map<String, Type> allTypes
    ) {
        var declaredFunctionSymbols = functionSignatures.stream()
            .map(functionSignature -> toSymbol(functionSignature, allTypes))
            .collect(Collectors.toMap(f -> new FunctionHeader(f.getName(), f.getParameters().size()), f -> f));

        var nativeFunctionSymbols = FunctionRegistry.getNativeFunctions(KrakenKel.EXPRESSION_TARGET)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                entry -> toSymbol(entry.getValue(), allTypes)
            ));

        Map<FunctionHeader, FunctionSymbol> functionSymbols = new HashMap<>(nativeFunctionSymbols);
        functionSymbols.putAll(declaredFunctionSymbols);
        return functionSymbols;
    }

    private static FunctionSymbol toSymbol(FunctionSignature functionSignature,
                                          Map<String, Type> types) {
        return new FunctionSymbol(
            functionSignature.getName(),
            toType(functionSignature.getReturnType(), types),
            toParameters(functionSignature.getParameterTypes(), types)
        );
    }

    private static FunctionSymbol toSymbol(FunctionDefinition functionDefinition,
                                          Map<String, Type> types) {
        return new FunctionSymbol(
            functionDefinition.getFunctionName(),
            toType(functionDefinition.getReturnType(), types),
            toParameters(functionDefinition.getParameterTypes(), types)
        );
    }

    private static List<FunctionParameter> toParameters(List<String> parameterTypes,
                                                        Map<String, Type> types) {
        List<FunctionParameter> parameters = new ArrayList<>();
        for(int i = 0; i < parameterTypes.size(); i++) {
            Type parameterType = toType(parameterTypes.get(i), types);
            parameters.add(new FunctionParameter(i, parameterType));
        }
        return parameters;
    }

    enum TypeCardinality {
        SINGLE,
        MULTIPLE,
        SINGLE_OR_MULTIPLE
    }

}
