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

package kraken.runtime.model.rule.payload.derive;

import kraken.model.derive.DefaultingType;
import kraken.model.payload.PayloadType;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.rule.payload.Payload;

public class DefaultValuePayload implements Payload {

    private final CompiledExpression valueExpression;
    private final DefaultingType defaultingType;
    private final PayloadType type;

    public DefaultValuePayload(CompiledExpression valueExpression, DefaultingType defaultingType) {
        this.valueExpression = valueExpression;
        this.defaultingType = defaultingType;
        this.type = PayloadType.DEFAULT;
    }

    public CompiledExpression getValueExpression() {
        return valueExpression;
    }

    public DefaultingType getDefaultingType() {
        return defaultingType;
    }

    @Override
    public PayloadType getType() {
        return type;
    }
}
