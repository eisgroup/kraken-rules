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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;

/**
 * Logs syntax errors that occurs while parsing Kraken Expressions
 *
 * @author mulevicius
 */
public class AstBuilderErrorListener extends BaseErrorListener {

    private final Logger logger = LoggerFactory.getLogger(AstBuilderErrorListener.class);

    private static final AstBuilderErrorListener INSTANCE = new AstBuilderErrorListener();

    private AstBuilderErrorListener() {
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e) {
        if(e != null){
            String errorMsg = MessageFormat.format(
                    "Failed to parse expression at line: ''{0}'', position: ''{1}'' because of: ''{2}''.", line, charPositionInLine, msg
            );
            logger.error(errorMsg);
        }
    }

    static AstBuilderErrorListener getInstance() {
        return INSTANCE;
    }
}
