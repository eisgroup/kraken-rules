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
package kraken.model.dsl.visitor;


import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.misc.Interval;

import kraken.model.dsl.KrakenDSL.FunctionSignatureContext;
import kraken.model.dsl.KrakenDSL.FunctionSignatureParameterContext;
import kraken.model.dsl.KrakenDSL.FunctionSignatureParametersContext;
import kraken.model.dsl.KrakenDSL.TypeContext;
import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.model.DSLFunctionSignature;
import kraken.model.dsl.model.DSLFunctionSignatureParameter;

/**
 * Visits function signature nodes in ANTLR parse tree and parses an instance of {@link DSLFunctionSignature} from each
 * function signature node.
 *
 * @author mulevicius
 */
public class DSLFunctionSignatureVisitor extends KrakenDSLBaseVisitor<DSLFunctionSignature> {

    @Override
    public DSLFunctionSignature visitFunctionSignature(FunctionSignatureContext ctx) {
        return new DSLFunctionSignature(
            ctx.functionName.getText(),
            toTypeToken(ctx.functionSignatureReturnType().type()),
            parameters(ctx.functionSignatureParameters()),
            GenericBoundsParser.parseBounds(ctx.genericBounds())
        );
    }

    private List<DSLFunctionSignatureParameter> parameters(FunctionSignatureParametersContext ctx) {
        return ctx != null
            ? ctx.functionSignatureParameter().stream().map(this::toParameter).collect(Collectors.toList())
            : List.of();
    }

    private DSLFunctionSignatureParameter toParameter(FunctionSignatureParameterContext ctx) {
        return new DSLFunctionSignatureParameter(toTypeToken(ctx.type()));
    }

    private String toTypeToken(TypeContext ctx) {
        int startIndex = ctx.getStart() != null ? ctx.getStart().getStartIndex() : 0;
        int endIndex = ctx.getStop() != null ? ctx.getStop().getStopIndex() : 0;
        return ctx.getStart().getInputStream().getText(new Interval(startIndex, endIndex));
    }

}
