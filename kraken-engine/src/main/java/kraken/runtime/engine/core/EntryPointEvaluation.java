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
package kraken.runtime.engine.core;

import java.util.List;

import kraken.runtime.model.rule.RuntimeRule;

/**
 * Represents rule data for a specific entry point.
 *
 * @author rimas
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess")
public class EntryPointEvaluation {

    private final String entryPointName;
    private final List<RuntimeRule> rules;

    /**
     * An ordered list of contextName.fieldName strings
     */
    private final List<String> fieldOrder;

    public EntryPointEvaluation(String entryPointName, List<RuntimeRule> rules, List<String> fieldOrder) {
        this.entryPointName = entryPointName;
        this.rules = rules;
        this.fieldOrder = fieldOrder;
    }

    /**
     *
     * @return full entry point name prefixed with namespace
     */
    public String getEntryPointName() {
        return entryPointName;
    }

    public List<RuntimeRule> getRules() {
        return rules;
    }
    
    public List<String> getFieldOrder() {
        return fieldOrder;
    }

}
