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
package kraken.model.project.validator.entrypoint;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationMessageBuilder.Message;
import kraken.model.project.validator.ValidationSession;

/**
 * Implements Entry point version duplication detection algorithm. An entry point
 * version is uniquely identified by entry point name and dimensions. If there are
 * multiple entry point versions having the same name and dimensions, then those
 * versions are considered to be duplicates.
 *
 * @author Tomas Dapkunas
 * @since 1.54.0
 */
public final class EntryPointVersionDuplicationValidator {

    private final KrakenProject krakenProject;

    public EntryPointVersionDuplicationValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    public void validate(EntryPoint entryPoint, ValidationSession validationSession) {
        boolean hasDuplicates = krakenProject.getEntryPointVersions().get(entryPoint.getName()).stream()
            .filter(entryPointVersion -> !Objects.equals(entryPoint.getEntryPointVariationId(),
                entryPointVersion.getEntryPointVariationId()))
            .anyMatch(entryPointVersion -> isDuplicate(entryPoint, entryPointVersion));

        if (hasDuplicates) {
            validationSession.add(ValidationMessageBuilder.create(Message.DUPLICATE_ENTRYPOINT_VERSION, entryPoint).build());
        }
    }

    private boolean isDuplicate(EntryPoint entryPoint, EntryPoint entryPointVersion) {
        Map<String, Object> entryPointDimensions = getDimensions(entryPoint);
        Map<String, Object> versionDimensions = getDimensions(entryPointVersion);

        return entryPointDimensions.size() == versionDimensions.size() &&
            entryPointDimensions.entrySet()
                .stream()
                .allMatch(entry -> entry.getValue().equals(versionDimensions.get(entry.getKey())));
    }

    private Map<String, Object> getDimensions(EntryPoint entryPoint) {
        return entryPoint.getMetadata() != null ? entryPoint.getMetadata().asMap() : Map.of();
    }

}
