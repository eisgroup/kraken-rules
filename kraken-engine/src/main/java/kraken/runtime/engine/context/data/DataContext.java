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
package kraken.runtime.engine.context.data;

import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.engine.context.info.ContextInstanceInfo;

import java.util.*;

/**
 * DTO wrapper for data context object instance
 *
 * @author rimas
 * @since 1.0
 */
public class DataContext {

    /**
     * Identifies particular data context instance
     */
    private String contextId;

    /**
     * Context definition name, identifies context type
     */
    private String contextName;

    /**
     * Underlying data object for context
     */
    private Object dataObject;

    private DataContext parentDataContext;

    private ContextInstanceInfo contextInstanceInfo;

    private transient RuntimeContextDefinition contextDefinition;
    /**
     * References from {@link DataContext} to other {@link DataContext}s.
     */
    private transient Map<String, ExternalDataReference> externalReferences = new HashMap<>();

    public void setContextDefinition(RuntimeContextDefinition contextDefinition) {
        this.contextDefinition = contextDefinition;
    }

    public void setContextName(String contextName) {
        this.contextName = contextName;
    }

    public String getContextName() {
        return contextName;
    }

    public void setDataObject(Object dataObject) {
        this.dataObject = dataObject;
    }

    public Object getDataObject() {
        return dataObject;
    }

    public void setContextId(String contextId) {
        this.contextId = contextId;
    }

    public String getContextId() {
        return contextId;
    }

    public String getIdString() {
        return contextName + ":" + contextId;
    }

    public void setContextInstanceInfo(ContextInstanceInfo contextInstanceInfo) {
        this.contextInstanceInfo = contextInstanceInfo;
    }

    public ContextInstanceInfo getContextInstanceInfo() {
        return contextInstanceInfo;
    }

    public DataContext getParentDataContext() {
        return parentDataContext;
    }

    public void setParentDataContext(DataContext parentDataContext) {
        this.parentDataContext = parentDataContext;
    }

    public Map<String, ExternalDataReference> getExternalReferences() {
        return externalReferences;
    }

    public void setExternalReferences(Map<String, ExternalDataReference> externalReferences) {
        this.externalReferences = externalReferences;
    }

    public RuntimeContextDefinition getContextDefinition() {
        return contextDefinition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DataContext that = (DataContext) o;

        return contextId.equals(that.contextId) &&
                contextName.equals(that.contextName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextId, contextName);
    }

    @Override
    public String toString() {
        return "DataContext[" + contextName + ":" + contextId + "]";
    }

}
