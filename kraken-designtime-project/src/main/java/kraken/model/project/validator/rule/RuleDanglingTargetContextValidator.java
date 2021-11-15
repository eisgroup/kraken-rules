/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.validator.rule;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

/**
 * @author mulevicius
 */
public class RuleDanglingTargetContextValidator {

    private final Set<String> accessibleContextsFromRoot = new HashSet<>();

    private final KrakenProject krakenProject;

    public RuleDanglingTargetContextValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;

        ContextDefinition root = krakenProject.getContextDefinitions().get(krakenProject.getRootContextName());
        collectAccessibleContextDefinitions(root);
    }

    public void validate(Rule rule, ValidationSession session) {
        if(!accessibleContextsFromRoot.contains(rule.getContext())) {
            String template = "applied on ContextDefinition ''{0}'' which is not related to Root Context ''{1}''";
            String message = MessageFormat.format(template, rule.getContext(), krakenProject.getRootContextName());
            session.add(new ValidationMessage(rule, message, Severity.ERROR));
        }
    }

    private void collectAccessibleContextDefinitions(ContextDefinition contextDefinition) {
        if(accessibleContextsFromRoot.contains(contextDefinition.getName())) {
            return;
        }

        accessibleContextsFromRoot.add(contextDefinition.getName());

        contextDefinition.getParentDefinitions().stream()
                .map(inherited -> krakenProject.getContextDefinitions().get(inherited))
                .forEach(inheritedContext -> collectAccessibleContextDefinitions(inheritedContext));

        contextDefinition.getChildren().values().stream()
                .map(child -> krakenProject.getContextDefinitions().get(child.getTargetName()))
                .forEach(childContext -> collectAccessibleContextDefinitions(childContext));
    }

}
