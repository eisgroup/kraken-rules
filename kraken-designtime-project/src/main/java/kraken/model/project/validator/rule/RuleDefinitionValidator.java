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

import java.util.List;

import kraken.model.Rule;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationSession;

/**
 * @author mulevicius
 */
public class RuleDefinitionValidator {

    private final List<RuleValidator> ruleValidators;
    private final List<RuleValidator> dynamicRuleValidators;

    public RuleDefinitionValidator(KrakenProject krakenProject) {
        this.ruleValidators = List.of(
            new RuleModelValidator(),
            new RuleTargetContextValidator(krakenProject),
            new RuleNotAddedToEntryPointValidator(krakenProject),
            new RulePayloadCompatibilityValidator(krakenProject),
            new RuleDanglingTargetContextValidator(krakenProject),
            new RuleDefinedOnCycleValidator(krakenProject),
            new RuleCrossContextCardinalityValidator(krakenProject),
            new RuleCrossContextDependencyValidator(krakenProject),
            new RuleExpressionValidator(krakenProject),
            new RuleServerSideOnlyValidator(krakenProject),
            new RuleDimensionsValidator(krakenProject),
            new RuleVersionDuplicationValidator(krakenProject),
            new RuleVersionsOverridabilityValidator(krakenProject),
            new RuleVersionsPayloadTypeValidator(krakenProject)
        );

        this.dynamicRuleValidators = List.of(
            new RuleModelValidator(),
            new RuleTargetContextValidator(krakenProject),
            new RulePayloadCompatibilityValidator(krakenProject),
            new RuleDanglingTargetContextValidator(krakenProject),
            new RuleDefinedOnCycleValidator(krakenProject)
        );
    }

    public void validate(Rule rule, ValidationSession session) {
        ValidationSession ruleValidationSession = new ValidationSession();
        for(RuleValidator ruleValidator : ruleValidators) {
            if(ruleValidator.canValidate(rule)) {
                ruleValidator.validate(rule, session);
            }
        }
        session.addAll(ruleValidationSession.getValidationMessages());
    }

    public void validateDynamicRule(Rule dynamicRule, ValidationSession session) {
        ValidationSession ruleValidationSession = new ValidationSession();
        for(RuleValidator dynamicRuleValidator : dynamicRuleValidators) {
            if(dynamicRuleValidator.canValidate(dynamicRule)) {
                dynamicRuleValidator.validate(dynamicRule, session);
            }
        }
        session.addAll(ruleValidationSession.getValidationMessages());
    }
}
