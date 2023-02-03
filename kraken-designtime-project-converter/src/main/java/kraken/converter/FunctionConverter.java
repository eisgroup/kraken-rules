/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.converter;

import java.util.List;
import java.util.stream.Collectors;

import kraken.converter.translation.KrakenExpressionTranslator;
import kraken.model.Function;
import kraken.model.FunctionParameter;
import kraken.runtime.model.function.CompiledFunction;
import kraken.runtime.model.function.Parameter;

/**
 * Converts designtime {@link Function} to runtime {@link CompiledFunction}
 *
 * @author mulevicius
 */
public class FunctionConverter {

    private final KrakenExpressionTranslator krakenExpressionTranslator;

    public FunctionConverter(KrakenExpressionTranslator krakenExpressionTranslator) {
        this.krakenExpressionTranslator = krakenExpressionTranslator;
    }

    public List<CompiledFunction> convert(List<Function> functions) {
        return functions.stream()
            .map(this::convert)
            .collect(Collectors.toList());
    }

    private CompiledFunction convert(Function function) {
        return new CompiledFunction(
            function.getName(),
            function.getParameters().stream().map(this::convert).collect(Collectors.toList()),
            function.getReturnType(),
            krakenExpressionTranslator.translateFunctionExpression(function)
        );
    }

    private Parameter convert(FunctionParameter parameter) {
        return new Parameter(parameter.getName());
    }
}
