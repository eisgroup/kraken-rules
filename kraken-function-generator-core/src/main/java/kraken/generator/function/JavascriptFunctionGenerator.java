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
package kraken.generator.function;

import java.util.List;
import java.util.stream.Collectors;

import kraken.converter.KrakenProjectConverter;
import kraken.el.TargetEnvironment;
import kraken.model.project.KrakenProject;
import kraken.runtime.model.function.CompiledFunction;

/**
 * @author mulevicius
 */
public class JavascriptFunctionGenerator {

    public List<KelFunction> generate(KrakenProject krakenProject) {
        KrakenProjectConverter converter = new KrakenProjectConverter(krakenProject, TargetEnvironment.JAVASCRIPT);
        return converter.convert().getFunctions().values()
            .stream()
            .map(this::toFunction)
            .collect(Collectors.toList());
    }

    private KelFunction toFunction(CompiledFunction function) {
        return new KelFunction(
            function.getName(),
            function.getParameters().stream()
                .map(p -> new FunctionParameter(p.getName()))
                .collect(Collectors.toList()),
            function.getBody().getExpressionString()
        );
    }

}
