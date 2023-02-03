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

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Type which is an array of other {@link #elementType}
 *
 * @author mulevicius
 */
public class ArrayType extends Type {

    public static ArrayType of(Type type) {
        return new ArrayType(type);
    }

    private final Type elementType;

    public ArrayType(Type elementType) {
        super(toTypeName(elementType));

        this.elementType = Objects.requireNonNull(elementType);
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public boolean isAssignableFrom(Type otherType) {
        if(otherType instanceof GenericType && ((GenericType) otherType).getBound() != null) {
            otherType = ((GenericType) otherType).getBound();
        }
        if (otherType.isAssignableToArray()) {
            return elementType.isAssignableFrom(otherType.unwrapArrayType());
        }
        return super.isAssignableFrom(otherType);
    }

    @Override
    public boolean isComparableWith(Type otherType) {
        return false;
    }

    @Override
    public Optional<Type> resolveCommonTypeOf(Type otherType) {
        if(otherType instanceof GenericType && ((GenericType) otherType).getBound() != null) {
            otherType = ((GenericType) otherType).getBound();
        }
        if (otherType.isAssignableToArray()) {
            return elementType.resolveCommonTypeOf(otherType.unwrapArrayType()).map(t -> ArrayType.of(t));
        }
        return super.resolveCommonTypeOf(otherType);
    }

    @Override
    public boolean isKnown() {
        return elementType.isKnown();
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    @Override
    public boolean isGeneric() {
        return elementType.isGeneric();
    }

    @Override
    public Type rewriteGenericTypes(Map<GenericType, Type> genericTypeRewrites) {
        return ArrayType.of(elementType.rewriteGenericTypes(genericTypeRewrites));
    }

    @Override
    public Map<GenericType, Type> resolveGenericTypeRewrites(Type argumentType) {
        if(argumentType instanceof ArrayType) {
            return elementType.resolveGenericTypeRewrites(((ArrayType) argumentType).elementType);
        }
        if(argumentType.equals(Type.ANY)) {
            return elementType.resolveGenericTypeRewrites(Type.ANY);
        }
        return Map.of();
    }

    @Override
    public Type rewriteGenericBounds() {
        return ArrayType.of(elementType.rewriteGenericBounds());
    }

    @Override
    public boolean isUnion() {
        return elementType.isUnion();
    }

    @Override
    public boolean isAssignableToArray() {
        return true;
    }

    @Override
    public Type unwrapArrayType() {
        return elementType;
    }

    @Override
    public Type wrapArrayType() {
        return this;
    }

    @Override
    public Type mapTo(Type target) {
        if(target.isDynamic()) {
            return Type.ANY;
        }
        return ArrayType.of(elementType.mapTo(target));
    }

    private static String toTypeName(Type elementType) {
        var et = elementType instanceof UnionType
            ? "(" + elementType + ")"
            : elementType.toString();

        return et + "[]";
    }
}
