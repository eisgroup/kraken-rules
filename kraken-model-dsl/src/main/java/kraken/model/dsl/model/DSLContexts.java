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
 * Represents context scope in Kraken DSL which may contain list of contexts
 *
 * @author mulevicius
 */
public class DSLContexts {

    private Collection<DSLContext> contexts;

    private Collection<DSLContexts> contextBlocks;

    public DSLContexts(Collection<DSLContext> contexts, Collection<DSLContexts> contextBlocks) {
        this.contexts = Objects.requireNonNull(contexts);
        this.contextBlocks = Objects.requireNonNull(contextBlocks);
    }

    public Collection<DSLContexts> getContextBlocks() {
        return Collections.unmodifiableCollection(contextBlocks);
    }

    public Collection<DSLContext> getContexts() {
        return Collections.unmodifiableCollection(contexts);
    }
}
