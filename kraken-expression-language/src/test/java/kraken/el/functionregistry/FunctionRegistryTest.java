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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsCollectionContaining.hasItem;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import kraken.el.functionregistry.documentation.ExampleDoc;
import kraken.el.functionregistry.documentation.FunctionDoc;
import kraken.el.functionregistry.documentation.LibraryDoc;
import kraken.el.functionregistry.documentation.ParameterDoc;

/**
 * @author mulevicius
 */
public class FunctionRegistryTest {

    @Test
    public void shouldImportJavaFunctions() {
        assertJavaFunction(f("StringFunction"), "String", "String");
        assertJavaFunction(f("BooleanFunction"), "Boolean", "Boolean");
        assertJavaFunction(f("NumberFunction"), "Number", "Number");
        assertJavaFunction(f("DateFunction"), "Date", "Date");
        assertJavaFunction(f("DateTimeFunction"), "DateTime", "DateTime");
        assertJavaFunction(f("MoneyFunction"), "Money", "Money");
        assertJavaFunction(f("AnyFunction"), "Any", "Any");
        assertJavaFunction(f("UnionFunction"), "Number | Money", "Number | Money");
        assertJavaFunction(f("GenericTypeFunction"), "<T>", "<T>", Map.of("T", "Date"));
        assertJavaFunction(f("GenericBoundedTypeFunction"), "<T>", "<T>[]", Map.of("T", "String"));
        assertJavaFunction(f("GenericExplicitBoundedTypeFunction"), "<T>", "<T>[]", Map.of("T", "String"));
        assertJavaFunction(f("GenericArrayTypeFunction"), "<T>[]", "<T>[]", Map.of("T", "Any"));
        assertJavaFunction(f("ExplicitTypeFunction"), "ComplexType", "ComplexType");
        assertJavaFunction(f("ImplicitTypeFunction"), "ComplexType", "ComplexType");
        assertJavaFunction(f("WildcardTypeFunction"), "ComplexType[]", "ComplexType");
        assertJavaFunction(f("ExplicitTypeCollectionFunction"), "ComplexType[]", "ComplexType[]");
    }

    @Test
    public void shouldImportFunctionDocs() {
        Collection<LibraryDoc> docs = FunctionRegistry.getLibraryDocs();

        // library
        var maybeDocumented = docs.stream()
            .filter(l -> l.getName().equals(FunctionRegistryTestFunctions.LIBRARY_NAME))
            .findAny();
        assertThat(maybeDocumented.isPresent(), is(true));
        var documented = maybeDocumented.get();

        assertThat(documented.getName(), is(FunctionRegistryTestFunctions.LIBRARY_NAME));
        assertThat(documented.getSince(), is(FunctionRegistryTestFunctions.LIBRARY_VERSION));
        assertThat(documented.getDescription(), is(FunctionRegistryTestFunctions.LIBRARY_DOCUMENTATION));

        // function
        assertThat(documented.getFunctions().isEmpty(), is(false));
        Optional<FunctionDoc> maybeFunction = documented.getFunctions().stream()
            .filter(f -> f.getFunctionHeader().getName().equals(FunctionRegistryTestFunctions.FUNCTION_NAME))
            .findAny();
        assertThat(maybeFunction.isPresent(), is(true));
        FunctionDoc function = maybeFunction.get();

        assertThat(function.getFunctionHeader().getParameterCount(), is(2));
        assertThat(function.getAdditionalInfo(), containsString("note"));
        assertThat(function.getDescription(), is(FunctionRegistryTestFunctions.FUNCTION_DESCRIPTION));
        assertThat(function.getReturnType(), is(FunctionRegistryTestFunctions.FUNCTION_RETURN_TYPE));
        assertThat(function.getSince(), is(FunctionRegistryTestFunctions.FUNCTION_SINCE));
        assertThat(function.getExamples(), hasSize(3));

        // examples
        //   valid example with default values
        var maybeValidExample = function.getExamples().stream()
            .filter(e -> e.getCall().equals(FunctionRegistryTestFunctions.EXAMPLE_CALL_VALID))
            .findAny();
        assertThat(maybeValidExample.isPresent(), is(true));
        ExampleDoc validExample = maybeValidExample.get();

        assertThat("by default example must be valid", validExample.isValidCall(), is(true));
        assertThat("default result must be empty string", validExample.getResult(), nullValue());

        //   example with explicit values
        var maybeExplicitExample = function.getExamples().stream()
            .filter(e -> e.getCall().equals(FunctionRegistryTestFunctions.EXAMPLE_CALL_INVALID))
            .findAny();
        assertThat(maybeExplicitExample.isPresent(), is(true));
        ExampleDoc explicitExample = maybeExplicitExample.get();

        assertThat(explicitExample.getCall(), is(FunctionRegistryTestFunctions.EXAMPLE_CALL_INVALID));
        assertThat(explicitExample.getResult(), is(FunctionRegistryTestFunctions.EXAMPLE_RESULT));
        assertThat(explicitExample.isValidCall(), is(FunctionRegistryTestFunctions.EXAMPLE_VALIDITY));

        // parameters
        // 1st
        Optional<ParameterDoc> maybe1StParam = function.getParameters().stream()
            .filter(p -> p.getName().equals(FunctionRegistryTestFunctions.PARAM_STRING_OR_POLICY))
            .findAny();
        assertThat(maybe1StParam.isPresent(), is(true));
        var firstParam = maybe1StParam.get();
        assertThat(firstParam.getDescription(), is(FunctionRegistryTestFunctions.PARAM_STRING_OR_POLICY_DESCRITPION));
        assertThat(firstParam.getType(), is(FunctionRegistryTestFunctions.STRING_POLICY_TYPE));

        // 2nd
        Optional<ParameterDoc> maybe2ndParam = function.getParameters().stream()
            .filter(p -> p.getName().equals(FunctionRegistryTestFunctions.PARAM_NON_NUL))
            .findAny();
        assertThat(maybe2ndParam.isPresent(), is(true));
        var secondParam = maybe2ndParam.get();
        assertThat(secondParam.getDescription(), nullValue());
        assertThat(secondParam.getType(), is("String"));
    }

    @Test
    public void shouldImportFunctionWithNoSince() {
        Collection<LibraryDoc> docs = FunctionRegistry.getLibraryDocs();

        var maybeDocumented = docs.stream()
            .filter(l -> l.getName().equals(FunctionRegistryTestFunctions.LIBRARY_NAME))
            .findAny();
        assertThat(maybeDocumented.isPresent(), is(true));
        var documented = maybeDocumented.get();

        assertThat(documented.getFunctions().isEmpty(), is(false));
        Optional<FunctionDoc> maybeFunction = documented.getFunctions().stream()
            .filter(f -> f.getFunctionHeader().getName().equals(FunctionRegistryTestFunctions.DOCSNOSINCE))
            .findAny();
        assertThat(maybeFunction.isPresent(), is(true));
        FunctionDoc function = maybeFunction.get();

        assertThat(function.getSince(), is(FunctionRegistryTestFunctions.LIBRARY_VERSION));
    }

    @Test
    public void shouldImportTargetSpecificFunction() {
        assertThat(f("TargetSpecificFunction").getExpressionTargets(), hasItem("target.specific"));
    }

    @Test
    public void shouldReturnFunctionsApplicableForTargetExpression() {
        Map<FunctionHeader, JavaFunction> functionForTarget = FunctionRegistry.getFunctions("target.specific");

        boolean correctFunctionReturned = functionForTarget.entrySet()
            .stream()
            .allMatch(
                entry -> entry.getValue().getExpressionTargets().isEmpty()
                    || entry.getValue().getExpressionTargets().contains("target.specific"));
        long targetSpecificCount = functionForTarget.entrySet().stream()
            .filter(entry -> entry.getValue().getExpressionTargets().contains("target.specific"))
            .count();

        assertTrue(correctFunctionReturned);
        assertEquals(2, targetSpecificCount);
    }

    private JavaFunction f(String name) {
        return FunctionRegistry.getFunctions().get(new FunctionHeader(name, 1));
    }

    private void assertJavaFunction(JavaFunction f, String returnType, String parameterType) {
        assertJavaFunction(f, returnType, parameterType, Map.of());
    }

    private void assertJavaFunction(JavaFunction f, String returnType, String parameterType, Map<String, String> genericTypes) {
        assertThat(f.getReturnType(), equalTo(returnType));
        assertThat(f.getParameterTypes().get(0), equalTo(parameterType));
        for(GenericTypeInfo genericTypeInfo : f.getGenericTypes()) {
            assertThat(genericTypeInfo.getBound(), equalTo(genericTypes.get(genericTypeInfo.getGeneric())));
        }
    }

}
