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
package kraken.model.dsl.model;

import java.util.Map;
import java.util.Objects;

/**
 * Represents external context bound to kraken model.
 *
 * @author Tomas Dapkunas
 * @since 1.3.0
 */
public class DSLExternalContext {

    private final Map<String, DSLExternalContext> contexts;
    private final Map<String, String> boundedContextDefinitions;

    public DSLExternalContext(Map<String, DSLExternalContext> contexts, Map<String, String> boundedContextDefinitions) {
        this.contexts = Objects.requireNonNull(contexts);
        this.boundedContextDefinitions = Objects.requireNonNull(boundedContextDefinitions);
    }

    public Map<String, DSLExternalContext> getContexts() {
        return contexts;
    }

    public Map<String, String> getBoundedContextDefinitions() {
        return boundedContextDefinitions;
    }

}
