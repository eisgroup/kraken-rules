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

package kraken.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.http.ResponseEntity;

import kraken.model.dimensions.DimensionSetService;
import kraken.model.EntryPointName;
import kraken.model.Rule;
import kraken.model.dsl.KrakenDSLModelParser;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.ResourceKrakenProject;
import kraken.model.project.validator.KrakenProjectValidationService;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.resource.Resource;
import kraken.runtime.repository.dynamic.DynamicRuleHolder;
import kraken.runtime.repository.dynamic.DynamicRuleRepository;

public class ReloadableRepository implements DynamicRuleRepository {

    private final ResourceKrakenProject baseKrakenProject;
    private final ReloadableStorage storage;
    private final KrakenProjectValidationService krakenProjectValidationService;

    public ReloadableRepository(ReloadableStorage storage, ResourceKrakenProject baseKrakenProject) {
        this.storage = storage;
        this.baseKrakenProject = baseKrakenProject;
        this.krakenProjectValidationService = new KrakenProjectValidationService();
    }

    @Override
    public Stream<DynamicRuleHolder> resolveDynamicRules(String namespace,
                                                         String entryPoint,
                                                         Map<String, Object> context) {

        // builds DimensionSetService by merging all base and stored entry points and rules
        // so that DimensionSet can be accurately calculated
        List<EntryPoint> mergedEntryPoints = new ArrayList<>(baseKrakenProject.getEntryPoints());
        mergedEntryPoints.addAll(storage.getEntryPoints().collect(Collectors.toList()));
        List<Rule> mergedRules = new ArrayList<>(baseKrakenProject.getRules());
        mergedRules.addAll(storage.getRules().collect(Collectors.toList()));
        KrakenProject mergedKrakenProject = baseKrakenProject.with(mergedEntryPoints, mergedRules);
        DimensionSetService dimensionSetService = new DimensionSetService(mergedKrakenProject);

        var ruleNames = storage.getEntryPoints()
            .filter(ep -> ep.getName().equals(entryPoint))
            .findFirst()
            .map(EntryPoint::getRuleNames)
            .orElse(List.of());

        return storage.getRules()
            .filter(rule -> ruleNames.contains(rule.getName()))
            .map(rule -> new DynamicRuleHolder(rule, dimensionSetService.resolveRuleDimensionSet(namespace, rule)));
    }

    public Return addRules(EntryPointName entryPointName, String rulesDsl) {
        Resource resource = KrakenDSLModelParser.parseResource(rulesDsl);

        List<EntryPoint> entryPoints = storage.getEntryPoints().collect(Collectors.toList());
        List<Rule> rules = Stream.concat(storage.getRules(), resource.getRules().stream()).collect(Collectors.toList());

        ResourceKrakenProject krakenProject = baseKrakenProject.with(entryPoints, rules);
        List<ValidationMessage> validationMessages = krakenProjectValidationService
                .validateRulesAndEntryPoints(krakenProject)
                .getErrors();
        if (!validationMessages.isEmpty()) {
            return Return.fail(
                    validationMessages.stream()
                            .map(ValidationMessage::toString)
                            .collect(Collectors.joining("\n"))
            );
        }
        storage.add(entryPointName, resource.getRules());
        return Return.ok("Rules are imported");
    }

    public Return removeRules(EntryPointName entryPointName, Collection<String> ruleNames) {
        storage.remove(entryPointName, ruleNames);
        return Return.ok("Rules: " + ruleNames + " are removed, from an EntryPoint and dynamic repository");
    }

    public Collection<Rule> getRules() {
        return storage.getRules().collect(Collectors.toList());
    }

    public Return clear() {
        storage.clear();
        return Return.ok("All Rules are removed and EntryPoints contains no rules");
    }

    public Return clear(EntryPointName entryPointName) {
        storage.clear(entryPointName);
        return Return.ok("EntryPoint " + entryPointName.name() + " contains no rules");
    }

    public EntryPoint getEntryPoint(EntryPointName entryPointName) {
        return storage.getEntryPoints()
                .filter(entryPoint -> entryPoint.getName().equals(entryPointName.toString()))
                .findFirst()
                .orElseThrow();
    }

    public static class Return {
        private String success;
        private String failure;

        private Return(String success, String failure) {
            this.success = success;
            this.failure = failure;
        }

        public static Return ok(String message) {
            return new Return(message, null);
        }

        public static Return fail(String message) {
            return new Return(null, message);
        }

        public Optional<String> getSuccess() {
            return Optional.ofNullable(success);
        }


        public Optional<String> getFailure() {
            return Optional.ofNullable(failure);
        }

        public ResponseEntity<String> createResponseEntity() {
            return Optional.ofNullable(success)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(500).body(failure));
        }
    }

}
