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
import static org.hamcrest.MatcherAssert.assertThat;

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

        assertThat(MONEY.isAssignableFrom(NUMBER), is(false));
        assertThat(NUMBER.isAssignableFrom(MONEY), is(true));
    }

    @Test
    public void typesShouldBeAssignableFromBoundedGenericType() {
        assertThat(STRING.isAssignableFrom(generic(STRING)), is(true));
        assertThat(arr(STRING).isAssignableFrom(generic(arr(STRING))), is(true));
        assertThat(STRING.isAssignableFrom(generic(arr(STRING))), is(false));
        assertThat(union(DATE, DATETIME).isAssignableFrom(generic(DATE)), is(true));

        assertThat(MONEY.isAssignableFrom(generic(NUMBER)), is(false));
        assertThat(NUMBER.isAssignableFrom(generic(MONEY)), is(true));

        assertThat(ANY.isAssignableFrom(generic(STRING)), is(true));
    }

    @Test
    public void typesShouldBeAssignableToBoundedGenericType() {
        assertThat(generic(STRING).isAssignableFrom(STRING), is(true));
        assertThat(generic(arr(STRING)).isAssignableFrom(arr(STRING)), is(true));
        assertThat(generic(STRING).isAssignableFrom(arr(STRING)), is(false));
        assertThat(generic(union(DATE, DATETIME)).isAssignableFrom(DATE), is(true));

        assertThat(generic(MONEY).isAssignableFrom(NUMBER), is(false));
        assertThat(generic(NUMBER).isAssignableFrom(MONEY), is(true));

        assertThat(generic(STRING).isAssignableFrom(ANY), is(true));
    }

    @Test
    public void noTypesShouldBeAssignableToUnboundedGenericType() {
        assertThat(generic().isAssignableFrom(STRING), is(false));
        assertThat(generic().isAssignableFrom(arr(STRING)), is(false));
        assertThat(generic().isAssignableFrom(UNKNOWN), is(false));

        assertThat(generic().isAssignableFrom(ANY), is(true));
    }

    @Test
    public void noTypesShouldBeAssignableFromUnboundedGenericType() {
        assertThat(STRING.isAssignableFrom(generic()), is(false));
        assertThat(arr(STRING).isAssignableFrom(arr(generic())), is(false));
        assertThat(UNKNOWN.isAssignableFrom(generic()), is(false));

        assertThat(ANY.isAssignableFrom(generic()), is(true));
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
    public void unionTypesShouldBeAssignable() {
        assertThat(NUMBER.isAssignableFrom(union(NUMBER, STRING)), is(true));
        assertThat(NUMBER.isAssignableFrom(union(BOOLEAN, STRING)), is(false));

        assertThat(union(BOOLEAN, STRING).isAssignableFrom(union(STRING, BOOLEAN)), is(true));
        assertThat(union(BOOLEAN, union(STRING, NUMBER)).isAssignableFrom(union(MONEY, DATE)), is(true));

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
    public void shouldReturnUnknownTypeIfTokenIsUnknown() {
        Type type = Type.toType("Coverage[]", Map.of());
        assertThat(type.isKnown(), is(false));
    }

    @Test
    public void shouldReturnEqualUnionTypesIgnoringSpacing() {
        Type type1 = Type.toType("Coverage | Policy", Map.of());
        Type type2 = Type.toType("Policy|Coverage", Map.of());
        assertThat(type1, equalTo(type2));
    }

    @Test
    public void shouldUnwrapArrayType() {
        assertThat(STRING.unwrapArrayType(), equalTo(STRING));
        assertThat(ANY.unwrapArrayType(), equalTo(ANY));
        assertThat(UNKNOWN.unwrapArrayType(), equalTo(UNKNOWN));
        assertThat(arr(STRING).unwrapArrayType(), equalTo(STRING));
        assertThat(arr(arr(STRING)).unwrapArrayType(), equalTo(arr(STRING)));
        assertThat(union(STRING, NUMBER).unwrapArrayType(), equalTo(union(STRING, NUMBER)));
        assertThat(union(arr(STRING), arr(NUMBER)).unwrapArrayType(), equalTo(union(STRING, NUMBER)));
        assertThat(union(ANY, STRING).unwrapArrayType(), equalTo(ANY));
        assertThat(union(arr(STRING), STRING).unwrapArrayType(), equalTo(STRING));
        assertThat(arr(union(STRING, arr(STRING))).unwrapArrayType(), equalTo(union(STRING, arr(STRING))));
    }

    @Test
    public void shouldWrapArrayType() {
        assertThat(STRING.wrapArrayType(), equalTo(arr(STRING)));
        assertThat(ANY.wrapArrayType(), equalTo(ANY));
        assertThat(UNKNOWN.wrapArrayType(), equalTo(UNKNOWN));
        assertThat(arr(STRING).wrapArrayType(), equalTo(arr(STRING)));
        assertThat(union(STRING, NUMBER).wrapArrayType(), equalTo(union(arr(STRING), arr(NUMBER))));
        assertThat(union(STRING, arr(NUMBER)).wrapArrayType(), equalTo(union(arr(STRING), arr(NUMBER))));
        assertThat(union(arr(STRING), arr(NUMBER)).wrapArrayType(), equalTo(union(arr(STRING), arr(NUMBER))));
        assertThat(union(ANY, STRING).wrapArrayType(), equalTo(ANY));
    }

    @Test
    public void shouldCalculateIfTypeIsAssignableToArray() {
        assertThat(STRING.isAssignableToArray(), is(false));
        assertThat(ANY.isAssignableToArray(), is(true));
        assertThat(UNKNOWN.isAssignableToArray(), is(false));
        assertThat(arr(STRING).isAssignableToArray(), is(true));
        assertThat(union(STRING, NUMBER).isAssignableToArray(), is(false));
        assertThat(union(STRING, arr(NUMBER)).isAssignableToArray(), is(true));
        assertThat(union(arr(STRING), arr(NUMBER)).isAssignableToArray(), is(true));
        assertThat(union(ANY, STRING).isAssignableToArray(), is(true));
        assertThat(arr(union(STRING, arr(STRING))).isAssignableToArray(), is(true));
    }

    @Test
    public void shouldMap() {
        assertThat(ANY.mapTo(STRING), is(STRING));
        assertThat(UNKNOWN.mapTo(STRING), is(STRING));
        assertThat(NUMBER.mapTo(STRING), is(STRING));
        assertThat(NUMBER.mapTo(arr(STRING)), is(arr(STRING)));
        assertThat(NUMBER.mapTo(arr(arr(STRING))), is(arr(arr(STRING))));
        assertThat(arr(NUMBER).mapTo(STRING), is(arr(STRING)));
        assertThat(arr(NUMBER).mapTo(arr(STRING)), is(arr(arr(STRING))));
        assertThat(arr(arr(NUMBER)).mapTo(STRING), is(arr(arr(STRING))));

        assertThat(arr(NUMBER).mapTo(union(STRING, arr(STRING))), is(arr(union(STRING, arr(STRING)))));
        assertThat(union(NUMBER, arr(NUMBER)).mapTo(union(STRING, arr(STRING))), is(arr(union(STRING, arr(STRING)))));
        assertThat(arr(union(NUMBER, BOOLEAN)).mapTo(STRING), is(arr(STRING)));
        assertThat(arr(union(NUMBER, arr(BOOLEAN))).mapTo(STRING), is(arr(union(STRING, arr(STRING)))));
        assertThat(union(NUMBER, BOOLEAN).mapTo(STRING), is(STRING));
        assertThat(union(NUMBER, arr(BOOLEAN)).mapTo(STRING), is(union(STRING, arr(STRING))));

        assertThat(NUMBER.mapTo(ArrayType.of(UNKNOWN)), is(ArrayType.of(UNKNOWN)));
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

    private Type generic(Type bound) {
        return new GenericType("T", bound);
    }
}
