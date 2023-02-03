/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl;

import java.net.URI;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

import kraken.annotations.API;
import kraken.model.dsl.KrakenDSL.KrakenContext;
import kraken.model.dsl.error.DslErrorListener;
import kraken.model.dsl.error.DslErrorListener.DslError;
import kraken.model.dsl.error.DslErrorStrategy;
import kraken.model.dsl.error.LineParseCancellationException;
import kraken.model.dsl.model.DSLModel;
import kraken.model.dsl.visitor.DSLModelVisitor;
import kraken.model.resource.Resource;
import kraken.utils.ResourceUtils;

/**
 * Parses Kraken Model DSL input to {@link Resource} instances
 *
 * @author mulevicius
 */
@API
public class KrakenDSLModelParser {

    private KrakenDSLModelParser() {
    }

    /**
     * @param dsl string to parse
     * @return a resource model that represents DSL contents
     * @throws DSLParsingException if DSL cannot be parsed
     * @throws LineParseCancellationException if DSL cannot be parsed with information about the position
     */
    public static Resource parseResource(String dsl, URI uri) {
        KrakenContext krakenContext = forDSL(dsl);
        DSLModelVisitor visitor = new DSLModelVisitor();
        DSLModel dslModel = visitor.visit(krakenContext);

        return KrakenDSLModelConverter.toResource(dslModel, uri);
    }

    /**
     * can be used only for testing
     * @param dsl
     * @return
     */
    public static Resource parseResource(String dsl) {
        return parseResource(dsl, ResourceUtils.randomResourceUri());
    }

    private static KrakenContext forDSL(String dsl) {
        DslErrorListener listener = new DslErrorListener();
        Common lexer = lexerForExpression(dsl);
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        KrakenDSL parser = new KrakenDSL(tokenStream);
        parser.setErrorHandler(new DslErrorStrategy());
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        KrakenContext krakenContext = parser.kraken();

        if(!listener.getErrors().isEmpty()) {
            DslError error = listener.getErrors().get(0);
            throw new LineParseCancellationException(error.getMessage(), error.getLine(), error.getColumn());
        }

        return krakenContext;
    }

    private static Common lexerForExpression(String expression) {
        CharStream stream = CharStreams.fromString(expression);
        return new CommonLexer(stream);
    }

    static class CommonLexer extends Common {

        public CommonLexer(CharStream input) {
            super(input);
        }

        /**
         *
         * It overrides popMode() of Abstract Lexer to gracefully handle
         * syntactically incorrect expressions when expression has more closing curly braces than open curly braces.
         * Without this change, ANTLR4 get's stuck in infinte loop and crashes with StackOverflowException when parsing expression.
         *
         * @return
         */
        @Override
        public int popMode() {
            return _modeStack.isEmpty()
                ? DEFAULT_MODE
                : super.popMode();
        }
    }
}
