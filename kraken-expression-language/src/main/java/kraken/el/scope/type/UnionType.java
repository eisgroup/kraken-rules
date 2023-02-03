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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import kraken.el.scope.SymbolTable;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.symbol.VariableSymbol;

/**
 * Models a Union Type of {@link #leftType} and {@link #rightType}
 *
 * @author mulevicius
 */
public class UnionType extends Type {

    private final Type leftType;
    private final Type rightType;

    public UnionType(Type leftType, Type rightType) {
        super(leftType + " | " + rightType, mergeSymbolTables(leftType, rightType), List.of(leftType, rightType));

        this.leftType = Objects.requireNonNull(leftType);
        this.rightType = Objects.requireNonNull(rightType);
    }

    public Type getLeftType() {
        return leftType;
    }

    public Type getRightType() {
        return rightType;
    }

    @Override
    public boolean isPrimitive() {
        return leftType.isPrimitive() && rightType.isPrimitive();
    }

    @Override
    public boolean isKnown() {
        return leftType.isKnown() && rightType.isKnown();
    }

    @Override
    public boolean isDynamic() {
        return leftType.isDynamic() || rightType.isDynamic();
    }

    @Override
    public boolean isGeneric() {
        return leftType.isGeneric() || rightType.isGeneric();
    }

    @Override
    public Type rewriteGenericTypes(Map<GenericType, Type> genericTypeRewrites) {
        return new UnionType(
            leftType.rewriteGenericTypes(genericTypeRewrites),
            rightType.rewriteGenericTypes(genericTypeRewrites)
        );
    }

    @Override
    public Map<GenericType, Type> resolveGenericTypeRewrites(Type argumentType) {
        Map<GenericType, Type> rewrites = new HashMap<>();
        rewrites.putAll(leftType.resolveGenericTypeRewrites(argumentType));
        rewrites.putAll(rightType.resolveGenericTypeRewrites(argumentType));
        return rewrites;
    }

    @Override
    public Type rewriteGenericBounds() {
        return new UnionType(
            leftType.rewriteGenericBounds(),
            rightType.rewriteGenericBounds()
        );
    }

    @Override
    public boolean isUnion() {
        return true;
    }

    @Override
    public boolean isAssignableToArray() {
        return leftType.isAssignableToArray() || rightType.isAssignableToArray();
    }

    @Override
    public boolean isAssignableFrom(Type otherType) {
        if(otherType instanceof GenericType && ((GenericType) otherType).getBound() != null) {
            otherType = ((GenericType) otherType).getBound();
        }
        if(otherType instanceof UnionType) {
            return this.isAssignableFrom(((UnionType) otherType).leftType) || this.isAssignableFrom(((UnionType) otherType).rightType);
        }
        return leftType.isAssignableFrom(otherType) || rightType.isAssignableFrom(otherType);
    }

    @Override
    public boolean isComparableWith(Type otherType) {
        if(otherType instanceof GenericType && ((GenericType) otherType).getBound() != null) {
            otherType = ((GenericType) otherType).getBound();
        }
        if(otherType instanceof UnionType) {
            return this.isComparableWith(((UnionType) otherType).leftType) || this.isComparableWith(((UnionType) otherType).rightType);
        }
        return leftType.isComparableWith(otherType) || rightType.isComparableWith(otherType);
    }

    @Override
    public Optional<Type> resolveCommonTypeOf(Type otherType) {
        if(otherType instanceof UnionType) {
            Optional<Type> leftToLeft = leftType.resolveCommonTypeOf(((UnionType) otherType).leftType);
            Optional<Type> leftToRight = leftType.resolveCommonTypeOf(((UnionType) otherType).rightType);
            Optional<Type> rightToLeft = rightType.resolveCommonTypeOf(((UnionType) otherType).leftType);
            Optional<Type> rightToRight = rightType.resolveCommonTypeOf(((UnionType) otherType).rightType);
            if(leftToLeft.isEmpty() && leftToRight.isEmpty() || rightToLeft.isEmpty() && rightToRight.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(simplified(
                leftToLeft.or(() -> leftToRight).orElse(Type.ANY),
                rightToRight.or(() -> rightToLeft).orElse(Type.ANY)
            ));
        }
        Optional<Type> left = leftType.resolveCommonTypeOf(otherType);
        Optional<Type> right = rightType.resolveCommonTypeOf(otherType);
        if(left.isEmpty() || right.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(simplified(left.get(), right.get()));
    }

    @Override
    public Type unwrapArrayType() {
        Type left = leftType.unwrapArrayType();
        Type right = rightType.unwrapArrayType();
        return simplified(left, right);
    }

    @Override
    public Type wrapArrayType() {
        Type left = leftType.wrapArrayType();
        Type right = rightType.wrapArrayType();
        return simplified(left, right);
    }

    @Override
    public Type mapTo(Type target) {
        Type left = leftType.mapTo(target);
        Type right = rightType.mapTo(target);
        return simplified(left, right);
    }

    private Type simplified(Type left, Type right) {
        if(left.equals(right)) {
            return left;
        }
        if(!left.isKnown() || !right.isKnown()) {
            return Type.UNKNOWN;
        }
        if(!left.isDynamic() && left.isAssignableFrom(right)) {
            return right;
        }
        if(!right.isDynamic() && right.isAssignableFrom(left)) {
            return left;
        }
        return new UnionType(left, right);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        UnionType unionType = (UnionType) o;
        return Objects.equals(leftType, unionType.leftType) && Objects.equals(rightType, unionType.rightType)
                || Objects.equals(leftType, unionType.rightType) && Objects.equals(rightType, unionType.leftType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(leftType, rightType);
    }

    private static SymbolTable mergeSymbolTables(Type leftType, Type rightType) {
        ArrayList<FunctionSymbol> functionSymbols = new ArrayList<>();
        functionSymbols.addAll(leftType.getProperties().getFunctions());
        functionSymbols.addAll(rightType.getProperties().getFunctions());

        Map<String, VariableSymbol> references = new HashMap<>(leftType.getProperties().getReferences());
        rightType.getProperties().getReferences().forEach((key1, value) -> references.computeIfPresent(
                key1,
                (key, v) -> !value.equals(v)
                        ? new VariableSymbol(key, new UnionType(value.getType(), v.getType()))
                        : value
        ));

        return new SymbolTable(functionSymbols, references);
    }

}
