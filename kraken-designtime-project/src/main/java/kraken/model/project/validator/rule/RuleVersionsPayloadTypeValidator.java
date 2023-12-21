/*
 *  Copyright © 2023 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.model.project.validator.rule;

import kraken.model.Payload;
import kraken.model.Rule;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationSession;

import java.util.stream.Collectors;

import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_INCONSISTENT_VERSION_PAYLOAD;

/**
 * Validates whether there are rule versions with incompatible payload types.
 * Rule versions with different payload types are supported only for Validation evaluation type payloads.
 *
 * @author kjuraityte
 */
public final class RuleVersionsPayloadTypeValidator implements RuleValidator {
    private final KrakenProject krakenProject;

    public RuleVersionsPayloadTypeValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    @Override
    public void validate(Rule rule, ValidationSession session) {
        Payload payload = rule.getPayload();
        var incompatibleTypes = krakenProject.getRuleVersions().get(rule.getName()).stream()
            .filter(version -> version.getPayload() != null)
            .filter(version -> !version.getPayload().getPayloadType().getEvaluationType().equals(payload.getPayloadType().getEvaluationType()))
            .map(v -> v.getPayload().getPayloadType().getTypeName())
            .sorted()
            .collect(Collectors.toList());
        if (!incompatibleTypes.isEmpty()) {
            String incompatibleTypesMessage;
            if (incompatibleTypes.size() == 1) {
                incompatibleTypesMessage = transformCamelCaseToWords(incompatibleTypes.get(0)).concat(" payload type");
            } else {
                var types = incompatibleTypes.stream().map(this::transformCamelCaseToWords).collect(Collectors.joining(", "));
                incompatibleTypesMessage = types.concat(" payload types");
            }
            var ruleTypeString = transformCamelCaseToWords(payload.getPayloadType().getTypeName());
            var m = ValidationMessageBuilder.create(RULE_INCONSISTENT_VERSION_PAYLOAD, rule)
                .parameters(ruleTypeString, incompatibleTypesMessage)
                .build();
            session.add(m);
        }
    }

    @Override
    public boolean canValidate(Rule rule) {
        return rule.getName() != null && rule.getPayload() != null;
    }

    private String transformCamelCaseToWords(String string) {
        return string.replaceAll("(?<=[^A-Z])(?=[A-Z])", " ").replace(" Payload", "");
    }
}