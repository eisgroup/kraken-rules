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
package kraken.model.derive;

import kraken.annotations.API;
import kraken.model.Expression;
import kraken.model.payload.PayloadType;

/**
 * Used to default field to value generated using provided value expression
 */
@API
public interface DefaultValuePayload extends DerivePayload {
    void setValueExpression(Expression valueExpression);

    /**
     * Expression, used to generate value to be set to model
     */
    Expression getValueExpression();

    void setDefaultingType(DefaultingType defaultingType);

    /**
     * Defines how logic is applied to model
     */
    DefaultingType getDefaultingType();

    @Override
    default PayloadType getPayloadType() {
        return PayloadType.DEFAULT;
    }
}
