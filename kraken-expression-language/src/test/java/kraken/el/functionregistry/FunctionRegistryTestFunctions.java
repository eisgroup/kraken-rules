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
public class FunctionRegistryTestFunctions implements FunctionLibrary {

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
    @ExpressionFunction("NativeFunction")
    public static Object nativeFunction(Object param) {
        return param;
    }

    @ExpressionTarget("target.specific")
    @ExpressionFunction("TargetSpecificFunction")
    public static Object targetSpecificFunction(Object param) {
        return param;
    }

    @ExpressionFunction("UnionFunction")
    public static @ReturnType("Number | Money") Object unionFunction(@ParameterType("Number | Money") Object param) {
        return param;
    }

    @ExpressionFunction("GenericTypeFunction")
    public static <T> T genericTypeFunction(T param) {
        return param;
    }

    @ExpressionFunction("GenericArrayTypeFunction")
    public static <T> Collection<T> genericArrayTypeFunction(Collection<T> param) {
        return param;
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
