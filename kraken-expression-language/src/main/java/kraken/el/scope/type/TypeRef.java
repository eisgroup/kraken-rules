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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import kraken.el.scope.SymbolTable;

/**
 * Represents a type which is a soft reference to a concrete {@link Type} that must exist in the system.
 *
 * @author mulevicius
 */
public class TypeRef extends Type {

    private final TypeRefResolver typeRefResolver;

    public TypeRef(String referencedTypeName, TypeRefResolver typeRefResolver) {
        super(referencedTypeName);

        this.typeRefResolver = typeRefResolver;
    }

    @Override
    public SymbolTable getProperties() {
        return resolveReferencedType().getProperties();
    }

    @Override
    public boolean isPrimitive() {
        return resolveReferencedType().isPrimitive();
    }

    @Override
    public Collection<Type> getExtendedTypes() {
        return resolveReferencedType().getExtendedTypes();
    }

    @Override
    public boolean isAssignableFrom(Type otherType) {
        return resolveReferencedType().isAssignableFrom(otherType);
    }

    @Override
    public boolean isComparableWith(Type otherType) {
        return resolveReferencedType().isComparableWith(otherType);
    }

    @Override
    public boolean isKnown() {
        return resolveReferencedType().isKnown();
    }

    @Override
    public boolean isDynamic() {
        return resolveReferencedType().isDynamic();
    }

    @Override
    public boolean isGeneric() {
        return resolveReferencedType().isGeneric();
    }

    @Override
    public Type rewriteGenericTypes(Map<GenericType, Type> genericTypeRewrites) {
        return resolveReferencedType().rewriteGenericTypes(genericTypeRewrites);
    }

    @Override
    public Map<GenericType, Type> resolveGenericTypeRewrites(Type argumentType) {
        return resolveReferencedType().resolveGenericTypeRewrites(argumentType);
    }

    @Override
    public Type rewriteGenericBounds() {
        return resolveReferencedType().rewriteGenericBounds();
    }

    @Override
    public boolean isUnion() {
        return resolveReferencedType().isUnion();
    }

    @Override
    public boolean isAssignableToArray() {
        return resolveReferencedType().isAssignableToArray();
    }

    @Override
    public Optional<Type> resolveCommonTypeOf(Type otherType) {
        return resolveReferencedType().resolveCommonTypeOf(otherType);
    }

    @Override
    public String toString() {
        return getName();
    }

    private Type resolveReferencedType() {
        return getOrThrowMissingType(typeRefResolver.resolveType(getName()));
    }

    @Override
    public Type unwrapArrayType() {
        return resolveReferencedType().unwrapArrayType();
    }

    @Override
    public Type mapTo(Type target) {
        return resolveReferencedType().mapTo(target);
    }

    @Override
    public Type wrapArrayType() {
        return resolveReferencedType().wrapArrayType();
    }

    private Type getOrThrowMissingType(Type type) {
        if(type == null) {
            throw new IllegalStateException(
                    "Type System of Kraken Expression Language is not initialized correctly. "
                            + "Type is referenced, but missing in the system: "
                            + getName());
        }
        return type;
    }
}
