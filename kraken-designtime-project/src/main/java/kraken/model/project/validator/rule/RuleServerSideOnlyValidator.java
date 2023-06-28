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

import java.util.List;

import kraken.model.Rule;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationSession;

/**
 * An implementation used to validate whether a Rule marked as @ServerSideOnly
 * is correctly defined.
 *
 * @author Tomas Dapkunas
 */
public final class RuleServerSideOnlyValidator implements RuleValidator {

    private final KrakenProject krakenProject;

    public RuleServerSideOnlyValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    @Override
    public void validate(Rule rule, ValidationSession session) {
        if (!rule.isServerSideOnly()) {
            boolean hasServerSideOnlyVariations = krakenProject.getRuleVersions()
                .getOrDefault(rule.getName(), List.of())
                .stream()
                .anyMatch(Rule::isServerSideOnly);

            if (hasServerSideOnlyVariations) {
                session.add(new ValidationMessage(
                    rule,
                    "variation is misconfigured, because it is not marked as @ServerSideOnly, "
                        + "but there are another Rule variation that is marked as @ServerSideOnly. "
                        + "All variations of the same Rule must be consistently marked as @ServerSideOnly",
                    Severity.ERROR)
                );
            }
        }
    }

    @Override
    public boolean canValidate(Rule rule) {
        return rule.getName() != null;
    }

}
