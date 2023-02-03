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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import kraken.el.Common;
import kraken.el.Value;
import kraken.el.ast.builder.CommonLexer;
import kraken.el.scope.SymbolTable;

/**
 * Represents AST node evaluation type
 *
 * @author mulevicius
 */
public class Type {

    public static final Type BOOLEAN = new Type("Boolean");
    public static final Type STRING = new Type("String");
    public static final Type NUMBER = new Type("Number");
    public static final Type MONEY = new Type("Money");
    public static final Type DATE = new Type("Date");
    public static final Type DATETIME = new Type("DateTime");
    public static final Type TYPE = new Type("Type");

    /**
     * Represents type that cannot be identified.
     * <p/>
     * This is different from {@link Type#ANY} because {@link Type#ANY} identifies that type is known and it is dynamic,
     * while {@link Type#UNKNOWN} indicates that type in general is unknown and does not exist.
     * <p/>
     * This usually indicates a type usage error in expression and should be validated accordingly.
     */
    public static final Type UNKNOWN = new Type("Unknown");

    /**
     * Represents a known dynamic type
     */
    public static final Type ANY = new Type("Any");

    /**
     * Parses type instance from type string token.
     * <p/>
     * Supported type tokens:
     * <ul>
     *     <li>native types - Any, Number, Money, Boolean, String, Date, DateTime </li>
     *     <li>registered type - EntityType</li>
     *     <li>array - Type[]</li>
     *     <li>union - Type1 | Type2</li>
     *     <li>generic - &lt;T&gt;</li>
     * </ul>
     *
     * @param typeToken
     * @param globalTypes
     * @return
     */
    public static Type toType(String typeToken, Map<String, Type> globalTypes) {
        return toType(typeToken, globalTypes, Map.of());
    }

    /**
     * Parses type instance from type string token.
     * <p/>
     * Supported type tokens:
     * <ul>
     *     <li>native types - Any, Number, Money, Boolean, String, Date, DateTime </li>
     *     <li>registered type - EntityType</li>
     *     <li>array - Type[]</li>
     *     <li>union - Type1 | Type2</li>
     *     <li>generic - &lt;T&gt;</li>
     * </ul>
     *
     * @param typeToken
     * @param globalTypes
     * @param bounds
     * @return
     */
    public static Type toType(String typeToken, Map<String, Type> globalTypes, Map<String, Type> bounds) {
        Common lexer = new CommonLexer(CharStreams.fromString(typeToken));
        Value value = new Value(new CommonTokenStream(lexer));
        TypeGeneratingVisitor visitor = new TypeGeneratingVisitor(globalTypes, bounds);
        return visitor.visit(value.type());
    }

    private static final Map<String, Type> primitiveTypes = Map.of(
        BOOLEAN.getName(), BOOLEAN,
        STRING.getName(), STRING,
        NUMBER.getName(), NUMBER,
        MONEY.getName(), MONEY,
        DATE.getName(), DATE,
        DATETIME.getName(), DATETIME,
        TYPE.getName(), TYPE
    );

    public static final Map<String, Type> nativeTypes;
    static {
        nativeTypes = new HashMap<>(primitiveTypes);
        nativeTypes.put(UNKNOWN.getName(), UNKNOWN);
        nativeTypes.put(ANY.getName(), ANY);
    }

    private final String name;

    private final SymbolTable properties;

    private final Collection<Type> extendedTypes;

    public Type(String name) {
        this(name, new SymbolTable(), Collections.emptyList());
    }

    public Type(String name, SymbolTable properties) {
        this(name, properties, Collections.emptyList());
    }

    public Type(String name, SymbolTable properties, Collection<Type> extendedTypes) {
        this.name = Objects.requireNonNull(name);
        this.properties = Objects.requireNonNull(properties);
        this.extendedTypes = Objects.requireNonNull(extendedTypes);
    }

    public String getName() {
        return name;
    }

    public boolean isPrimitive() {
        return primitiveTypes.containsKey(name);
    }

    public SymbolTable getProperties() {
        return properties;
    }

    /**
     * @return true if type exists in subsystem, or it is a dynamic type
     */
    public boolean isKnown() {
        return !this.equals(UNKNOWN);
    }

    public boolean isDynamic() {
        return this.equals(ANY);
    }

    /**
     * @return true if this type is generic in some way
     */
    public boolean isGeneric() {
        return false;
    }

    /**
     * @param genericTypeRewrites
     * @return a type with generics rewritten or UNKNOWN iof rewrite not provided
     */
    public Type rewriteGenericTypes(Map<GenericType, Type> genericTypeRewrites) {
        return this;
    }

    /**
     *
     * @param argumentType
     * @return resolves type rewrites based on actual argument type provided for a generic type parameter.<p>
     *         If argumentType is String[] and this type is T[], then T rewrite is String.<p>
     *         If argumentType is String[][] and this type is T[], then T rewrite is String[].<p>
     *         If argumentType is String[] and this type is T, then T rewrite is String[].
     *         If argumentType is String[] and this type is not a generic, or it cannot resolve to compatible generic,
     *         then rewrite type is not resolved
     */
    public Map<GenericType, Type> resolveGenericTypeRewrites(Type argumentType) {
        return Map.of();
    }

    /**
     * @return a type with generics rewritten to their bounds or ANY if unbounded
     */
    public Type rewriteGenericBounds() {
        return this;
    }

    /**
     * @return true if this type is union of more than one specific type
     */
    public boolean isUnion() {
        return false;
    }

    public boolean isAssignableToArray() {
        return isDynamic();
    }

    /**
     * @param otherType
     * @return true if this type is the same or is a super type of other type
     */
    public boolean isAssignableFrom(Type otherType) {
        return this.isDynamic()
            || otherType.isDynamic()
            || otherType instanceof GenericType && ((GenericType) otherType).getBound() != null && this.isAssignableFrom(((GenericType) otherType).getBound())
            || this.equals(NUMBER) && otherType.equals(MONEY)
            || this.equals(otherType)
            || otherType.getExtendedTypes().stream().anyMatch(oEx -> isAssignableFrom(oEx));
    }

    public boolean isComparableWith(Type otherType) {
        return this.isDynamic()
            || otherType.isDynamic()
            || otherType instanceof GenericType && ((GenericType) otherType).getBound() != null && this.isComparableWith(((GenericType) otherType).getBound())
            || areNumber(this, otherType)
            || areDateTime(this, otherType)
            || areDate(this, otherType)
            || otherType.getExtendedTypes().stream().anyMatch(this::isComparableWith);
    }

    public Optional<Type> resolveCommonTypeOf(Type otherType) {
        if(isDynamic() || otherType.isDynamic()) {
            return Optional.of(Type.ANY);
        }
        if(!isKnown() || !otherType.isKnown()) {
            return Optional.of(Type.UNKNOWN);
        }
        if(this.isAssignableFrom(otherType)) {
            return Optional.of(this);
        }
        if(otherType.isAssignableFrom(this)) {
            return Optional.of(otherType);
        }
        return extendedTypes.stream()
                .filter(extendedType -> extendedType.isAssignableFrom(otherType))
                .findFirst();
    }

    private boolean areNumber(Type firstType, Type secondType){
        return (Type.NUMBER.isAssignableFrom(firstType)  || Type.MONEY.isAssignableFrom(firstType))
            && (Type.NUMBER.isAssignableFrom(secondType) || Type.MONEY.isAssignableFrom(secondType));
    }

    private boolean areDateTime(Type firstType, Type secondType){
        return Type.DATETIME.isAssignableFrom(firstType) && Type.DATETIME.isAssignableFrom(secondType);
    }

    private boolean areDate(Type firstType, Type secondType){
        return Type.DATE.isAssignableFrom(firstType) && Type.DATE.isAssignableFrom(secondType);
    }

    public Collection<Type> getExtendedTypes() {
        return extendedTypes;
    }

    /**
     * @return wraps type to array if it is not array already. For example, if type is String, then array type is String[].
     * If type is already array then the type itself is returned. If type is dynamic then dynamic is returned.
     */
    public Type wrapArrayType() {
        if(this.isDynamic()) {
            return Type.ANY;
        }
        if(!this.isKnown()) {
            return Type.UNKNOWN;
        }
        return ArrayType.of(this);
    }

    /**
     * @return unwraps type of array. For example, if type is String[][], then array type is String[].
     * If type is already singular then the type itself is returned.
     */
    public Type unwrapArrayType() {
        return this;
    }

    /**
     *
     * @param target type to flat map to
     * @return mapped typed from this type to target type.
     * If type is Coverage[] and target is Money, then mapped type is Money[].
     * If type is Coverage[] and target is Money[], then mapped type is Money[][].
     * If type is Coverage[] and target is (Money | Money[]), then mapped type is (Money | Money[])[].
     */
    public Type mapTo(Type target) {
        return target;
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
        if (o == null) {
            return false;
        }
        Type type = (Type) o;
        return Objects.equals(name, type.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    public static String toArrayToken(String typeToken) {
        return typeToken + "[]";
    }

    public static String toGenericsToken(String typeToken) {
        return "<" + typeToken + ">";
    }
}
