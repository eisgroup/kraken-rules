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
package kraken.model.validation;

import kraken.annotations.API;
import kraken.model.Expression;
import kraken.model.payload.PayloadType;

/**
 * Allows specifying arbitrary validation logic in form of boolean expression.
 * Expression is ran against context instance - if it yields false, field is considered
 * invalid. Otherwise, field is considered to be valid.
 *
 * @author rimas
 * @since 1.0
 */
@API
 public interface AssertionPayload extends ValidationPayload{

    Expression getAssertionExpression();

    void setAssertionExpression(Expression assertionExpression);

    @Override
    default PayloadType getPayloadType() {
        return PayloadType.ASSERTION;
    }
}
