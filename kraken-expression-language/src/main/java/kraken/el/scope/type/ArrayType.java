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

import java.util.Objects;

/**
 * Type which is an array of other {@link #elementType}
 *
 * @author mulevicius
 */
public class ArrayType extends Type {

    public static ArrayType of(Type type) {
        return new ArrayType(type);
    }

    private Type elementType;

    public ArrayType(Type elementType) {
        super(elementType.getName() + "[]");

        this.elementType = Objects.requireNonNull(elementType);
    }

    public Type getElementType() {
        return elementType;
    }

    @Override
    public boolean isAssignableFrom(Type otherType) {
        if(otherType instanceof ArrayType) {
            return elementType.isAssignableFrom(((ArrayType) otherType).getElementType());
        }
        return super.isAssignableFrom(otherType);
    }

    @Override
    public boolean isComparableWith(Type otherType) {
        return false;
    }

    @Override
    public boolean isKnown() {
        return elementType.isKnown();
    }
}
