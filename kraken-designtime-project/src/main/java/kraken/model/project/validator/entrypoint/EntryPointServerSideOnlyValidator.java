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

import static kraken.model.project.validator.Severity.ERROR;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.ENTRYPOINT_INCONSISTENT_INCLUDE_SERVER_SIDE_ONLY;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.ENTRYPOINT_INCONSISTENT_RULE_SERVER_SIDE_ONLY;
import static kraken.model.project.validator.ValidationMessageBuilder.Message.ENTRYPOINT_INCONSISTENT_VERSION_SERVER_SIDE_ONLY;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import kraken.model.Rule;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationSession;

/**
 * @author mulevicius
 */
public class EntryPointServerSideOnlyValidator {

    private final KrakenProject krakenProject;

    public EntryPointServerSideOnlyValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    public void validate(EntryPoint entryPoint, ValidationSession session) {
        validateServerSideOnlyVariations(entryPoint, session);
        validateServerSideOnlyIncludes(entryPoint, session);
        validateAssignedServerSideRules(entryPoint, session);
    }

    private void validateServerSideOnlyVariations(EntryPoint entryPoint, ValidationSession session) {
        if(entryPoint.isServerSideOnly()) {
            return;
        }
        List<EntryPoint> allVariations = krakenProject.getEntryPointVersions().get(entryPoint.getName());
        List<EntryPoint> serverSideOnlyVariations = allVariations.stream()
            .filter(ep -> ep.isServerSideOnly())
            .collect(Collectors.toList());

        if(!serverSideOnlyVariations.isEmpty() && serverSideOnlyVariations.size() != allVariations.size()) {
            var m = ValidationMessageBuilder.create(ENTRYPOINT_INCONSISTENT_VERSION_SERVER_SIDE_ONLY, entryPoint)
                .build();
            session.add(m);
        }
    }

    private void validateAssignedServerSideRules(EntryPoint entryPoint, ValidationSession session) {
        Map<String, List<Rule>> ruleVersions = krakenProject.getRuleVersions();

        if (!entryPoint.isServerSideOnly()) {
            Set<String> serverSideOnlyRuleNames = collectAllRuleNames(entryPoint.getName())
                .distinct()
                .filter(ruleName -> ruleVersions.getOrDefault(ruleName, List.of())
                    .stream()
                    .anyMatch(Rule::isServerSideOnly))
                .collect(Collectors.toSet());

            if (!serverSideOnlyRuleNames.isEmpty()) {
                var m = ValidationMessageBuilder.create(ENTRYPOINT_INCONSISTENT_RULE_SERVER_SIDE_ONLY, entryPoint)
                    .parameters(String.join(", ", serverSideOnlyRuleNames))
                    .build();
                session.add(m);
            }
        }
    }

    private Stream<String> collectAllRuleNames(String entryPointName) {
        List<EntryPoint> allVersions = krakenProject.getEntryPointVersions().getOrDefault(entryPointName, List.of());

        return allVersions.stream()
            .flatMap(entryPoint -> Stream.concat(
                entryPoint.getRuleNames().stream(),
                entryPoint.getIncludedEntryPointNames().stream()
                    .filter(included -> !included.equalsIgnoreCase(entryPoint.getName()))
                    .flatMap(this::collectAllRuleNames)));
    }

    private void validateServerSideOnlyIncludes(EntryPoint entryPoint, ValidationSession session) {
        Map<String, List<EntryPoint>> entryPoints = krakenProject.getEntryPointVersions();

        if(!entryPoint.isServerSideOnly()) {
            List<EntryPoint> includedServerSideEntryPoints = entryPoint.getIncludedEntryPointNames().stream()
                .filter(entryPointName -> entryPoints.containsKey(entryPointName))
                .flatMap(entryPointName -> krakenProject.getEntryPointVersions().get(entryPointName).stream())
                .filter(ep -> ep.isServerSideOnly())
                .collect(Collectors.toList());
            if(!includedServerSideEntryPoints.isEmpty()) {
                var includesString = includedServerSideEntryPoints.stream()
                    .map(EntryPoint::getName)
                    .collect(Collectors.joining(","));
                var m = ValidationMessageBuilder.create(ENTRYPOINT_INCONSISTENT_INCLUDE_SERVER_SIDE_ONLY, entryPoint)
                    .parameters(includesString)
                    .build();
                session.add(m);
            }
        }
    }

}
