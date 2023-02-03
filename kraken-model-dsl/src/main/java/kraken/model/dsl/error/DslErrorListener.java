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
package kraken.model.dsl.error;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

/**
 * Collects syntax errors that occurs while parsing Kraken DSL
 *
 * @author mulevicius
 */
public class DslErrorListener extends BaseErrorListener {

    private final List<DslError> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        String message = MessageFormat.format(
            "Failed to parse dsl at line: ''{0}'', position: ''{1}'' because of: ''{2}''.",
            line,
            charPositionInLine,
            msg
        );
        errors.add(new DslError(message, line, charPositionInLine));
    }

    public List<DslError> getErrors() {
        return errors;
    }

    public static class DslError {

        private final String message;
        private final int line;
        private final int column;

        public DslError(String message, int line, int column) {
            this.message = message;
            this.line = line;
            this.column = column;
        }

        public String getMessage() {
            return message;
        }

        public int getLine() {
            return line;
        }

        public int getColumn() {
            return column;
        }
    }
}
