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
package kraken.model.project.validator.rule;

import java.util.stream.Collectors;

import kraken.model.Rule;
import kraken.model.project.KrakenProject;
import kraken.model.project.ccr.CrossContextService;
import kraken.model.project.ccr.CrossContextServiceProvider;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

/**
 * @author Pavel Surinin
 */
public class RuleDefinedOnCycleValidator implements RuleValidator {

    private final KrakenProject krakenProject;

    private final CrossContextService crossContextService;

    public RuleDefinedOnCycleValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;

        this.crossContextService = CrossContextServiceProvider.forProject(krakenProject);
    }

    @Override
    public void validate(Rule rule, ValidationSession session) {
        var cycled = crossContextService.getAllCycles().stream()
                .flatMap(x -> x.getNodeNames().stream())
                .collect(Collectors.toSet());

        String cycleString = String.join(", ", cycled);
        String template = "is defined on '%s', " +
                "which is included in a recursive data structure between: '%s'. " +
                "Defining rules on recursive Context Definition is not supported.";

        if(cycled.contains(rule.getContext())) {
            session.add(new ValidationMessage(
                rule,
                String.format(template, rule.getContext(), cycleString),
                Severity.ERROR
            ));
        }
    }

    @Override
    public boolean canValidate(Rule rule) {
        return rule.getName() != null
            && rule.getContext() != null
            && krakenProject.getContextDefinitions().containsKey(rule.getContext());
    }
}
