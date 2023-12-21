/*
 *  Copyright © 2023 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.model.project.validator.rule;

import kraken.model.Rule;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationSession;
import kraken.model.validation.ValidationPayload;

import java.util.Objects;

import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_INCONSISTENT_VERSION_OVERRIDABLE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_INCONSISTENT_VERSION_OVERRIDABLE_GROUP;

/**
 * Validates whether all rule versions have the same overridability configuration.
 * Applicable for rule versions with Validation evaluation type payloads
 *
 * @author kjuraityte
 */
public final class RuleVersionsOverridabilityValidator implements RuleValidator {
    private final KrakenProject krakenProject;

    public RuleVersionsOverridabilityValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    @Override
    public void validate(Rule rule, ValidationSession session) {
        ValidationPayload payload = (ValidationPayload) rule.getPayload();
        boolean hasOverridableVariations = krakenProject.getRuleVersions().get(rule.getName()).stream()
            .filter(r -> r.getPayload() instanceof ValidationPayload)
            .anyMatch(r -> payload.isOverridable() != ((ValidationPayload) r.getPayload()).isOverridable());
        if (hasOverridableVariations) {
            var m = ValidationMessageBuilder.create(RULE_INCONSISTENT_VERSION_OVERRIDABLE, rule).build();
            session.add(m);
        }
        if (payload.isOverridable()) {
            boolean overridableGroupVaries = krakenProject.getRuleVersions().get(rule.getName()).stream()
                .filter(r -> r.getPayload() instanceof ValidationPayload)
                .anyMatch(r -> !Objects.equals(payload.getOverrideGroup(), ((ValidationPayload) r.getPayload()).getOverrideGroup()));
            if (overridableGroupVaries) {
                var m = ValidationMessageBuilder.create(RULE_INCONSISTENT_VERSION_OVERRIDABLE_GROUP, rule).build();
                session.add(m);
            }
        }
    }

    @Override
    public boolean canValidate(Rule rule) {
        return rule.getName() != null
            && rule.getPayload() != null
            && rule.getPayload().getPayloadType() != null
            && rule.getPayload() instanceof ValidationPayload;
    }
}