/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.model.project.validator.rule;

import java.util.Map;
import java.util.Objects;

import kraken.model.Rule;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationMessageBuilder.Message;
import kraken.model.project.validator.ValidationSession;

/**
 * Implements Rule version duplication detection algorithm. A rule version is
 * uniquely identified by rule name and dimensions. If there are multiple rule
 * versions having the same name and dimensions, then those versions are
 * considered to be duplicates.
 *
 * @author Tomas Dapkunas
 * @since 1.54.0
 */
public final class RuleVersionDuplicationValidator implements RuleValidator {

    private final KrakenProject krakenProject;

    public RuleVersionDuplicationValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    @Override
    public void validate(Rule rule, ValidationSession session) {
        boolean hasDuplicates = krakenProject.getRuleVersions().get(rule.getName())
            .stream()
            .filter(ruleVersion -> !Objects.equals(ruleVersion.getRuleVariationId(), rule.getRuleVariationId()))
            .anyMatch(ruleVersion -> isDuplicate(rule, ruleVersion));

        if (hasDuplicates) {
            session.add(ValidationMessageBuilder.create(Message.DUPLICATE_RULE_VERSION, rule).build());
        }
    }

    private boolean isDuplicate(Rule rule, Rule ruleVersion) {
        Map<String, Object> ruleDimensions = getDimensions(rule);
        Map<String, Object> versionDimensions = getDimensions(ruleVersion);

        return ruleDimensions.size() == versionDimensions.size() &&
            ruleDimensions.entrySet()
                .stream()
                .allMatch(entry -> entry.getValue().equals(versionDimensions.get(entry.getKey())));
    }

    private Map<String, Object> getDimensions(Rule rule) {
        return rule.getMetadata() != null ? rule.getMetadata().asMap() : Map.of();
    }

    @Override
    public boolean canValidate(Rule rule) {
        return rule.getName() != null
            && krakenProject.getRuleVersions().get(rule.getName()).size() > 1;
    }

}
