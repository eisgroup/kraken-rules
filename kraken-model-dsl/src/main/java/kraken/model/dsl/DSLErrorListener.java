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

import org.antlr.v4.runtime.DiagnosticErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Logs syntax errors that occurs while parsing Kraken DSL
 *
 * @author mulevicius
 */
public class DSLErrorListener extends DiagnosticErrorListener {

    private final Logger logger = LoggerFactory.getLogger(DSLErrorListener.class);

    private static final DSLErrorListener INSTANCE = new DSLErrorListener();

    private DSLErrorListener() {
        super(true);
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        if(e != null) {
            String message = MessageFormat.format(
                    "Failed to parse dsl at line: ''{0}'', position: ''{1}'' because of: ''{2}''.", line, charPositionInLine, msg
            );
            logger.error(message);
        }
    }

    static DSLErrorListener getInstance() {
        return INSTANCE;
    }
}
