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

import kraken.el.scope.SymbolTable;
import kraken.el.scope.symbol.FunctionSymbol;
import kraken.el.scope.symbol.VariableSymbol;

import java.util.*;

/**
 * Models a Union Type of {@link #leftType} and {@link #rightType}
 *
 * @author mulevicius
 */
public class UnionType extends Type {

    private Type leftType;
    private Type rightType;

    public UnionType(Type leftType, Type rightType) {
        super("OR", mergeSymbolTables(leftType, rightType), List.of(leftType, rightType));

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
        return leftType.isPrimitive() || rightType.isPrimitive();
    }

    @Override
    public boolean isKnown() {
        return leftType.isKnown() && rightType.isKnown();
    }

    @Override
    public boolean isAssignableFrom(Type otherType) {
        if(otherType instanceof UnionType) {
            return this.isAssignableFrom(((UnionType) otherType).leftType) || this.isAssignableFrom(((UnionType) otherType).rightType);
        }
        return leftType.isAssignableFrom(otherType) || rightType.isAssignableFrom(otherType);
    }

    @Override
    public boolean isComparableWith(Type otherType) {
        if(otherType instanceof UnionType) {
            return this.isComparableWith(((UnionType) otherType).leftType) || this.isComparableWith(((UnionType) otherType).rightType);
        }
        return leftType.isComparableWith(otherType) || rightType.isComparableWith(otherType);
    }

    @Override
    public String toString() {
        return leftType + " | " + rightType;
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
