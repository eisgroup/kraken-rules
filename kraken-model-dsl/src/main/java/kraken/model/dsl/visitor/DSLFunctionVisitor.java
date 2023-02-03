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

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.Interval;
import org.apache.commons.lang3.StringUtils;

import kraken.model.dsl.FunctionDocLexer;
import kraken.model.dsl.FunctionDocParser;
import kraken.model.dsl.FunctionDocParser.DocContext;
import kraken.model.dsl.KrakenDSL.FunctionDocContext;
import kraken.model.dsl.KrakenDSL.FunctionImplementationContext;
import kraken.model.dsl.KrakenDSL.FunctionParameterContext;
import kraken.model.dsl.KrakenDSL.FunctionParametersContext;
import kraken.model.dsl.KrakenDSL.TypeContext;
import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.error.DslErrorListener;
import kraken.model.dsl.error.DslErrorListener.DslError;
import kraken.model.dsl.error.DslErrorStrategy;
import kraken.model.dsl.error.LineParseCancellationException;
import kraken.model.dsl.model.DSLFunction;
import kraken.model.dsl.model.DSLFunctionDocumentation;
import kraken.model.dsl.model.DSLFunctionParameter;

/**
 * Visits function implementation nodes in ANTLR parse tree and parses an instance of {@link DSLFunction} from
 * each function implementation node.
 *
 * @author mulevicius
 */
public class DSLFunctionVisitor extends KrakenDSLBaseVisitor<DSLFunction> {

    private static final String docsPrefix = "/**";
    private static final String docsPostfix = "*/";

    @Override
    public DSLFunction visitFunctionImplementation(FunctionImplementationContext ctx) {
        return new DSLFunction(
            ctx.functionName.getText(),
            toTypeToken(ctx.returnType().type()),
            parameters(ctx.functionParameters()),
            GenericBoundsParser.parseBounds(ctx.genericBounds()),
            ExpressionReader.read(ctx.functionBody),
            parseFunctionDoc(ctx.functionDoc())
        );
    }

    private List<DSLFunctionParameter> parameters(FunctionParametersContext ctx) {
        return ctx != null
            ? ctx.functionParameter().stream().map(this::toParameter).collect(Collectors.toList())
            : List.of();
    }

    private DSLFunctionParameter toParameter(FunctionParameterContext ctx) {
        return new DSLFunctionParameter(ctx.parameterName.getText(), toTypeToken(ctx.parameterType));
    }

    private String toTypeToken(TypeContext ctx) {
        int startIndex = ctx.getStart() != null ? ctx.getStart().getStartIndex() : 0;
        int endIndex = ctx.getStop() != null ? ctx.getStop().getStopIndex() : 0;
        return ctx.getStart().getInputStream().getText(new Interval(startIndex, endIndex));
    }

    private DSLFunctionDocumentation parseFunctionDoc(FunctionDocContext ctx) {
        if(ctx == null) {
            return null;
        }
        String docs = ctx.getText();
        if(docs.startsWith(docsPrefix) && docs.endsWith(docsPostfix)) {
            docs = docs.substring(docsPrefix.length(), docs.length() - docsPostfix.length());
        } else {
            throw new IllegalStateException("Critical error when parsing function docs "
                + "- cannot recognize starting and ending symbols");
        }

        if(StringUtils.isBlank(docs)) {
            return null;
        }

        DslErrorListener listener = new DslErrorListener();
        FunctionDocLexer lexer = new FunctionDocLexer(CharStreams.fromString(docs));
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);
        FunctionDocParser parser = new FunctionDocParser(new CommonTokenStream(lexer));
        parser.setErrorHandler(new DslErrorStrategy());
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        DocContext docContext = parser.doc();

        if(!listener.getErrors().isEmpty()) {
            DslError error = listener.getErrors().get(0);
            Cursor cursor = adjustCursorForDsl(ctx, new Cursor(error.getLine(), error.getColumn()));
            throw new LineParseCancellationException(error.getMessage(), cursor.line, cursor.column);
        }

        try {
            var docsVisitor = new DSLFunctionDocumentationVisitor();
            return docsVisitor.visitDoc(docContext);
        } catch (LineParseCancellationException e) {
            Cursor cursor = adjustCursorForDsl(ctx, new Cursor(e.getLine(), e.getColumn()));
            throw new LineParseCancellationException(e.getMessage(), cursor.line, cursor.column, e);
        }
    }

    /**
     * Moves cursor in relation to overall Kraken DSL position
     *
     * @param ctx
     * @param cursor
     * @return
     */
    private Cursor adjustCursorForDsl(FunctionDocContext ctx, Cursor cursor) {
        return new Cursor(
            ctx.getStart().getLine() + cursor.line - 1,
            cursor.line == 1
                ? ctx.getStart().getCharPositionInLine() + docsPrefix.length() + cursor.column
                : cursor.column
        );
    }

    private static class Cursor {
        int line;
        int column;

        Cursor(int line, int column) {
            this.line = line;
            this.column = column;
        }
    }
}
