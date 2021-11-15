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
package kraken.runtime.engine.context.info;

import kraken.annotations.API;

/**
 * Contains information about particular context data instance, obtained using {@link ContextInstanceInfoResolver}
 * SPI. Should be extended with implementation specific details for each SPI specific implementation
 *
 * @author rimas
 * @since 1.0
 */
@API
public interface ContextInstanceInfo {

    /**
     * Contains data instance id, uniquely identifying this particular data instance among
     * other instances of same context. Should be consistend for timeframe of single validation
     * Can be used by invoking client application to identify and map rule results to particular
     * data object from their domain
     *
     * @return      context instance id
     */
    String getContextInstanceId();

    /**
     * Contains name of the context definition data instance represents, must map to
     * actual context definition name in model
     *
     * @return      context definition name
     */
    String getContextName();

}
