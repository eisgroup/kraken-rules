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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("WeakerAccess")
public class ContextDefinitionImpl implements ContextDefinition, Serializable {

    private boolean root;

    private String name;

    private String physicalNamespace;

    private boolean strict;

    private boolean system;

    private Map<String, ContextNavigation> children;

    private Map<String, ContextField> contextFields;

    private Collection<String> parentDefinitions;

    public ContextDefinitionImpl() {
        this.children = new HashMap<>();
        this.contextFields = new HashMap<>();
        this.parentDefinitions = new ArrayList<>();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isStrict() {
        return strict;
    }

    @Override
    public void setStrict(boolean strict) {
        this.strict = strict;
    }

    @Override
    public boolean isSystem() {
        return system;
    }

    @Override
    public void setSystem(boolean system) {
        this.system = system;
    }

    @Override
    public void setChildren(Map<String, ContextNavigation> children) {
        this.children = children;
    }

    @Override
    public Map<String, ContextNavigation> getChildren() {
        return children;
    }

    @Override
    public void setContextFields(Map<String, ContextField> contextFields) {
        this.contextFields = contextFields;
    }

    @Override
    public Map<String, ContextField> getContextFields() {
        return contextFields;
    }

    @Override
    public void setParentDefinitions(Collection<String> parentDefinition) {
        this.parentDefinitions = parentDefinition;
    }

    @Override
    public Collection<String> getParentDefinitions() {
        return parentDefinitions;
    }

    @Override
    public boolean isRoot() {
        return root;
    }

    @Override
    public boolean setRoot(boolean isRoot) {
        return root = isRoot;
    }

    @Override
    public String getPhysicalNamespace() {
        return physicalNamespace;
    }

    @Override
    public void setPhysicalNamespace(String name) {
        this.physicalNamespace = name;
    }

    @Override
    public String toString() {
        return this.getFullName();
    }
}
