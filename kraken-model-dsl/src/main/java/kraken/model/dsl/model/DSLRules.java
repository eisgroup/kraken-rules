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
 * Represents rule scope in Kraken DSL which may contain list of rules
 *
 * @author mulevicius
 */
public class DSLRules {

    private Collection<DSLRule> rules;

    /**
     * Common metadata for all rules; if rule has metadata specified then it will take precedence over common metadata
     */
    private DSLMetadata metadata;

    private Collection<DSLRules> ruleBlocks;

    private boolean serverSideOnly;

    public DSLRules(Collection<DSLRule> rules,
                    DSLMetadata metadata,
                    Collection<DSLRules> ruleBlocks,
                    boolean serverSideOnly) {
        this.rules = Objects.requireNonNull(rules);
        this.metadata = metadata;
        this.ruleBlocks = Objects.requireNonNull(ruleBlocks);
        this.serverSideOnly = serverSideOnly;
    }

    public Collection<DSLRule> getRules() {
        return Collections.unmodifiableCollection(rules);
    }

    public DSLMetadata getMetadata() {
        return metadata;
    }

    public Collection<DSLRules> getRuleBlocks() {
        return Collections.unmodifiableCollection(ruleBlocks);
    }

    public boolean isServerSideOnly() {
        return serverSideOnly;
    }

}
