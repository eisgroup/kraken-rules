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
package kraken.el.scope;

import static kraken.el.scope.type.Type.toType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Nullable;

import kraken.el.scope.symbol.FunctionParameter;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.symbol.VariableSymbol;
import kraken.el.scope.type.Type;
import kraken.el.scope.type.TypeRef;

/**
 * Represents type scope of current expression
 *
 * @author mulevicius
 */
public class Scope {

    private final String name;

    private final ScopeType scopeType;

    private final Scope parentScope;

    private final Type type;

    private final Map<String, Type> allTypes;

    /**
     * Creates GLOBAL Scope; a GLOBAL scope is a scope that has no parent;
     * it must also contain a registry of all types available in the system
     *
     * @param type of object represented by this scope;
     *             if type is {@link TypeRef} then it will be expanded into full type object by using {@link #allTypes}
     * @param allTypes available in the system
     */
    public Scope(Type type, Map<String, Type> allTypes) {
        this(ScopeType.GLOBAL, null, type, withNativeTypes(allTypes));
    }

    /**
     * Creates a child Scope which is nested inside parent Scope.
     *
     * @param scopeType indicates what kind of Scope this is
     * @param parentScope of this child Scope
     * @param type of object represented by this scope;
     *             if type is {@link TypeRef} then it will be expanded into full type object by using {@link #allTypes}
     */
    public Scope(ScopeType scopeType, @Nullable Scope parentScope, Type type) {
        this(scopeType, parentScope, type, Map.of());
    }

    private Scope(ScopeType scopeType, @Nullable Scope parentScope, Type type, Map<String, Type> allTypes) {
        this.scopeType = Objects.requireNonNull(scopeType);
        this.parentScope = parentScope;
        this.type = Objects.requireNonNull(type);
        this.allTypes = Objects.requireNonNull(allTypes);
        this.name = Optional.ofNullable(parentScope).map(Scope::getName).map(s -> s + "->").orElse("") + type;
    }

    /**
     *
     * @return unique name of the scope
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return type of the scope which allows to make a decision based on what kind of syntax of KEL is represented
     *          by this scope (PATH -vs- FILTER, GLOBAL -vs- LOCAL)
     * @see ScopeType
     */
    public ScopeType getScopeType() {
        return scopeType;
    }

    /**
     *
     * @return parent if it exists; if scope does not have a parent then it is a root scope.
     */
    public Scope getParentScope() {
        return parentScope;
    }

    /**
     *
     * @return actual structure of the object that is accessible in this scope.
     */
    public Type getType() {
        return type;
    }

    /**
     *
     * @return all types accessible in this scope
     */
    public Map<String, Type> getAllTypes() {
        return Collections.unmodifiableMap(allTypes);
    }


    /**
     * @param name of symbol
     * @return true if symbol exists in current immediate scope;
     *              a symbol is in current immediate scope if the scope is static and has symbol by name
     *              or a scope is dynamic and no static parent scope has this symbol
     */
    public boolean isReferenceInCurrentScope(String name) {
        return type.getProperties().getReferences().containsKey(name)
            || type.equals(Type.ANY)
            && !Optional.ofNullable(parentScope).map(scope -> scope.isReferenceStrictlyInScope(name)).orElse(false);
    }

    /**
     * @param name of symbol
     * @return true if symbol exists in strict scope including parents;
     *              if scope is dynamic then reference is not strictly in scope;
     */
    private boolean isReferenceStrictlyInScope(String name) {
        return type.getProperties().getReferences().containsKey(name)
            || Optional.ofNullable(parentScope).map(scope -> scope.isReferenceStrictlyInScope(name)).orElse(false);
    }

    /**
     * @param name of symbol
     * @return true if symbol exists only in global scope but not in any descendant scope
     */
    public boolean isReferenceInGlobalScope(String name) {
        return ScopeType.GLOBAL == scopeType && isReferenceInCurrentScope(name)
            || !isReferenceInCurrentScope(name)
            && Optional.ofNullable(parentScope).map(scope -> scope.isReferenceInGlobalScope(name)).orElse(false);
    }

    public Scope findClosestScopeOfType(ScopeType scopeType) {
        if(this.scopeType == scopeType) {
            return this;
        }
        return Optional.ofNullable(parentScope).map(scope -> scope.findClosestScopeOfType(scopeType)).orElse(null);
    }

    /**
     *
     * @param name of function symbol
     * @param paramCount of function
     * @return resolves actual function symbol accessible from within this scope
     */
    public Optional<FunctionSymbol> resolveFunctionSymbol(String name, int paramCount) {
        if(type.equals(Type.ANY) && ScopeType.GLOBAL == scopeType) {
            List<FunctionParameter> parameters = new ArrayList<>();
            for(int i = 0; i < paramCount; i++) {
                parameters.add(new FunctionParameter(i, Type.ANY));
            }
            return Optional.of(new FunctionSymbol(name, Type.ANY, parameters));
        }

        return resolveLocalFunctionSymbol(name, paramCount)
                .or(() -> Optional.ofNullable(parentScope).flatMap(s -> s.resolveFunctionSymbol(name, paramCount)));
    }

    private Optional<FunctionSymbol> resolveLocalFunctionSymbol(String name, int paramCount) {
        return type.getProperties().getFunctions().stream()
                .filter(fx -> fx.getName().equals(name) && fx.getParameters().size() == paramCount)
                .findFirst();
    }

    /**
     *
     * @param name of symbol
     * @return resolves actual reference symbol accessible from within this scope
     */
    public Optional<VariableSymbol> resolveReferenceSymbol(String name) {
        if(type.equals(Type.ANY) && isReferenceInCurrentScope(name)) {
            return Optional.of(new VariableSymbol(name, Type.ANY));
        }
        return Optional.ofNullable(type.getProperties().getReferences().get(name))
                .or(() -> Optional.ofNullable(parentScope).flatMap(s -> s.resolveReferenceSymbol(name)));
    }

    /**
     * @param name of symbol
     * @return a type of scope that has this particular reference symbol
     */
    public ScopeType findScopeTypeOfReference(String name) {
        if(isReferenceInCurrentScope(name)) {
            return getScopeType();
        }
        return Optional.ofNullable(parentScope)
            .map(s -> s.findScopeTypeOfReference(name))
            .orElse(null);
    }

    /**
     * @return all global variable symbols accessible within this scope
     */
    public Map<String, VariableSymbol> resolveGlobalSymbols() {
        if(ScopeType.GLOBAL == scopeType) {
            return type.getProperties().getReferences();
        }
        return Optional.ofNullable(parentScope)
            .map(s -> s.resolveGlobalSymbols())
            .orElse(Collections.emptyMap());
    }

    /**
     *
     * @param typeToken
     * @return a global type available in the current scope.
     */
    public Type resolveTypeOf(String typeToken) {
        if(type.equals(Type.ANY)) {
            return Type.ANY;
        }
        return Optional.ofNullable(parentScope)
            .map(s -> s.resolveTypeOf(typeToken))
            .orElseGet(() -> toType(typeToken, allTypes));
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Scope scope = (Scope) o;
        return name.equals(scope.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    private static Map<String, Type> withNativeTypes(Map<String, Type> allTypes) {
        Map<String, Type> types = new HashMap<>();
        types.putAll(allTypes);
        types.putAll(Type.nativeTypes);
        return types;
    }

    private static final Scope ANY = new Scope(ScopeType.LOCAL, new Scope(Type.ANY, Map.of()), Type.ANY);

    public static Scope dynamic() {
        return ANY;
    }

}
