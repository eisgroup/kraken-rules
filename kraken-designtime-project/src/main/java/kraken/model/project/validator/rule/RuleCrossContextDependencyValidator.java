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

import static kraken.model.project.validator.ValidationMessageBuilder.Message.RULE_CCR_IS_AMBIGUOUS;

import java.util.List;
import java.util.stream.Collectors;

import kraken.context.path.ContextPath;
import kraken.cross.context.path.CrossContextPath;
import kraken.model.Rule;
import kraken.model.project.KrakenProject;
import kraken.model.project.ccr.CrossContextService;
import kraken.model.project.ccr.CrossContextServiceProvider;
import kraken.model.project.dependencies.FieldDependency;
import kraken.model.project.dependencies.RuleDependencyExtractor;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationSession;

/**
 * Class which validates CCR defined in {@link Rule}s
 * Validations:
 * - Checks cross-context references ambiguities
 *
 * @author psurinin
 */
public class RuleCrossContextDependencyValidator implements RuleValidator {

    private final KrakenProject krakenProject;

    private final RuleDependencyExtractor dependencyExtractor;

    private final CrossContextService crossContextService;

    public RuleCrossContextDependencyValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
        this.crossContextService = CrossContextServiceProvider.forProject(krakenProject);
        this.dependencyExtractor = new RuleDependencyExtractor(krakenProject);
    }

    @Override
    public void validate(Rule rule, ValidationSession session) {
        List<ContextPath> pathsToRuleTarget = crossContextService.getPathsFor(rule.getContext());
        List<String> ccrDependencies = dependencyExtractor.extractDependencies(rule).stream()
                .filter(FieldDependency::isCcrDependency)
                .map(FieldDependency::getContextName)
                .distinct()
                .collect(Collectors.toList());

        for(String ccrDependency : ccrDependencies) {
            for(ContextPath pathToRuleTarget : pathsToRuleTarget) {
                var ccrDependencyPaths = crossContextService.resolvePaths(pathToRuleTarget, ccrDependency);
                if (ccrDependencyPaths.size() > 1) {
                    var m = ValidationMessageBuilder.create(RULE_CCR_IS_AMBIGUOUS, rule)
                        .parameters(
                            ccrDependency,
                            pathToRuleTarget.getPathAsString(),
                            ccrDependencyPaths.stream()
                                .map(CrossContextPath::toString)
                                .collect(Collectors.joining(", ")))
                        .build();
                    session.add(m);
                }
            }
        }
    }

    @Override
    public boolean canValidate(Rule rule) {
        return rule.getName() != null
            && rule.getContext() != null
            && krakenProject.getContextDefinitions().containsKey(rule.getContext())
            && rule.getTargetPath() != null
            && krakenProject.getContextProjection(rule.getContext()).getContextFields().containsKey(rule.getTargetPath());
    }

}

