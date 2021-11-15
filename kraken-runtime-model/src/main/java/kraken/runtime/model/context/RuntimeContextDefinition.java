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

package kraken.runtime.model.context;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Represents runtime data context definition on which rules are defined and executed.
 *
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class RuntimeContextDefinition implements Serializable {

    private static final long serialVersionUID = -7180954738541025942L;

    private final String name;
    private final Map<String, ContextNavigation> children;
    private final Map<String, ContextField> fields;
    private final Collection<String> inheritedContexts;

    /**
     * Creates a new instance of {@code RuntimeContextDefinition} with given arguments.
     *
     * @param name              Unique identifier of this context definition.
     * @param children          Children of this context definition.
     * @param fields            Fields defined on this context definition.
     * @param inheritedContexts Names of parent context definitions.
     */
    public RuntimeContextDefinition(
            String name,
            Map<String, ContextNavigation> children,
            Map<String, ContextField> fields,
            Collection<String> inheritedContexts) {
        this.name = name;
        this.children = children;
        this.fields = fields;
        this.inheritedContexts = inheritedContexts;
    }

    public String getName() {
        return name;
    }

    public Map<String, ContextNavigation> getChildren() {
        return children;
    }

    public Map<String, ContextField> getFields() {
        return fields;
    }

    public Collection<String> getInheritedContexts() {
        return inheritedContexts;
    }

}
