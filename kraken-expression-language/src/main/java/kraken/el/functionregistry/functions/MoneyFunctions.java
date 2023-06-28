/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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
package kraken.el.functionregistry.functions;

import javax.money.MonetaryAmount;

import kraken.el.functionregistry.Example;
import kraken.el.functionregistry.ExpressionFunction;
import kraken.el.functionregistry.FunctionDocumentation;
import kraken.el.functionregistry.FunctionLibrary;
import kraken.el.functionregistry.LibraryDocumentation;
import kraken.el.functionregistry.Native;
import kraken.el.functionregistry.ParameterDocumentation;
import kraken.el.functionregistry.ParameterType;
import kraken.el.math.Numbers;

/**
 * @author mulevicius
 */
@SuppressWarnings("squid:S1118")
@LibraryDocumentation(
    name = "Money",
    description = "Functions that operate with Money values.",
    since = "1.0.28"
)
@Native
public class MoneyFunctions implements FunctionLibrary {

    @FunctionDocumentation(
        description = "Coerces money type to number. Can be used with dynamic context, when "
            + "data type is no known in compile time",
        example = {
            @Example("FromMoney(context.preRevision.limitAmount)"),
            @Example(value = "FromMoney(null)", result = "null"),
        }
    )
    @ExpressionFunction("FromMoney")
    public static Number fromMoney(
        @ParameterDocumentation(name = "monetaryAmount") @ParameterType("Money") Object monetaryAmount
    ) {
        if (monetaryAmount instanceof MonetaryAmount) {
            return Numbers.fromMoney(((MonetaryAmount) monetaryAmount));
        }

        return (Number) monetaryAmount;
    }
}
