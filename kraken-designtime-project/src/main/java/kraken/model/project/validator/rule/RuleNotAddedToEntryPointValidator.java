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

import java.util.Set;
import java.util.stream.Collectors;

import kraken.model.Rule;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationMessageBuilder.Message;
import kraken.model.project.validator.ValidationSession;

/**
 * @author mulevicius
 */
public class RuleNotAddedToEntryPointValidator implements RuleValidator {

    private final Set<String> allAddedRules;

    public RuleNotAddedToEntryPointValidator(KrakenProject krakenProject) {
        this.allAddedRules = krakenProject.getEntryPoints().stream()
            .flatMap(ep -> ep.getRuleNames().stream())
            .collect(Collectors.toSet());
    }

    @Override
    public void validate(Rule rule, ValidationSession session) {
        if(!allAddedRules.contains(rule.getName())) {
            var m = ValidationMessageBuilder.create(Message.RULE_NOT_IN_ENTRYPOINT, rule).build();
            session.add(m);
        }
    }

    @Override
    public boolean canValidate(Rule rule) {
        return rule.getName() != null;
    }
}
