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

import kraken.model.Expression;

public class DefaultValuePayloadImpl implements DefaultValuePayload {

    private Expression valueExpression;

    private DefaultingType defaultingType;

    // used for deserialization only
    private String type;

    public DefaultValuePayloadImpl() {
        this.type = getPayloadType().getTypeName();
    }

    @Override
    public void setValueExpression(Expression valueExpression) {
        this.valueExpression = valueExpression;
    }

    @Override
    public Expression getValueExpression() {
        return valueExpression;
    }

    @Override
    public void setDefaultingType(DefaultingType defaultingType) {
        this.defaultingType = defaultingType;
    }

    @Override
    public DefaultingType getDefaultingType() {
        return defaultingType;
    }

}
