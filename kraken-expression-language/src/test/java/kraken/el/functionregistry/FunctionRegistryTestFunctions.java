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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import javax.money.MonetaryAmount;

/**
 * @author mulevicius
 */
@LibraryDocumentation(
    name = FunctionRegistryTestFunctions.LIBRARY_NAME,
    description = FunctionRegistryTestFunctions.LIBRARY_DOCUMENTATION,
    since = FunctionRegistryTestFunctions.LIBRARY_VERSION
)
public class FunctionRegistryTestFunctions implements FunctionLibrary {

    public static final String LIBRARY_NAME = "Test Functions";
    public static final String LIBRARY_VERSION = "1.0.0";
    public static final String LIBRARY_DOCUMENTATION = "library documentation";
    public static final String FUNCTION_NAME = "Document";
    public static final String FUNCTION_DESCRIPTION = "description";
    public static final String FUNCTION_SINCE = "1.1.1";
    public static final String EXAMPLE_CALL_INVALID = "Document(null)";
    public static final String EXAMPLE_RESULT = "null will produce an error";
    public static final boolean EXAMPLE_VALIDITY = false;
    public static final String EXAMPLE_CALL_VALID = "Document('1')";
    public static final String FUNCTION_RETURN_TYPE = "String | Policy";
    public static final String DOCSNOSINCE = "docsnosince";
    public static final String PARAM_STRING_OR_POLICY = "stringOrPolicy";
    public static final String PARAM_STRING_OR_POLICY_DESCRITPION = "can be instance of policy or path to policy";
    public static final String PARAM_NON_NUL = "NON_NUL";
    public static final String STRING_POLICY_TYPE = "String | Policy";

    @FunctionDocumentation(
        description = FUNCTION_DESCRIPTION,
        since = FUNCTION_SINCE,
        example = {
            @Example(EXAMPLE_CALL_VALID),
            @Example(value = "Document('test')", result = "nothing will happen"),
            @Example(value = EXAMPLE_CALL_INVALID, result = EXAMPLE_RESULT, validCall = EXAMPLE_VALIDITY),
        }
    )
    @ExpressionFunction(FUNCTION_NAME)
    @ReturnType(FUNCTION_RETURN_TYPE)
    public static Object docs(
        @ParameterType(STRING_POLICY_TYPE)
        @ParameterDocumentation(
            name = PARAM_STRING_OR_POLICY,
            description = PARAM_STRING_OR_POLICY_DESCRITPION
        )
            Object stringOrPolicy,
        @NotNull @ParameterDocumentation(name = PARAM_NON_NUL) String notnullParam
    ) {
        return stringOrPolicy;
    }

    @FunctionDocumentation(
        description = "description",
        example = {
            @Example(value = "nocsnosince()"),
        }
    )
    @ExpressionFunction(DOCSNOSINCE)
    public static Object docsnosince() {
        return "stringOrPolicy";
    }

    @ExpressionFunction("StringFunction")
    public static String stringFunction(String param) {
        return param;
    }

    @ExpressionFunction("BooleanFunction")
    public static Boolean booleanFunction(Boolean param) {
        return param;
    }

    @ExpressionFunction("NumberFunction")
    public static BigDecimal numberFunction(BigDecimal param) {
        return param;
    }

    @ExpressionFunction("DateFunction")
    public static LocalDate dateFunction(LocalDate param) {
        return param;
    }

    @ExpressionFunction("DateTimeFunction")
    public static LocalDateTime dateTimeFunction(LocalDateTime param) {
        return param;
    }

    @ExpressionFunction("MoneyFunction")
    public static MonetaryAmount moneyFunction(MonetaryAmount param) {
        return param;
    }

    @ExpressionFunction("AnyFunction")
    public static Object anyFunction(Object param) {
        return param;
    }

    @Native
    @FunctionDocumentation(
        description = "",
        example = {
            @Example("NativeFunction(a)")
        }
    )
    @ExpressionFunction("NativeFunction")
    public static Object nativeFunction(Object param) {
        return param;
    }

    @ExpressionTarget("target.specific")
    @ExpressionFunction("TargetSpecificFunction")
    public static Object targetSpecificFunction(Object param) {
        return param;
    }

    @ExpressionTarget({"target.specific", "another.target"})
    @ExpressionFunction("OtherTargetSpecificFunction")
    public static Object otherTargetSpecificFunction(Object param) {
        return param;
    }

    @ExpressionTarget({"different.target.specific"})
    @ExpressionFunction("DifferentTargetSpecificFunction")
    public static Object differentTargetSpecificFunction(Object param) {
        return param;
    }

    @ExpressionFunction("UnionFunction")
    public static @ReturnType("Number | Money")
    Object unionFunction(@ParameterType("Number | Money") Object param) {
        return param;
    }

    @ExpressionFunction("GenericTypeFunction")
    public static <T extends LocalDate> T genericTypeFunction(T param) {
        return param;
    }

    @ExpressionFunction("GenericArrayTypeFunction")
    public static <T> Collection<T> genericArrayTypeFunction(Collection<T> param) {
        return param;
    }

    @ExpressionFunction("GenericBoundedTypeFunction")
    public static <T extends String> T genericBoundedTypeFunction(Collection<T> param) {
        return param.iterator().next();
    }

    @ExpressionFunction(
        value = "GenericExplicitBoundedTypeFunction",
        genericTypes = @GenericType(generic = "T", bound = "String")
    )
    public static <T> T genericExplicitBoundedTypeFunction(Collection<T> param) {
        return param.iterator().next();
    }

    @ExpressionFunction("ExplicitTypeFunction")
    @ReturnType("ComplexType")
    public static Object explicitTypeFunction(@ParameterType("ComplexType") Object param) {
        return param;
    }

    @ExpressionFunction("ImplicitTypeFunction")
    public static ComplexType implicitTypeFunction(ComplexType param) {
        return param;
    }

    @ExpressionFunction("WildcardTypeFunction")
    public static List<? extends ComplexType> wildcardTypeFunction(ComplexType param) {
        return List.of(param);
    }

    @ExpressionFunction("ExplicitTypeCollectionFunction")
    @ReturnType("ComplexType[]")
    public static Collection<ComplexType> explicitTypeCollectionFunction(
        @ParameterType("ComplexType[]") Collection<ComplexType> param) {
        return param;
    }

    public static class ComplexType {

    }
}
