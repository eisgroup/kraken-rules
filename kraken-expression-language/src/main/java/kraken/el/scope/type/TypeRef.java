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

import java.util.Collection;
import java.util.Optional;

/**
 * Represents a type which is a soft reference to a concrete {@link Type} that must exist in the system.
 *
 * @author mulevicius
 */
public class TypeRef extends Type {

    private TypeRefResolver typeRefResolver;

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
    public Optional<Type> resolveCommonTypeOf(Type otherType) {
        return resolveReferencedType().resolveCommonTypeOf(otherType);
    }

    @Override
    public String toString() {
        return "Ref<" + getName() + ">";
    }

    private Type resolveReferencedType() {
        return getOrThrowMissingType(typeRefResolver.resolveType(getName()));
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
