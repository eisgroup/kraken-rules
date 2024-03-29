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

import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_TARGET_CONTEXT_DANGLING;

import kraken.model.Rule;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationSession;

/**
 * @author mulevicius
 */
public class RuleDanglingTargetContextValidator implements RuleValidator {

    private final KrakenProject krakenProject;

    public RuleDanglingTargetContextValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    @Override
    public void validate(Rule rule, ValidationSession session) {
        if(!krakenProject.getConnectedContextDefinitions().contains(rule.getContext())) {
            var m = ValidationMessageBuilder.create(RULE_TARGET_CONTEXT_DANGLING, rule)
                .parameters(rule.getContext(), krakenProject.getRootContextName())
                .build();
            session.add(m);
        }
    }

    @Override
    public boolean canValidate(Rule rule) {
        return rule.getName() != null
            && rule.getContext() != null
            && krakenProject.getContextDefinitions().containsKey(rule.getContext());
    }

}
