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
import kraken.model.KrakenModelItem;

import java.util.Map;

/**
 * Represents single external context definition which can be referred
 * in kraken rules expressions.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
@API
public interface ExternalContextDefinition extends KrakenModelItem {

    /**
     * Returns all attributes of this external context definition.
     *
     * @return Attributes.
     */
    Map<String, ExternalContextDefinitionAttribute> getAttributes();

    void setAttributes(Map<String, ExternalContextDefinitionAttribute> attributes);

}
