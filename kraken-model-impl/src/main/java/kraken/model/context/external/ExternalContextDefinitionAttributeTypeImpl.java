/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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
package kraken.model.context.external;

import kraken.model.context.Cardinality;

import java.io.Serializable;

/**
 * Default implementation of {@code ExternalContextDefinitionAttributeType}.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
public final class ExternalContextDefinitionAttributeTypeImpl implements Serializable, ExternalContextDefinitionAttributeType {

    private static final long serialVersionUID = 6066793739031945540L;

    private String type;
    private Boolean primitive;
    private Cardinality cardinality;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void setType(String type) {
        this.type = type;
    }

    @Override
    public Boolean getPrimitive() {
        return primitive;
    }

    @Override
    public void setPrimitive(Boolean primitive) {
        this.primitive = primitive;
    }

    @Override
    public Cardinality getCardinality() {
        return cardinality;
    }

    @Override
    public void setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
    }

}
