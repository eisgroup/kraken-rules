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
package kraken.el.scope.type;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import kraken.el.scope.SymbolTable;

/**
 * Type which is generic by {@link #getName()}. Used to support generics in custom function implementation.
 * GenericType shall only appear in {@link kraken.el.scope.symbol.FunctionSymbol} and must be rewritten
 * when {@link kraken.el.ast.Ast} is built.
 *
 * @author mulevicius
 */
public class GenericType extends Type {

    private final Type bound;

    public GenericType(String name) {
        this(name, null);
    }

    public GenericType(String name, Type bound) {
        super(
            "<" + name + ">",
            bound != null ? bound.getProperties() : new SymbolTable(),
            bound != null ? bound.getExtendedTypes() : Collections.emptyList()
        );

        this.bound = bound;
    }

    @Override
    public boolean isAssignableFrom(Type otherType) {
        if(otherType instanceof GenericType) {
            return this.equals(otherType);
        }
        if(this.bound != null) {
            return this.bound.isAssignableFrom(otherType);
        }
        return super.isAssignableFrom(otherType);
    }

    @Override
    public boolean isAssignableToArray() {
        if(this.bound != null) {
            return this.bound.isAssignableToArray();
        }
        return super.isAssignableToArray();
    }

    @Override
    public boolean isDynamic() {
        if(this.bound != null) {
            return this.bound.isDynamic();
        }
        return super.isDynamic();
    }

    @Override
    public boolean isPrimitive() {
        if(this.bound != null) {
            return this.bound.isPrimitive();
        }
        return super.isPrimitive();
    }

    @Override
    public boolean isKnown() {
        if(this.bound != null) {
            return this.bound.isKnown();
        }
        return super.isKnown();
    }

    @Override
    public boolean isGeneric() {
        return true;
    }

    @Override
    public Type rewriteGenericTypes(Map<GenericType, Type> genericTypeRewrites) {
        if(genericTypeRewrites.containsKey(this)) {
            return genericTypeRewrites.get(this);
        }
        return Type.UNKNOWN;
    }

    @Override
    public Map<GenericType, Type> resolveGenericTypeRewrites(Type argumentType) {
        return Map.of(this, argumentType);
    }

    @Override
    public Type rewriteGenericBounds() {
        if(bound != null) {
            return bound.rewriteGenericBounds();
        }
        return Type.ANY;
    }

    @Override
    public boolean isUnion() {
        if(this.bound != null) {
            return this.bound.isUnion();
        }
        return super.isUnion();
    }

    @Override
    public boolean isComparableWith(Type otherType) {
        if(this.bound != null) {
            return this.bound.isComparableWith(otherType);
        }
        return super.isComparableWith(otherType);
    }

    @Override
    public Optional<Type> resolveCommonTypeOf(Type otherType) {
        if(otherType instanceof GenericType && this.equals(otherType)) {
            return Optional.of(this);
        }
        if(this.bound != null) {
            return this.bound.resolveCommonTypeOf(otherType);
        }
        return super.resolveCommonTypeOf(otherType);
    }

    @Nullable
    public Type getBound() {
        return bound;
    }
}
