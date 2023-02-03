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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

/**
 * @author mulevicius
 */
public final class RuleTargetContextValidator implements RuleValidator {

    private final KrakenProject krakenProject;

    public RuleTargetContextValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    @Override
    public void validate(Rule rule, ValidationSession session) {
        ContextDefinition contextDefinition = krakenProject.getContextDefinitions().get(rule.getContext());
        if (contextDefinition == null) {
            String message = String.format("missing ContextDefinition with name '%s'", rule.getContext());
            session.add(new ValidationMessage(rule, message, Severity.ERROR));
        } else if (contextDefinition.isSystem()) {
            String message = String.format(
                "cannot be applied on system ContextDefinition '%s'.",
                rule.getContext());
            session.add(new ValidationMessage(rule, message, Severity.ERROR));
        } else if (contextDefinition.isStrict()) {
            ContextField contextField = krakenProject.getContextProjection(contextDefinition.getName())
                .getContextFields().get(rule.getTargetPath());
            if (contextField == null) {
                String messageFormat = "ContextDefinition '%s' doesn't have field '%s'";
                String message = String.format(messageFormat, rule.getContext(), rule.getTargetPath());
                session.add(new ValidationMessage(rule, message, Severity.ERROR));
            } else if (contextField.isExternal()) {
                String messageFormat = "cannot be applied on external field - '%s'";
                String message = String.format(messageFormat, rule.getTargetPath());
                session.add(new ValidationMessage(rule, message, Severity.ERROR));
            }
        }

        if(krakenProject.getRuleVersions().containsKey(rule.getName())) {
            List<Rule> rulesAppliedOnDifferentAttribute = krakenProject.getRuleVersions().get(rule.getName()).stream()
                .filter(r -> r.getContext() != null)
                .filter(r -> r.getTargetPath() != null)
                .filter(
                    r -> !r.getContext().equals(rule.getContext()) || !r.getTargetPath().equals(rule.getTargetPath()))
                .collect(Collectors.toList());

            if (!rulesAppliedOnDifferentAttribute.isEmpty()) {
                Set<String> appliedOn = rulesAppliedOnDifferentAttribute.stream()
                    .map(r -> r.getContext() + "." + r.getTargetPath())
                    .collect(Collectors.toSet());
                String msg = "has version applied on different context or attribute: " + appliedOn;
                session.add(new ValidationMessage(rule, msg, Severity.ERROR));
            }
        }
    }

    @Override
    public boolean canValidate(Rule rule) {
        return rule.getName() != null
            && rule.getContext() != null
            && rule.getTargetPath() != null;
    }
}
