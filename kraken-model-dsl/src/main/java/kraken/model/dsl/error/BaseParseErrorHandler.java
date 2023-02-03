/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;

import java.text.MessageFormat;

/**
 * ErrorHandler, that prepares error message vase for the custom dsl parse error handling cases.
 * {@link this#getMessageDetails(Parser, Token)} )} will be inserted right into prepared error message.
 *
 * @author Pavel Surinin
 * @since 1.8.0
 */
public abstract class BaseParseErrorHandler implements ParseErrorHandler {

    protected abstract String getMessageDetails(Parser recognizer, Token offendingToken);

    @Override
    public abstract boolean isApplicable(Parser recognizer, Token offendingToken);

    @Override
    public String getErrorMessage(Parser recognizer, Token token) {
        int line = getTokenLine(token);
        int position = getCharPositionInLine(token);
        CharStream dsl = recognizer.getTokenStream().getTokenSource().getInputStream();
        String formattedDsl = DslErrorFormatter.format(
                dsl.toString(),
                line,
                position,
                position + token.getText().length()
        );
        return MessageFormat.format(
                "Failed to parse DSL at line: ''{0}'', position: ''{1}''.\n"
                        + "{2}.\n"
                        + "Failure occurred in DSL:\n"
                        + "{3}",
                line,
                position,
                getMessageDetails(recognizer, token),
                formattedDsl
        );
    }

    private int getCharPositionInLine(Token token) {
        return token.getCharPositionInLine() + 1;
    }

    private int getTokenLine(Token token) {
        return token.getLine();
    }
}
