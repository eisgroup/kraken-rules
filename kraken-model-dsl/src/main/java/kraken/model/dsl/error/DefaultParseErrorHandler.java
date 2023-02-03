/*
 *  Copyright 2021 EIS Ltd and/or one of its affiliates.
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

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;

import java.text.MessageFormat;

/**
 * @author Pavel Surinin
 * @since 1.8.0
 */
public class DefaultParseErrorHandler extends BaseParseErrorHandler {

    public DefaultParseErrorHandler() {
    }

    @Override
    protected String getMessageDetails(Parser recognizer, Token offendingToken) {
        return MessageFormat.format(
                "Expected: {0}, but token found: ''{1}''",
                recognizer.getExpectedTokens().toString(recognizer.getVocabulary()),
                offendingToken.getText()
        );
    }

    @Override
    public boolean isApplicable(Parser recognizer, Token offendingToken) {
        return true;
    }
}
