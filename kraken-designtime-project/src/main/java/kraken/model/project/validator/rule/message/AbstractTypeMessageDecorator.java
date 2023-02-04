/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.model.project.validator.rule.message;

import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import kraken.el.ast.Expression;
import kraken.el.ast.Identifier;
import kraken.el.ast.ReferenceValue;
import kraken.el.ast.validation.details.AstDetails;
import kraken.el.scope.type.ArrayType;
import kraken.model.Rule;
import kraken.model.context.Cardinality;
import kraken.model.project.KrakenProject;
import kraken.model.project.ccr.CrossContextService;

/**
 * A base class to be extended by more concrete decorators. Provides common functionality to obtain
 * CCR Path related message(s).
 *
 * @author Tomas Dapkunas
 * @since 1.29.0
 */
public abstract class AbstractTypeMessageDecorator<T extends AstDetails> implements AstMessageDecorator<T> {

    private final KrakenProject krakenProject;
    private final CrossContextService crossContextService;

    public AbstractTypeMessageDecorator(KrakenProject krakenProject,
                                        CrossContextService crossContextService) {
        this.krakenProject = krakenProject;
        this.crossContextService = crossContextService;
    }

    /**
     * Returns a formatted message CCR path message if following conditions are true:
     *
     * <ul>
     *     <li>First {@link Expression} reference is an {@link Identifier}</li>
     *     <li>{@link Identifier} is context definition and is not the same as {@link Rule} target context type.</li>
     *     <li>Rule target context has paths to {@link Identifier} type.</li>
     * </ul>
     */
    protected String describeIfExpressionIsSimplePathAndCcr(Expression expression, Rule rule) {
        return findFirstReferenceOfSimplePath(expression)
            .filter(identifier -> isContextDefinition(identifier.getIdentifier()))
            .filter(identifier -> isCrossContext(identifier.getIdentifier(), rule.getContext()))
            .map(identifier -> describeIfExpressionIsSimplePathAndCcr(rule, expression, identifier))
            .orElse(StringUtils.EMPTY);
    }

    protected boolean isSimplePathAndCcrIsArray(Expression expression) {
        return findFirstReferenceOfSimplePath(expression)
            .map(exp -> exp.getEvaluationType() instanceof ArrayType
                && expression.getEvaluationType() instanceof ArrayType)
            .orElse(false);
    }

    private String describeIfExpressionIsSimplePathAndCcr(Rule rule, Expression expression, Identifier ccrIdentifier) {
        if (ccrIdentifier.getEvaluationType() instanceof ArrayType) {
            String ccrMessage = resolveCcrPaths(rule, ccrIdentifier, Cardinality.MULTIPLE);

            if (!ccrMessage.isEmpty()) {
                String tokenText = expression.getToken().getText();
                String template =
                    " '%s' is a collection of values, because '%s' is a cross context reference accessed %s.";
                return String.format(template, tokenText, ccrIdentifier.getIdentifier(), ccrMessage);
            }
        } else {
            String ccrMessage = resolveCcrPaths(rule, ccrIdentifier, Cardinality.SINGLE);

            if (!ccrMessage.isEmpty()) {
                String tokenText = expression.getToken().getText();
                String template =
                    " '%s' is not a collection of values, because '%s' is a cross context reference accessed %s.";
                return String.format(template, tokenText, ccrIdentifier.getIdentifier(), ccrMessage);
            }
        }

        return StringUtils.EMPTY;
    }

    private String resolveCcrPaths(Rule rule, Identifier ccrIdentifier, Cardinality cardinality) {
        return crossContextService.getPathsFor(rule.getContext())
            .stream()
            .map(pathToRuleCtx -> {
                String concatenated = crossContextService
                    .resolvePaths(pathToRuleCtx, ccrIdentifier.getIdentifier())
                    .stream()
                    .filter(crossContextPath -> crossContextPath.getCardinality() == cardinality)
                    .map(crossContextPath -> crossContextPath.toString().replaceAll("\\.", "->"))
                    .collect(Collectors.joining(", "));

                if (!concatenated.isEmpty()) {
                    String template = "from '%s' which resolves to %s cardinality through path(s): %s";
                    return String.format(template,
                        pathToRuleCtx.toString(),
                        cardinality.getName(),
                        concatenated);
                }

                return StringUtils.EMPTY;
            })
            .filter(StringUtils::isNotEmpty)
            .collect(Collectors.joining(" and "));
    }

    protected Optional<Identifier> findFirstReferenceOfSimplePath(Expression expression) {
        return Optional.of(expression)
            .filter(exp -> exp instanceof ReferenceValue)
            .map(exp -> ((ReferenceValue) exp).getReference())
            .filter(exp -> exp.isSimplePath())
            .map(exp -> exp.getFirstReference())
            .filter(exp -> exp instanceof Identifier)
            .map(exp -> (Identifier) exp);
    }

    private boolean isContextDefinition(String ccrIdentifier) {
        return krakenProject.getContextDefinitions().containsKey(ccrIdentifier);
    }

    private boolean isCrossContext(String ccrIdentifier, String targetContext) {
        return !ccrIdentifier.equals(targetContext);
    }

}
