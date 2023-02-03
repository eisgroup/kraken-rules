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
package kraken.model.dsl.error;

import java.util.List;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import kraken.model.dsl.KrakenDSLModelParser;

/**
 * Custom Error handling strategy for {@link KrakenDSLModelParser}.
 * Will not recover when {@link Parser} will call.
 * Instead of recovering will throw {@link ParseCancellationException}
 *
 * @author avasiliauskas
 */
public class DslErrorStrategy extends DefaultErrorStrategy {

    private final ParseErrorHandler defaultErrorHandler = new DefaultParseErrorHandler();

    private final List<ParseErrorHandler> errorHandlerList;

    public DslErrorStrategy() {
        this.errorHandlerList = List.of(new RuleBodyParseErrorHandler());
    }

    /**
     * Instead of recovering will re-throw {@link RecognitionException} wrapped in
     * {@link ParseCancellationException} with error message.
     */
    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        throw new LineParseCancellationException(
                errorMessage(recognizer, e.getOffendingToken()),
                e.getOffendingToken().getLine(),
                e.getOffendingToken().getCharPositionInLine(),
                e
        );
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        throw new LineParseCancellationException(
            errorMessage(recognizer, recognizer.getCurrentToken()),
            recognizer.getCurrentToken().getLine(),
            recognizer.getCurrentToken().getCharPositionInLine()
        );
    }

    @Override
    public void sync(Parser recognizer) throws RecognitionException {
    }

    private String errorMessage(Parser recognizer, Token offendingToken) {
        for (ParseErrorHandler errorHandler : errorHandlerList) {
            if (errorHandler.isApplicable(recognizer, offendingToken)) {
                return errorHandler.getErrorMessage(recognizer, offendingToken);
            }
        }
        return defaultErrorHandler.getErrorMessage(recognizer, offendingToken);
    }
}
