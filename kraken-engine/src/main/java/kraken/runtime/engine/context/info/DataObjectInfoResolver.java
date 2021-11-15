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

import java.util.Collection;

/**
 * Resolves context name and id from supplied data object, which is to be
 * used as data instance for data context
 *
 * @author rimas
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess")
public interface DataObjectInfoResolver {

    /**
     * Resolve context definition name for specified data object instance
     *
     * @param data  data object instance for context
     * @return      context definition name
     */
    String resolveContextNameForObject(Object data);

    /**
     * Resolve context instance id string from specified data object instance
     *
     * @param data  data object instance for context
     * @return      context instance id string
     */
    String resolveContextIdForObject(Object data);

    /**
     * Validates if supplied data object is supported by this resolver implmentation
     *
     * @param data  data object instance for context
     * @return      collection of error messages
     */
    Collection<DataErrorDefinition> validateContextDataObject(Object data);

}
