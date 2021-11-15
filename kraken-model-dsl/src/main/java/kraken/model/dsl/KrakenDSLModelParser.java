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
import java.util.List;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;

import kraken.annotations.API;
import kraken.model.dsl.error.DSLErrorStrategy;
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
     */
    public static Resource parseResource(String dsl, URI uri) {
        KrakenDSL parser = forDSL(dsl);
        DSLModelVisitor visitor = new DSLModelVisitor();
        DSLModel dslModel = visitor.visit(parser.kraken());

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

    private static KrakenDSL forDSL(String dsl) {
        Common lexer = lexerForExpression(dsl);
        lexer.removeErrorListeners();
        lexer.addErrorListener(DSLErrorListener.getInstance());
        TokenStream tokenStream = new CommonTokenStream(lexer);
        KrakenDSL parser = new KrakenDSL(tokenStream);
        parser.setErrorHandler(new DSLErrorStrategy(List.of()));
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
        parser.removeErrorListeners();
        parser.addErrorListener(DSLErrorListener.getInstance());
        return parser;
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
