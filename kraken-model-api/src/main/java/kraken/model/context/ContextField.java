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
package kraken.model.context;

import kraken.annotations.API;

/**
 * Models a field attribute on {@link ContextDefinition}. Field attributes can be used to
 * define rules on or referenced in rules expressions.
 * 
 * Context fields are defined only for strict data context definitions.
 *
 * @author rimas
 * @since 1.0
 */
@API
public interface ContextField {

    /**
     * Symbolic field name by which it is referenced in expression or applyTo
     *
     * @return
     */
    String getName();

    /**
     * Cardinality of attribute
     *
     * @return
     */
    Cardinality getCardinality();

    /**
     * Data type of attribute, for not limited to primitive types
     *
     * @return
     */
    String getFieldType();

    /**
     * Physical path to attribute value in context data object
     *
     * @return
     */
    String getFieldPath();

    /**
     * Declares that field can only be used in rule expression or condition but not as a rule target
     */
    boolean isExternal();

    void setExternal(boolean external);

    void setFieldType(String fieldType);

    void setCardinality(Cardinality cardinality);

    void setName(String name);

    void setFieldPath(String fieldPath);
}
