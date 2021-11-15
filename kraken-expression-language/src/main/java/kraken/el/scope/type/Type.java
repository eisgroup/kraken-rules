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

    public static final Type BOOLEAN = new Type("Boolean", true);
    public static final Type STRING = new Type("String", true);
    public static final Type NUMBER = new Type("Number", true);
    public static final Type MONEY = new Type("Money", true);
    public static final Type DATE = new Type("Date", true);
    public static final Type DATETIME = new Type("DateTime", true);
    public static final Type TYPE = new Type("Type", true);

    /**
     * Represents type that cannot be identified.
     * <p/>
     * This is different from {@link Type#ANY} because {@link Type#ANY} identifies that type is known and it is dynamic,
     * while {@link Type#UNKNOWN} indicates that type in general is unknown and does not exist.
     * <p/>
     * This usually indicates a type usage error in expression and should be validated accordingly.
     */
    public static final Type UNKNOWN = new Type("Unknown", false, false);

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
        Common lexer = new CommonLexer(CharStreams.fromString(typeToken));
        Value value = new Value(new CommonTokenStream(lexer));
        TypeGeneratingVisitor visitor = new TypeGeneratingVisitor(globalTypes);
        return visitor.visit(value.type());
    }

    public static final Map<String, Type> nativeTypes = Map.of(
            BOOLEAN.getName(), BOOLEAN,
            STRING.getName(), STRING,
            NUMBER.getName(), NUMBER,
            MONEY.getName(), MONEY,
            DATE.getName(), DATE,
            DATETIME.getName(), DATETIME,
            TYPE.getName(), TYPE,
            UNKNOWN.getName(), UNKNOWN,
            ANY.getName(), ANY
    );

    private final String name;

    private final boolean primitive;

    private final boolean known;

    private final SymbolTable properties;

    private final Collection<Type> extendedTypes;

    public Type(String name) {
        this(name, false);
    }

    public Type(String name, boolean primitive) {
        this(name, primitive, true);
    }

    public Type(String name, boolean primitive, boolean known) {
        this(name, primitive, known, new SymbolTable(), Collections.emptyList());
    }

    public Type(String name, SymbolTable properties) {
        this(name, properties, Collections.emptyList());
    }

    public Type(String name, SymbolTable properties, Collection<Type> extendedTypes) {
        this(name, false, true, properties, extendedTypes);
    }

    public Type(String name, boolean primitive, boolean known, SymbolTable properties, Collection<Type> extendedTypes) {
        this.name = Objects.requireNonNull(name);
        this.primitive = primitive;
        this.known = known;
        this.properties = Objects.requireNonNull(properties);
        this.extendedTypes = Objects.requireNonNull(extendedTypes);
    }

    public String getName() {
        return name;
    }

    public boolean isPrimitive() {
        return primitive;
    }

    public SymbolTable getProperties() {
        return properties;
    }

    /**
     * @return true if type exists in subsystem, or it is a dynamic type
     */
    public boolean isKnown() {
        return known;
    }

    /**
     * @param otherType
     * @return true if this type is the same or is a super type of other type
     */
    public boolean isAssignableFrom(Type otherType) {
        return this.equals(ANY)
                || otherType.equals(ANY)
                || this.equals(NUMBER) && otherType.equals(MONEY)
                || this.equals(MONEY) && otherType.equals(NUMBER)
                || this.equals(otherType)
                || otherType.getExtendedTypes().stream().anyMatch(oEx -> isAssignableFrom(oEx));
    }

    public boolean isComparableWith(Type otherType) {
        return this.equals(ANY)
                || otherType.equals(ANY)
                || areNumber(this, otherType)
                || areDateTime(this, otherType)
                || areDate(this, otherType)
                || otherType.getExtendedTypes().stream().anyMatch(this::isComparableWith);
    }

    public Optional<Type> resolveCommonTypeOf(Type otherType) {
        if(this.isAssignableFrom(otherType)) {
            return Optional.of(this);
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
