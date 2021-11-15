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

import kraken.annotations.API;
import kraken.model.context.Cardinality;

/**
 * Represents a type of external context definition attribute.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
@API
public interface ExternalContextDefinitionAttributeType {

    /**
     * Returns actual type of {@code this} attribute. For supported primitive type values
     * see {@link kraken.model.context.PrimitiveFieldDataType}.
     *
     * @return Type.
     */
    String getType();

    void setType(String type);

    /**
     * Returns whether {@code this} attribute is primitive or not. For supported primitive type
     * values see {@link kraken.model.context.PrimitiveFieldDataType}.
     *
     * @return {@code true} if primitive.
     */
    Boolean getPrimitive();

    void setPrimitive(Boolean primitive);

    /**
     * Returns cardinality of {@code this} attribute.
     *
     * @return Cardinality.
     */
    Cardinality getCardinality();

    void setCardinality(Cardinality cardinality);

}
