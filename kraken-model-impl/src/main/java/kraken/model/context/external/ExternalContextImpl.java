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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Default implementation of {@code ExternalContext}.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
public final class ExternalContextImpl implements Serializable, ExternalContext {

    private static final long serialVersionUID = 3215904002222639172L;

    private Map<String, ExternalContext> contexts;
    private Map<String, ExternalContextDefinitionReference> refExtContextDefinitions;

    private String name;
    private String physicalNamespace;

    public ExternalContextImpl() {
        contexts = new HashMap<>();
        refExtContextDefinitions = new HashMap<>();
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
    public String getPhysicalNamespace() {
        return physicalNamespace;
    }

    @Override
    public void setPhysicalNamespace(String physicalNamespace) {
        this.physicalNamespace = physicalNamespace;
    }

    @Override
    public Map<String, ExternalContext> getContexts() {
        return contexts;
    }

    @Override
    public void setContexts(Map<String, ExternalContext> contexts) {
        this.contexts = contexts;
    }

    @Override
    public Map<String, ExternalContextDefinitionReference> getExternalContextDefinitions() {
        return refExtContextDefinitions;
    }

    @Override
    public void setExternalContextDefinitions(Map<String, ExternalContextDefinitionReference> contextDefinitions) {
        this.refExtContextDefinitions = contextDefinitions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExternalContextImpl that = (ExternalContextImpl) o;

        return Objects.equals(physicalNamespace, that.physicalNamespace)
            && Objects.equals(name, that.name)
            && Objects.equals(refExtContextDefinitions, that.refExtContextDefinitions)
            && Objects.equals(contexts, that.contexts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, physicalNamespace, refExtContextDefinitions, contexts);
    }
}
