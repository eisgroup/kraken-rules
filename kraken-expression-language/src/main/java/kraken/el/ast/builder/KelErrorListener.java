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

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Logs syntax errors that occurs while parsing KEL Expressions
 *
 * @author mulevicius
 */
public class KelErrorListener extends BaseErrorListener {

    private final List<KelError> errors = new ArrayList<>();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                            String msg, RecognitionException e) {
        String errorMsg = MessageFormat.format(
            "Failed to parse expression at line: ''{0}'', position: ''{1}'' because of: ''{2}''",
            line,
            charPositionInLine,
            msg
        );
        errors.add(new KelError(errorMsg));
    }

    public List<KelError> getErrors() {
        return errors;
    }

    static class KelError {
        private final String message;

        public KelError(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
