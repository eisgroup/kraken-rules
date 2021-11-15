/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.engine.dto;

import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.runtime.engine.context.info.ContextInstanceInfo;

/**
 * Provides information about the context on which rule was evaluated
 *
 * @author mulevicius
 */
public class ContextFieldInfo {

    private final String contextId;
    private final String contextName;
    private final String fieldName;
    private final String fieldPath;

    public ContextFieldInfo(String contextId, String contextName, String fieldName, String fieldPath) {
        this.contextId = contextId;
        this.contextName = contextName;
        this.fieldName = fieldName;
        this.fieldPath = fieldPath;
    }

    /**
     *
     * @return if of the entity as provided by {@link ContextInstanceInfo#getContextInstanceId()}
     */
    public String getContextId() {
        return contextId;
    }

    /**
     *
     * @return name of context, equal to {@link ContextDefinition#getName()} and {@link Rule#getContext()}
     */
    public String getContextName() {
        return contextName;
    }

    /**
     *
     * @return name of the field on which the rule was applied on, equal to {@link ContextField#getName()} and {@link Rule#getTargetPath()}
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     *
     * @return  actual bean path to the value of this field, equal to {@link ContextField#getFieldPath()}
     */
    public String getFieldPath() {
        return fieldPath;
    }

    @Override
    public String toString() {
        return contextName + ":" + contextId + ":" + fieldName;
    }
}
