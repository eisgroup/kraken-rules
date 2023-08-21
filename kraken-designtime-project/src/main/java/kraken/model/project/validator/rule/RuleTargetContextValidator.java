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

import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_INCONSISTENT_VERSION_TARGET;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_TARGET_CONTEXT_FIELD_FORBIDDEN;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_TARGET_CONTEXT_FIELD_UNKNOWN;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_TARGET_CONTEXT_SYSTEM;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_TARGET_CONTEXT_UNKNOWN;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;

import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextField;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessageBuilder;
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
            var m = ValidationMessageBuilder.create(RULE_TARGET_CONTEXT_UNKNOWN, rule)
                .parameters(rule.getContext())
                .build();
            session.add(m);
        } else if (contextDefinition.isSystem() && !(rule.getPayload() instanceof DefaultValuePayload)) {
            var m = ValidationMessageBuilder.create(RULE_TARGET_CONTEXT_SYSTEM, rule)
                .parameters(rule.getContext())
                .build();
            session.add(m);
        } else if (contextDefinition.isStrict()) {
            ContextField contextField = krakenProject.getContextProjection(contextDefinition.getName())
                .getContextFields().get(rule.getTargetPath());
            if (contextField == null) {
                var m = ValidationMessageBuilder.create(RULE_TARGET_CONTEXT_FIELD_UNKNOWN, rule)
                    .parameters(rule.getContext(), rule.getTargetPath())
                    .build();
                session.add(m);
            } else if (BooleanUtils.isTrue(contextField.getForbidTarget())) {
                var m = ValidationMessageBuilder.create(RULE_TARGET_CONTEXT_FIELD_FORBIDDEN, rule)
                    .parameters(rule.getTargetPath())
                    .build();
                session.add(m);
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
                var appliedOnString = rulesAppliedOnDifferentAttribute.stream()
                    .map(r -> r.getContext() + "." + r.getTargetPath())
                    .collect(Collectors.joining(", "));
                var m = ValidationMessageBuilder.create(RULE_INCONSISTENT_VERSION_TARGET, rule)
                    .parameters(appliedOnString)
                    .build();
                session.add(m);
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
