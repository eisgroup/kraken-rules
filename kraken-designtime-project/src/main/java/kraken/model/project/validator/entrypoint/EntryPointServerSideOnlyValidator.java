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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessage;
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
            session.add(new ValidationMessage(
                entryPoint,
                String.format(
                    "EntryPoint '%s' variation is misconfigured, because it is not marked as @ServerSideOnly, "
                        + "but there are another EntryPoint variation that is marked as @ServerSideOnly."
                        + "All variations of the same EntryPoint must be consistently marked as @ServerSideOnly.",
                    entryPoint.getName()
                ),
                ERROR
            ));
        }
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
                session.add(new ValidationMessage(
                    entryPoint,
                    String.format(
                        "Entry Point '%s' not annotated as @ServerSideOnly includes entry point(s): '%s' marked as @ServerSideOnly",
                        entryPoint.getName(),
                        includedServerSideEntryPoints.stream()
                            .map(EntryPoint::getName)
                            .collect(Collectors.joining(","))
                    ),
                    ERROR
                ));
            }
        }
    }

}
