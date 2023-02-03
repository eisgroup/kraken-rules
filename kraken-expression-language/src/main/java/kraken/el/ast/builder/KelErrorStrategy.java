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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.Vocabulary;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.misc.IntervalSet;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import kraken.el.Kel;

/**
 * Custom Error handling strategy for {@link AstBuilder}.
 * Will not recover when {@link Parser} will call.
 * Instead of recovering will throw {@link ParseCancellationException}
 *
 * @author avasiliauskas
 */
public class KelErrorStrategy extends DefaultErrorStrategy {

    /**
     * Instead of recovering will re-throw {@link RecognitionException} wrapped in
     * {@link ParseCancellationException} with error message.
     */
    @Override
    public void recover(Parser recognizer, RecognitionException e) {
        throw new ParseCancellationException(errorMessage(recognizer), e);
    }

    @Override
    public Token recoverInline(Parser recognizer) throws RecognitionException {
        throw new ParseCancellationException(errorMessage(recognizer));
    }

    @Override
    protected void reportInputMismatch(Parser recognizer, InputMismatchException e) {
        super.reportInputMismatch(recognizer, e);
    }

    private String errorMessage(Parser recognizer) {
        Token token = recognizer.getCurrentToken();
        String msg = MessageFormat.format(
            "expected: {0}, but token found: {1}",
            getReadableExpectations(recognizer.getVocabulary(), recognizer.getExpectedTokens()),
            getTokenErrorDisplay(token)
        );
        int line = token.getLine();
        int position = token.getCharPositionInLine() + 1;
        return MessageFormat.format(
            "Failed to parse expression at line: ''{0}'', position: ''{1}'', {2}", line, position, msg
        );
    }

    private Collection<String> getReadableExpectations(Vocabulary vocabulary, IntervalSet expectedTokens) {
        var expectations = new ArrayList<String>();
        for (Interval interval : expectedTokens.getIntervals()) {
            if (interval.a == interval.b) {
                expectations.addAll(resolveText(vocabulary, interval.a));
            } else {
                for (int i = interval.a; i <= interval.b; i++) {
                    expectations.addAll(resolveText(vocabulary, i));
                }
            }
        }
        return expectations;
    }

    private Collection<String> resolveText(Vocabulary vocabulary, int tokenType) {
        var literalName = vocabulary.getLiteralName(tokenType);
        if (literalName != null) {
            return List.of(literalName);
        } else {
            switch (tokenType) {
                case Kel.OP_IN:
                case Kel.IN:
                    return q("in");
                case Kel.DOT:
                    return q(".");
                case Kel.ELSE:
                    return q("else");
                case Kel.EVERY:
                    return q("every");
                case Kel.FALSE:
                    return q("false");
                case Kel.FOR:
                    return q("for");
                case Kel.IF:
                    return q("if");
                case Kel.MATCHES:
                    return q("matches");
                case Kel.NOT:
                    return q("not");
                case Kel.NULL:
                    return q("null");
                case Kel.OP_ADD:
                    return q("+");
                case Kel.OP_AND:
                    return q("and", "&&");
                case Kel.OP_DIV:
                    return q("/");
                case Kel.OP_EQUALS:
                    return q("=", "==");
                case Kel.OP_EXP:
                    return q("**");
                case Kel.OP_INSTANCEOF:
                    return q("instanceof");
                case Kel.OP_LESS:
                    return q("<");
                case Kel.OP_LESS_EQUALS:
                    return q("<=");
                case Kel.OP_MINUS:
                    return q("-");
                case Kel.OP_MOD:
                    return q("%");
                case Kel.OP_MORE:
                    return q(">");
                case Kel.OP_MORE_EQUALS:
                    return q(">=");
                case Kel.OP_MULT:
                    return q("*");
                case Kel.OP_NEGATION:
                    return q("not", "!");
                case Kel.OP_NOT_EQUALS:
                    return q("!=");
                case Kel.OR:
                case Kel.OP_OR:
                    return q("or", "||");
                case Kel.OP_PIPE:
                    return q("|");
                case Kel.OP_QUESTION:
                    return q("?");
                case Kel.OP_TYPEOF:
                    return q("typeof");
                case Kel.RETURN:
                    return q("return");
                case Kel.SATISFIES:
                    return q("satisfies");
                case Kel.THIS:
                    return q("this");
                case Kel.TRUE:
                    return q("true");
            }
        }
        return List.of();
    }

    private Collection<String> q(String... strings) {
        return Arrays.stream(strings).sequential().map(s -> "'" + s + "'").collect(Collectors.toList());
    }
}
