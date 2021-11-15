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
package kraken.el.ast.builder;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.text.MessageFormat;

/**
 * Custom Error handling strategy for {@link AstBuilder}.
 * Will not recover when {@link Parser} will call.
 * Instead of recovering will throw {@link ParseCancellationException}
 *
 * @author avasiliauskas
 */
public class AstBuilderErrorStrategy extends DefaultErrorStrategy {

    private static final AstBuilderErrorStrategy INSTANCE = new AstBuilderErrorStrategy();

    private AstBuilderErrorStrategy() {
    }

    /**
     * Instead of recovering will re-throw {@link RecognitionException} wrapped in
     * {@link ParseCancellationException} with error message.
     */
    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        throw new ParseCancellationException(errorMessage(recognizer), e);
    }

    /**
     * Instead of recovering will throw {@link ParseCancellationException} with error message.
     */
    @Override
    public Token recoverInline(Parser recognizer) {
        throw new ParseCancellationException(errorMessage(recognizer));
    }

    @Override
    public void sync(Parser recognizer) { }

    public static AstBuilderErrorStrategy getInstance() {
        return INSTANCE;
    }

    private String errorMessage(Parser recognizer) {
        Token token = recognizer.getCurrentToken();
        String msg = MessageFormat.format(
                "expected: {0}, but token found: {1}",
                recognizer.getExpectedTokens().toString(recognizer.getVocabulary()), getTokenErrorDisplay(token)
        );
        int line = token.getLine();
        int position = token.getCharPositionInLine();
        return MessageFormat.format(
                "Failed to parse expression at line: ''{0}'', position: ''{1}'', {2}.", line, position, msg
        );
    }
}
