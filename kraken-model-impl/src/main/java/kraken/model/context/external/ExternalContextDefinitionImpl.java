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

/**
 * Default implementation of {@code ExternalContextDefinition}.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
public final class ExternalContextDefinitionImpl implements Serializable, ExternalContextDefinition {

    private static final long serialVersionUID = 3314940192003120368L;

    private String name;
    private String physicalNamespace;

    private Map<String, ExternalContextDefinitionAttribute> attributes;

    public ExternalContextDefinitionImpl() {
        attributes = new HashMap<>();
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
    public Map<String, ExternalContextDefinitionAttribute> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(Map<String, ExternalContextDefinitionAttribute> attributes) {
        this.attributes = attributes;
    }

}
