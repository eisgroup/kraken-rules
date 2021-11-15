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
package kraken.el.functionregistry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;

import org.junit.Test;

/**
 * @author mulevicius
 */
public class FunctionRegistryTest {

    @Test
    public void shouldImportFunctionDefinitions() {
        assertFunctionDefinition(f("StringFunction"), "String", "String");
        assertFunctionDefinition(f("BooleanFunction"), "Boolean", "Boolean");
        assertFunctionDefinition(f("NumberFunction"), "Number", "Number");
        assertFunctionDefinition(f("DateFunction"), "Date", "Date");
        assertFunctionDefinition(f("DateTimeFunction"), "DateTime", "DateTime");
        assertFunctionDefinition(f("MoneyFunction"), "Money", "Money");
        assertFunctionDefinition(f("AnyFunction"), "Any", "Any");
        assertFunctionDefinition(f("UnionFunction"), "Number | Money", "Number | Money");
        assertFunctionDefinition(f("GenericTypeFunction"), "<T>", "<T>");
        assertFunctionDefinition(f("GenericArrayTypeFunction"), "<T>[]", "<T>[]");
        assertFunctionDefinition(f("ExplicitTypeFunction"), "ComplexType", "ComplexType");
        assertFunctionDefinition(f("ImplicitTypeFunction"), "ComplexType", "ComplexType");
        assertFunctionDefinition(f("WildcardTypeFunction"), "ComplexType[]", "ComplexType");
        assertFunctionDefinition(f("ExplicitTypeCollectionFunction"), "ComplexType[]", "ComplexType[]");
    }

    @Test
    public void shouldImportTargetSpecificFunction() {
        assertThat(f("TargetSpecificFunction").getExpressionTargets(), hasItem("target.specific"));
    }

    @Test
    public void shouldImportNativeFunction() {
        assertThat(f("NativeFunction").isNativeFunction(), is(true));
    }

    private FunctionDefinition f(String name) {
        return FunctionRegistry.getFunctions().get(new FunctionHeader(name, 1));
    }

    private void assertFunctionDefinition(FunctionDefinition f, String returnType, String parameterType) {
        assertThat(f.getReturnType(), equalTo(returnType));
        assertThat(f.getParameterTypes().get(0), equalTo(parameterType));
    }
}
