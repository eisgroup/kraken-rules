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
package kraken.runtime.engine.evaluation.loop;

import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.model.rule.RuntimeRule;

/**
 * Contains rule with data context.
 *
 * @author mulevicius
 * @since 1.40.0
 */
public class RuleEvaluationInstance {
    private final String namespace;
    private final RuntimeRule rule;
    private final DataContext dataContext;

    public RuleEvaluationInstance(String namespace, RuntimeRule rule, DataContext dataContext) {
        this.namespace = namespace;
        this.rule = rule;
        this.dataContext = dataContext;
    }

    public String getNamespace() {
        return namespace;
    }

    public RuntimeRule getRule() {
        return rule;
    }

    public DataContext getDataContext() {
        return dataContext;
    }

    public Integer getPriority() {
        return rule.getPriority() != null ? rule.getPriority() : 0;
    }
}
