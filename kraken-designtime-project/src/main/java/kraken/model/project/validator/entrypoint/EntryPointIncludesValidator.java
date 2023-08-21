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
package kraken.model.project.validator.entrypoint;

import static kraken.model.project.validator.ValidationMessageBuilder.Message.ENTRYPOINT_DUPLICATE_INCLUDE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.ENTRYPOINT_NESTED_INCLUDE;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.ENTRYPOINT_UNKNOWN_INCLUDE;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationSession;

/**
 * @author mulevicius
 */
public class EntryPointIncludesValidator {

    private final KrakenProject krakenProject;

    public EntryPointIncludesValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    public void validate(EntryPoint entryPoint, ValidationSession session) {
        Map<String, List<EntryPoint>> entryPointsByName = krakenProject.getEntryPointVersions();

        for (String includedEntryPointName : entryPoint.getIncludedEntryPointNames()) {
            List<EntryPoint> includedEntryPoints = entryPointsByName.get(includedEntryPointName);
            if (includedEntryPoints == null || includedEntryPoints.isEmpty()) {
                var m = ValidationMessageBuilder.create(ENTRYPOINT_UNKNOWN_INCLUDE, entryPoint)
                    .parameters(includedEntryPointName)
                    .build();
                session.add(m);
            }

            if (includedEntryPoints != null) {
                validateTransitiveEntryPointIncludes(entryPoint, session, includedEntryPoints);
                collectWarningsForDuplicateRules(entryPoint, session, includedEntryPoints);
            }
        }
    }

    private void collectWarningsForDuplicateRules(EntryPoint entryPoint,
                                                  ValidationSession session,
                                                  List<EntryPoint> includedEntryPoints) {
        for(EntryPoint includedEntryPoint : includedEntryPoints) {
            List<String> duplicateRuleNames = includedEntryPoint.getRuleNames().stream()
                    .filter(ruleName -> entryPoint.getRuleNames().contains(ruleName))
                    .collect(Collectors.toList());

            if(!duplicateRuleNames.isEmpty()) {
                var m = ValidationMessageBuilder.create(ENTRYPOINT_DUPLICATE_INCLUDE, entryPoint)
                    .parameters(includedEntryPoint, String.join(", ", duplicateRuleNames))
                    .build();
                session.add(m);
            }
        }
    }

    private void validateTransitiveEntryPointIncludes(EntryPoint entryPoint,
                                                      ValidationSession session,
                                                      List<EntryPoint> includedEntryPoints) {
        includedEntryPoints.stream()
            .filter(includedEntryPoint -> !includedEntryPoint.getIncludedEntryPointNames().isEmpty())
            .findFirst()
            .ifPresent(entryPointWithTransitiveIncludes -> {
                var m = ValidationMessageBuilder.create(ENTRYPOINT_NESTED_INCLUDE, entryPoint)
                    .parameters(entryPointWithTransitiveIncludes.getName())
                    .build();
                session.add(m);
            });
    }
}
