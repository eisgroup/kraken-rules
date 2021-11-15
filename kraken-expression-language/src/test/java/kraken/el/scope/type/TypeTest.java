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

import org.junit.Test;

import static kraken.el.scope.type.Type.ANY;
import static kraken.el.scope.type.Type.BOOLEAN;
import static kraken.el.scope.type.Type.DATE;
import static kraken.el.scope.type.Type.DATETIME;
import static kraken.el.scope.type.Type.MONEY;
import static kraken.el.scope.type.Type.NUMBER;
import static kraken.el.scope.type.Type.STRING;
import static kraken.el.scope.type.Type.UNKNOWN;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 * @author mulevicius
 */
public class TypeTest {

    @Test
    public void typesShouldBeAssignable() {
        assertThat(NUMBER.isAssignableFrom(NUMBER), is(true));
        assertThat(STRING.isAssignableFrom(STRING), is(true));
        assertThat(DATE.isAssignableFrom(DATE), is(true));
        assertThat(DATETIME.isAssignableFrom(DATETIME), is(true));
        assertThat(BOOLEAN.isAssignableFrom(BOOLEAN), is(true));
        assertThat(MONEY.isAssignableFrom(MONEY), is(true));

        assertThat(DATE.isAssignableFrom(DATETIME), is(false));
        assertThat(DATETIME.isAssignableFrom(DATE), is(false));

        assertThat(MONEY.isAssignableFrom(NUMBER), is(true));
        assertThat(NUMBER.isAssignableFrom(MONEY), is(true));
    }

    @Test
    public void allTypesShouldBeAssignableToGenericType() {
        assertThat(generic().isAssignableFrom(STRING), is(true));
        assertThat(STRING.isAssignableFrom(generic()), is(false));

        assertThat(generic().isAssignableFrom(UNKNOWN), is(true));
        assertThat(UNKNOWN.isAssignableFrom(generic()), is(false));

        assertThat(generic().isAssignableFrom(ANY), is(true));
        assertThat(generic().isAssignableFrom(arr(UNKNOWN)), is(true));
        assertThat(generic().isAssignableFrom(arr(STRING)), is(true));
        assertThat(generic().isAssignableFrom(arr(generic())), is(true));

        assertThat(arr(generic()).isAssignableFrom(ANY), is(true));
        assertThat(arr(generic()).isAssignableFrom(arr(STRING)), is(true));
        assertThat(arr(generic()).isAssignableFrom(arr(UNKNOWN)), is(true));
        assertThat(arr(generic()).isAssignableFrom(arr(ANY)), is(true));
    }

    @Test
    public void unknownTypeIsNotAssignable() {
        assertThat(UNKNOWN.isAssignableFrom(UNKNOWN), is(true));
        assertThat(STRING.isAssignableFrom(UNKNOWN), is(false));
        assertThat(UNKNOWN.isAssignableFrom(STRING), is(false));
        assertThat(UNKNOWN.isAssignableFrom(ANY), is(true));
    }

    @Test
    public void anyTypeIsAlwaysAssignable() {
        assertThat(ANY.isAssignableFrom(ANY), is(true));
        assertThat(ANY.isAssignableFrom(UNKNOWN), is(true));
        assertThat(STRING.isAssignableFrom(ANY), is(true));
        assertThat(ANY.isAssignableFrom(STRING), is(true));
    }

    @Test
    public void arrayTypeIsAssignableToArrayType() {
        assertThat(arr(STRING).isAssignableFrom(STRING), is(false));
        assertThat(STRING.isAssignableFrom(arr(STRING)), is(false));

        assertThat(arr(STRING).isAssignableFrom(arr(STRING)), is(true));
        assertThat(arr(STRING).isAssignableFrom(arr(ANY)), is(true));
        assertThat(arr(STRING).isAssignableFrom(arr(UNKNOWN)), is(false));

        assertThat(arr(ANY).isAssignableFrom(ANY), is(true));

        assertThat(arr(ANY).isAssignableFrom(arr(ANY)), is(true));
        assertThat(arr(ANY).isAssignableFrom(arr(UNKNOWN)), is(true));
        assertThat(arr(ANY).isAssignableFrom(arr(STRING)), is(true));

        assertThat(arr(UNKNOWN).isAssignableFrom(arr(ANY)), is(true));
        assertThat(arr(UNKNOWN).isAssignableFrom(arr(UNKNOWN)), is(true));
        assertThat(arr(UNKNOWN).isAssignableFrom(arr(STRING)), is(false));
    }

    @Test
    public void typeRefShouldBeEquivalentToType() {
        Map<String, Type> allTypes = Map.of("string", STRING);

        assertThat(arr(typeRef("string", allTypes)).isAssignableFrom(arr(STRING)), is(true));
        assertThat(arr(typeRef("string", allTypes)).isAssignableFrom(arr(ANY)), is(true));

        assertThat(arr(typeRef("string", allTypes)).isAssignableFrom(STRING), is(false));
        assertThat(typeRef("string", allTypes).isAssignableFrom(arr(STRING)), is(false));

        assertThat(arr(typeRef("string", allTypes)).isAssignableFrom(arr(UNKNOWN)), is(false));
        assertThat(arr(ANY).isAssignableFrom(arr(typeRef("string", allTypes))), is(true));
        assertThat(arr(UNKNOWN).isAssignableFrom(arr(typeRef("string", allTypes))), is(false));
    }

    @Test
    public void orTypesShouldBeAssignable() {
        assertThat(NUMBER.isAssignableFrom(union(NUMBER, STRING)), is(true));
        assertThat(NUMBER.isAssignableFrom(union(BOOLEAN, STRING)), is(false));

        assertThat(union(BOOLEAN, STRING).isAssignableFrom(union(STRING, BOOLEAN)), is(true));
        assertThat(union(BOOLEAN, union(STRING, NUMBER)).isAssignableFrom(union(MONEY, DATE)), is(true));

        assertThat(generic().isAssignableFrom(union(BOOLEAN, STRING)), is(true));
        assertThat(union(BOOLEAN, STRING).isAssignableFrom(generic()), is(false));

        assertThat(union(generic(), DATE).isAssignableFrom(union(BOOLEAN, STRING)), is(true));
        assertThat(union(BOOLEAN, DATE).isAssignableFrom(union(generic(), STRING)), is(false));

        assertThat(ANY.isAssignableFrom(union(NUMBER, STRING)), is(true));
        assertThat(union(BOOLEAN, STRING).isAssignableFrom(ANY), is(true));

        assertThat(UNKNOWN.isAssignableFrom(union(NUMBER, STRING)), is(false));
        assertThat(union(BOOLEAN, STRING).isAssignableFrom(UNKNOWN), is(false));

        assertThat(union(arr(STRING), STRING).isAssignableFrom(STRING), is(true));

        Map<String, Type> allTypes = Map.of("string", STRING);

        assertThat(union(typeRef("string", allTypes), NUMBER).isAssignableFrom(STRING), is(true));
        assertThat(union(arr(typeRef("string", allTypes)), MONEY).isAssignableFrom(arr(STRING)), is(true));
    }

    @Test
    public void shouldCreateTypeFromToken() {
        Type coverageType = new Type("Coverage");
        Map<String, Type> globalTypes = Map.of("Coverage", coverageType);

        assertThat(Type.toType("String", globalTypes), equalTo(STRING));
        assertThat(Type.toType("Number", globalTypes), equalTo(NUMBER));
        assertThat(Type.toType("Money", globalTypes), equalTo(MONEY));
        assertThat(Type.toType("Boolean", globalTypes), equalTo(BOOLEAN));
        assertThat(Type.toType("Date", globalTypes), equalTo(DATE));
        assertThat(Type.toType("DateTime", globalTypes), equalTo(DATETIME));
        assertThat(Type.toType("Any", globalTypes), equalTo(ANY));

        assertThat(Type.toType("String[]", globalTypes), equalTo(ArrayType.of(STRING)));
        assertThat(Type.toType("Number[]", globalTypes), equalTo(ArrayType.of(NUMBER)));
        assertThat(Type.toType("Money[]", globalTypes), equalTo(ArrayType.of(MONEY)));
        assertThat(Type.toType("Boolean[]", globalTypes), equalTo(ArrayType.of(BOOLEAN)));
        assertThat(Type.toType("Date[]", globalTypes), equalTo(ArrayType.of(DATE)));
        assertThat(Type.toType("DateTime[]", globalTypes), equalTo(ArrayType.of(DATETIME)));
        assertThat(Type.toType("Any[]", globalTypes), equalTo(ArrayType.of(ANY)));

        assertThat(Type.toType("Any[][]", globalTypes), equalTo(ArrayType.of(ArrayType.of(ANY))));

        assertThat(Type.toType("Coverage", globalTypes), equalTo(coverageType));
        assertThat(Type.toType("Coverage[]", globalTypes), equalTo(ArrayType.of(coverageType)));

        assertThat(Type.toType("<T>", globalTypes), equalTo(new GenericType("T")));
        assertThat(Type.toType("<T>[]", globalTypes), equalTo(ArrayType.of(new GenericType("T"))));

        assertThat(Type.toType("Number | Money", globalTypes), equalTo(new UnionType(NUMBER, MONEY)));
        assertThat(
            Type.toType("(Number[][] | Money)[][]", globalTypes),
            equalTo(
                ArrayType.of(
                    ArrayType.of(
                        new UnionType(ArrayType.of(ArrayType.of(NUMBER)), MONEY)
                    )
                )
            )
        );
    }

    @Test
    public void shouldReturnUnknownTypeIfTokenISUnknown() {
        Type type = Type.toType("Coverage[]", Map.of());
        assertThat(type, equalTo(ArrayType.of(new Type("Coverage", false, false))));
        assertThat(type.isKnown(), is(false));
    }

    @Test
    public void shouldReturnEqualUnionTypesIgnoringSpacing() {
        Type type1 = Type.toType("Coverage | Policy", Map.of());
        Type type2 = Type.toType("Policy|Coverage", Map.of());
        assertThat(type1, equalTo(type2));
    }

    private Type union(Type left, Type right) {
        return new UnionType(left, right);
    }

    private Type typeRef(String typeName, Map<String, Type> allTypes) {
        return new TypeRef(typeName, type -> allTypes.get(type));
    }

    private Type arr(Type t) {
        return ArrayType.of(t);
    }

    private Type generic() {
        return new GenericType("T");
    }
}
