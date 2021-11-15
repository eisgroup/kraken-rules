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

import static kraken.model.project.validator.Severity.ERROR;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kraken.context.path.ContextPath;
import kraken.context.path.ContextPathExtractor;
import kraken.cross.context.path.CrossContextPath;
import kraken.cross.context.path.CrossContextPathsResolver;
import kraken.model.Rule;
import kraken.model.context.Cardinality;
import kraken.model.project.KrakenProject;
import kraken.model.project.ccr.CrossContextService;
import kraken.model.project.ccr.CrossContextServiceProvider;
import kraken.model.project.ccr.CrossContextServiceSupplier;
import kraken.model.project.dependencies.FieldDependency;
import kraken.model.project.dependencies.RuleDependencyExtractor;
import kraken.model.project.validator.ValidationMessage;
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
public class RuleCrossContextCardinalityValidator {

    private static final Logger logger = LoggerFactory.getLogger(RuleCrossContextCardinalityValidator.class);

    private final KrakenProject krakenProject;

    private final RuleDependencyExtractor dependencyExtractor;

    private final CrossContextService crossContextService;

    public RuleCrossContextCardinalityValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;

        this.crossContextService = CrossContextServiceProvider.forProject(krakenProject);
        this.dependencyExtractor = new RuleDependencyExtractor(krakenProject);
    }

    public void validate(Rule rule, ValidationSession session) {
        List<String> dependantContexts = dependencyExtractor.extractDependencies(rule).stream()
                .filter(FieldDependency::isContextDependency)
                .map(FieldDependency::getContextName)
                .distinct()
                .collect(Collectors.toList());

        List<ContextPath> rulePaths = crossContextService.getPathsFor(rule.getContext());

        logger.trace("For rule '{}' found dependant context definitions: {}", rule.getFullName(), dependantContexts);

        dependantContexts.forEach(dependantContext -> {
            Map<Cardinality, List<Pair<String, CrossContextPath>>> grouped =
                    rulePaths.stream()
                            .filter(isRuleContextSource(rule.getContext(), krakenProject))
                            .flatMap(contextPath -> crossContextService
                                    .resolvePaths(contextPath, dependantContext).stream()
                                    .map(crossContextPath -> new Pair<>(contextPath.getPathAsString(), crossContextPath)))
                            .collect(Collectors.groupingBy(pair -> pair.right.getCardinality()));

            if (grouped.size() > 1) {
                String message = createMessage(rule, dependantContext, grouped);
                session.add(new ValidationMessage(rule, message, ERROR));
            }
        });
    }

    private Predicate<ContextPath> isRuleContextSource(String ruleContext, KrakenProject krakenProject) {
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

    private String createMessage(Rule rule, String dc, Map<Cardinality, List<Pair<String, CrossContextPath>>> grouped) {
        Function<Pair<String, CrossContextPath>, String> messageByCardinality =
                pair -> "\n\t\t\tfrom '" + pair.left + "' to '" + String.join(".", pair.right.getPath()) + "'";
        final String message = String.format(
                "Cross Context Reference from '%s' to '%s' resolves to different cardinalities in different parts of model. " +
                        "\n\t\tSingle: %s, " +
                        "\n\t\tMultiple: %s",
                rule.getContext(),
                dc,
                grouped.get(Cardinality.SINGLE).stream()
                        .map(messageByCardinality)
                        .collect(Collectors.joining(", ")),
                grouped.get(Cardinality.MULTIPLE).stream()
                        .map(messageByCardinality)
                        .collect(Collectors.joining(", "))
        );
        logger.trace(message);
        return message;
    }

}
