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

import kraken.model.context.Cardinality;
import kraken.runtime.model.context.RuntimeContextDefinition;
import kraken.runtime.engine.context.info.ContextInstanceInfo;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

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
     * A path to {@code this} data context as provided by {@link kraken.runtime.DataContextPathProvider}.
     */
    private String contextPath;

    /**
     * Underlying data object for context
     */
    private Object dataObject;

    private DataContext parentDataContext;

    private ContextInstanceInfo contextInstanceInfo;

    private RuntimeContextDefinition contextDefinition;

    /**
     * References from {@link DataContext} to other {@link DataContext}s.
     */
    private Map<String, DataReference> dataContextReferences = new HashMap<>();

    private Map<String, Object> objectReferences = new HashMap<>();

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

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    @Nullable
    public String getContextPath() {
        return contextPath;
    }

    public String getContextDescription() {
        return contextPath == null
            ? getIdString()
            : contextName + ":" + contextPath + ":" + contextId;
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

    public Map<String, DataReference> getDataContextReferences() {
        var selfReference = new DataReference(contextName, List.of(this), Cardinality.SINGLE);
        Map<String, DataReference> dataContextReferencesCopy = new HashMap<>(dataContextReferences);
        dataContextReferencesCopy.put(contextName, selfReference);
        if(contextDefinition != null) {
            for (var inheritedContextName : contextDefinition.getInheritedContexts()) {
                dataContextReferencesCopy.put(inheritedContextName, selfReference);
            }
        }
        return dataContextReferencesCopy;
    }

    public Map<String, Object> getObjectReferences() {
        Map<String, Object> objectReferencesCopy = new HashMap<>(objectReferences);
        objectReferencesCopy.put(contextName, dataObject);
        if(contextDefinition != null) {
            for (var inheritedContextName : contextDefinition.getInheritedContexts()) {
                objectReferencesCopy.put(inheritedContextName, dataObject);
            }
        }
        return objectReferencesCopy;
    }

    public void updateReference(DataReference reference) {
        var objectReference = reference.getCardinality() == Cardinality.SINGLE
            ? (reference.getDataContext() != null ? reference.getDataContext().getDataObject() : null)
            : reference.getDataContexts().stream().map(c -> c.dataObject).collect(Collectors.toList());
        this.objectReferences.put(reference.getName(), objectReference);
        this.dataContextReferences.put(reference.getName(), reference);
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
