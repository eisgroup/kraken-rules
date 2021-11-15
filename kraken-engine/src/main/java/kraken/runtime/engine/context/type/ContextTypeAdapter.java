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
package kraken.runtime.engine.context.type;

/**
 * This adapter is used when context instance is extracted. It has method to define eligibility
 * and provide method {@link ContextTypeAdapter#getValue(Object)} to extract context instance from wrapper.
 *
 * @author psurinin
 * @since 1.0
 */
public interface ContextTypeAdapter extends TypeAdapter {

    /**
     * Extracts value from sprecific object passed in parameters.
     * e.g. if this is {@link java.util.Optional} type adapter
     * <pre>
     *     return ((Optional) object).create()
     * </pre>
     * @param object    object to extract value from
     * @return          extracted value
     */
    Object getValue(Object object);

}
