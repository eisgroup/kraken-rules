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
package kraken.model.dsl.visitor;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import kraken.model.dsl.FunctionDocBaseVisitor;
import kraken.model.dsl.FunctionDocParser.DocContext;
import kraken.model.dsl.FunctionDocParser.ExampleContext;
import kraken.model.dsl.FunctionDocParser.ParameterContext;
import kraken.model.dsl.FunctionDocParser.SinceContext;
import kraken.model.dsl.FunctionDocParser.TextContext;
import kraken.model.dsl.FunctionDocParser.UnrecognizedContext;
import kraken.model.dsl.error.LineParseCancellationException;
import kraken.model.dsl.model.DSLFunctionDocumentation;
import kraken.model.dsl.model.DSLFunctionExample;
import kraken.model.dsl.model.DSLParameterDocumentation;

/**
 * @author mulevicius
 */
public class DSLFunctionDocumentationVisitor extends FunctionDocBaseVisitor<DSLFunctionDocumentation> {

    @Override
    public DSLFunctionDocumentation visitDoc(DocContext ctx) {
        if(!ctx.tags().unrecognized().isEmpty()) {
            UnrecognizedContext unrecognized = ctx.tags().unrecognized().get(0);
            throw new LineParseCancellationException(
                "Documentation section is not recognized: " + unrecognized.getText(),
                unrecognized.getStart().getLine(),
                unrecognized.getStart().getCharPositionInLine()
            );
        }
        return new DSLFunctionDocumentation(
            normalizeSpaces(ctx.description().text()),
            parseSince(ctx.tags().since()),
            ctx.tags().example().stream().map(this::parseExample).collect(Collectors.toList()),
            ctx.tags().parameter().stream().map(this::parseParameter).collect(Collectors.toList())
        );
    }

    private String parseSince(List<SinceContext> sinceContexts) {
        if(sinceContexts.size() > 1) {
            SinceContext secondSince = sinceContexts.get(1);
            throw new LineParseCancellationException(
                "Only one '@since' section is allowed per function documentation",
                secondSince.getStart().getLine(),
                secondSince.getStart().getCharPositionInLine()
            );
        }
        if(sinceContexts.size() == 1) {
            return normalizeSpaces(sinceContexts.get(0).version);
        }
        return null;
    }

    private DSLFunctionExample parseExample(ExampleContext exampleContext) {
        return new DSLFunctionExample(
            strip(exampleContext.exampleText),
            exampleContext.result != null ? strip(exampleContext.result) : null,
            exampleContext.INVALID_EXAMPLE() == null
        );
    }

    private DSLParameterDocumentation parseParameter(ParameterContext parameterContext) {
        return new DSLParameterDocumentation(
            parameterContext.parameterName.getText(),
            normalizeSpaces(parameterContext.parameterDescription)
        );
    }

    private String strip(TextContext textContext) {
        return textContext.getText().strip();
    }

    private String normalizeSpaces(TextContext textContext) {
        return StringUtils.normalizeSpace(textContext.getText());
    }

}
