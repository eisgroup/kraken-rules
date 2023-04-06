/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.engine.context;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;

import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.model.rule.RuntimeRule;

/**
 * @author Mindaugas Ulevicius
 */
public class ContextData {

    private final List<DataContext> contexts;
    private final List<DataContext> allowedContexts;
    private final List<DataContext> forbiddenContexts;

    public ContextData(List<DataContext> contexts, RuntimeRule rule) {
        this.contexts = contexts;
        this.allowedContexts = contexts.stream()
            .filter(c -> !isForbiddenTarget(c, rule.getTargetPath()))
            .collect(Collectors.toList());
        this.forbiddenContexts = contexts.stream()
            .filter(c -> isForbiddenTarget(c, rule.getTargetPath()))
            .collect(Collectors.toList());
    }

    public List<DataContext> getContexts() {
        return contexts;
    }

    public List<DataContext> getAllowedContexts() {
        return allowedContexts;
    }

    public List<DataContext> getForbiddenContexts() {
        return forbiddenContexts;
    }

    private boolean isForbiddenTarget(DataContext dataContext, String fieldName) {
        var field = dataContext.getContextDefinition().getFields().get(fieldName);
        return field != null && BooleanUtils.isTrue(field.getForbidTarget());
    }
}
