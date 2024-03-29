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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kraken.context.path.ContextPath;
import kraken.cross.context.path.CrossContextPath;
import kraken.model.Rule;
import kraken.model.context.Cardinality;
import kraken.model.project.KrakenProject;
import kraken.model.project.ccr.CrossContextService;
import kraken.model.project.ccr.CrossContextServiceProvider;
import kraken.model.project.dependencies.FieldDependency;
import kraken.model.project.dependencies.RuleDependencyExtractor;
import kraken.model.project.validator.ValidationMessage;
import kraken.model.project.validator.ValidationMessageBuilder;
import kraken.model.project.validator.ValidationMessageBuilder.Message;
import kraken.model.project.validator.ValidationSession;
import kraken.utils.dto.Pair;


/**
 * Validates Rule dependencies extracted from expressions.
 * If dependencies points via Cross Context References to entities
 * with different cardinality, validator return failures
 *
 * @author psurinin@eisgroup.com
 * @since 1.0.36
 */
public class RuleCrossContextCardinalityValidator implements RuleValidator {

    private static final Logger logger = LoggerFactory.getLogger(RuleCrossContextCardinalityValidator.class);

    private final KrakenProject krakenProject;

    private final RuleDependencyExtractor dependencyExtractor;

    private final CrossContextService crossContextService;

    public RuleCrossContextCardinalityValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;

        this.crossContextService = CrossContextServiceProvider.forProject(krakenProject);
        this.dependencyExtractor = new RuleDependencyExtractor(krakenProject);
    }

    @Override
    public void validate(Rule rule, ValidationSession session) {
        List<String> ccrDependencies = dependencyExtractor.extractDependencies(rule).stream()
                .filter(FieldDependency::isCcrDependency)
                .map(FieldDependency::getContextName)
                .distinct()
                .collect(Collectors.toList());

        List<ContextPath> rulePaths = crossContextService.getPathsFor(rule.getContext());

        logger.trace("For rule '{}' found cross context dependencies: {}", rule.getFullName(), ccrDependencies);

        ccrDependencies.forEach(ccr -> {
            Map<Cardinality, List<Pair<String, CrossContextPath>>> grouped =
                    rulePaths.stream()
                            .filter(isRuleContextSource(rule.getContext()))
                            .flatMap(contextPath -> crossContextService
                                    .resolvePaths(contextPath, ccr).stream()
                                    .map(crossContextPath -> new Pair<>(contextPath.getPathAsString(), crossContextPath)))
                            .collect(Collectors.groupingBy(pair -> pair.right.getCardinality()));

            if (grouped.size() > 1) {
                session.add(createMessage(rule, ccr, grouped));
            }
        });
    }

    @Override
    public boolean canValidate(Rule rule) {
        return rule.getName() != null
            && rule.getContext() != null
            && krakenProject.getContextDefinitions().containsKey(rule.getContext())
            && rule.getTargetPath() != null
            && krakenProject.getContextProjection(rule.getContext()).getContextFields().containsKey(rule.getTargetPath());
    }

    private Predicate<ContextPath> isRuleContextSource(String ruleContext) {
        return path -> {
            String last = path.getLastElement();

            Collection<String> inherited = krakenProject.getContextProjection(last).getParentDefinitions();

            boolean isPathToContextSource = inherited.contains(ruleContext) || last.equals(ruleContext);
            logger.trace(
                    "Path to context '{}' is{}a target source for '{}'",
                    path,
                    isPathToContextSource ? " " : " not ",
                    ruleContext
            );
            return isPathToContextSource;
        };
    }

    private ValidationMessage createMessage(Rule rule, String ccr, Map<Cardinality, List<Pair<String, CrossContextPath>>> grouped) {
        return ValidationMessageBuilder.create(Message.RULE_CCR_DIFFERENT_CARDINALITIES, rule)
            .parameters(rule.getContext(), ccr, formatExplanation(grouped))
            .build();
    }

    private String formatExplanation(Map<Cardinality, List<Pair<String, CrossContextPath>>> grouped) {
        var single = grouped.get(Cardinality.SINGLE).stream()
            .map(e -> "from " + e.left + " to " + e.right)
            .collect(Collectors.joining(System.lineSeparator() + "    ", "    ", ""));
        var multiple = grouped.get(Cardinality.MULTIPLE).stream()
            .map(e -> "from " + e.left + " to " + e.right)
            .collect(Collectors.joining(System.lineSeparator() + "    ", "    ", ""));

        return "\n  Single:\n" + single + "\n  Multiple:\n" + multiple;
    }
}
