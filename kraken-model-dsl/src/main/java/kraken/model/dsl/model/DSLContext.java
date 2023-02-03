/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl.model;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

/**
 * @author mulevicius
 */
public class DSLContext {

    private String name;

    private boolean strict;

    private boolean root;

    private boolean system;

    private Collection<String> inheritedContexts;

    private Collection<DSLContextField> fields;

    private Collection<DSLContextChild> children;

    public DSLContext(String name,
                      boolean strict,
                      boolean root,
                      boolean system,
                      Collection<String> inheritedContexts,
                      Collection<DSLContextField> fields,
                      Collection<DSLContextChild> children) {
        this.name = Objects.requireNonNull(name);
        this.system = system;
        this.strict = strict;
        this.root = root;
        this.inheritedContexts = Objects.requireNonNull(inheritedContexts);
        this.fields = Objects.requireNonNull(fields);
        this.children = Objects.requireNonNull(children);
    }

    public String getName() {
        return name;
    }

    public boolean isStrict() {
        return strict;
    }

    public Collection<String> getInheritedContexts() {
        return Collections.unmodifiableCollection(inheritedContexts);
    }

    public Collection<DSLContextField> getFields() {
        return Collections.unmodifiableCollection(fields);
    }

    public Collection<DSLContextChild> getChildren() {
        return Collections.unmodifiableCollection(children);
    }

    public boolean isRoot() {
        return root;
    }

    public boolean isSystem() {
        return system;
    }
    
}
