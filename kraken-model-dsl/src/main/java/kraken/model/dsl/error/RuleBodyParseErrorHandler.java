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

import java.text.MessageFormat;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;

import kraken.model.dsl.Common;
import kraken.model.dsl.KrakenDSL.ARuleContext;
import kraken.model.dsl.KrakenDSL.PayloadContext;

/**
 * Provides hints for the user when he has incorrectly ordered rule body elements in rule DSL.
 * Handled rule body elements are: Priority, When, Description, Payload
 *
 * @author mulevicius
 * @since 1.40.0
 */
public class RuleBodyParseErrorHandler extends BaseParseErrorHandler {

    @Override
    protected String getMessageDetails(Parser recognizer, Token offendingToken) {
        return MessageFormat.format(
            "Expected: {0}, but token found: ''{1}''. "
                + "Maybe you have defined rule body elements in incorrect order? "
                + "Keep in mind that rule body elements must be defined in this order starting from the top: "
                + "Description, Priority, Condition, Payload",
            recognizer.getExpectedTokens().toString(recognizer.getVocabulary()),
            offendingToken.getText()
        );
    }

    @Override
    public boolean isApplicable(Parser recognizer, Token offendingToken) {
        return
            (recognizer.getContext() instanceof ARuleContext || recognizer.getContext() instanceof PayloadContext)
            &&
            (offendingToken.getType() == Common.PRIORITY || offendingToken.getType() == Common.WHEN
                || offendingToken.getType() == Common.DESCRIPTION);
    }
}
