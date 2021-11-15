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
package kraken.model.dsl.model;

import java.util.Objects;

/**
 * Represents external context definition field type.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
public class DSLExternalContextDefinitionFieldType {

    private final String type;
    private final Boolean isPrimitive;
    private final DSLCardinality cardinality;

    public DSLExternalContextDefinitionFieldType(String type, Boolean isPrimitive, DSLCardinality cardinality) {
        this.type = Objects.requireNonNull(type);
        this.isPrimitive = Objects.requireNonNull(isPrimitive);
        this.cardinality = Objects.requireNonNull(cardinality);
    }

    public String getType() {
        return type;
    }

    public Boolean isPrimitive() {
        return isPrimitive;
    }

    public DSLCardinality getCardinality() {
        return cardinality;
    }

}
