/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.converter.translation;

import static kraken.el.ast.Template.asTemplateExpression;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import kraken.converter.KrakenProjectConvertionException;
import kraken.converter.translation.ast.*;
import kraken.el.*;
import kraken.el.ast.Ast;
import kraken.el.ast.AstType;
import kraken.el.ast.Template;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.ast.builder.AstBuildingException;
import kraken.el.scope.Scope;
import kraken.model.Expression;
import kraken.model.Rule;
import kraken.model.context.ContextNavigation;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.dependencies.FieldDependency;
import kraken.model.project.dependencies.RuleDependencyExtractor;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderProvider;
import kraken.runtime.model.expression.*;
import kraken.runtime.model.rule.payload.validation.ErrorMessage;

/**
 * Translates designtime expression to runtime expression in {@link TargetEnvironment}
 *
 * @author mulevicius
 */
public class KrakenExpressionTranslator {

    private TargetEnvironment targetEnvironment;
    private ExpressionLanguage expressionLanguage;
    private AstRewriter complexExpressionRewriter;
    private AstRewriter pathExpressionRewriter;
    private RuleDependencyExtractor ruleDependencyExtractor;
    private KrakenProject krakenProject;
    private ScopeBuilder scopeBuilder;

    public KrakenExpressionTranslator(KrakenProject krakenProject,
                                      TargetEnvironment targetEnvironment,
                                      RuleDependencyExtractor ruleDependencyExtractor) {

        this.expressionLanguage = KrakenKel.create(targetEnvironment);
        this.targetEnvironment = targetEnvironment;
        if (targetEnvironment.equals(TargetEnvironment.JAVASCRIPT)) {
            this.complexExpressionRewriter = new JavaScriptRewriter(krakenProject);
        } else if (targetEnvironment.equals(TargetEnvironment.JAVA)) {
            this.complexExpressionRewriter = new JavaRewriter(krakenProject);
        } else {
            throw new IllegalStateException("Unknown TargetEnvironment: " + targetEnvironment);
        }
        this.pathExpressionRewriter = new SimplePathRewriter(krakenProject);


        this.krakenProject = krakenProject;
        this.ruleDependencyExtractor = ruleDependencyExtractor;
        this.scopeBuilder = ScopeBuilderProvider.forProject(krakenProject);
    }

    public CompiledExpression translateExpression(Rule rule, Expression expression) {
        try {
            kraken.model.context.ContextDefinition contextDefinition = krakenProject.getContextDefinitions().get(rule.getContext());
            Scope scope = scopeBuilder.buildScope(contextDefinition);

            Collection<FieldDependency> dependencies = ruleDependencyExtractor.extractDependencies(expression, scope);

            List<ExpressionVariable> expressionVariables = dependencies.stream()
                    .filter(d -> d.isContextDependency())
                    .filter(d -> !d.getContextName().equals(rule.getContext()))
                    .map(d -> new ExpressionVariable(d.getContextName(), ExpressionVariableType.CROSS_CONTEXT))
                    .collect(Collectors.toList());

            Ast rewrittenAst = rewrite(AstBuilder.from(expression.getExpressionString(), scope));

            return new CompiledExpression(
                    translateExpression(rewrittenAst),
                    convert(rewrittenAst.getAstType()),
                    rewrittenAst.getAstType() == AstType.LITERAL ? (Serializable) rewrittenAst.getCompiledLiteralValue() : null,
                    rewrittenAst.getAstType() == AstType.LITERAL ? rewrittenAst.getCompiledLiteralValueType() : null,
                    expressionVariables
            );
        } catch (AstBuildingException e) {
            throw new KrakenProjectConvertionException("Error while translating expression: " + expression.getExpressionString(), e);
        }
    }

    public ErrorMessage translateErrorMessage(Rule rule, kraken.model.ErrorMessage errorMessage) {
        if(errorMessage == null) {
            return null;
        }
        if(errorMessage.getErrorMessage() == null) {
            return new ErrorMessage(errorMessage.getErrorCode(), List.of(), List.of());
        }
        kraken.model.context.ContextDefinition contextDefinition = krakenProject.getContextDefinitions().get(rule.getContext());
        Scope scope = scopeBuilder.buildScope(contextDefinition);

        Template template = (Template) AstBuilder.from(asTemplateExpression(errorMessage.getErrorMessage()), scope).getExpression();

        List<CompiledExpression> templateExpressions = template.getTemplateExpressions().stream()
            .map(e -> {
                Expression expression = RulesModelFactory.getInstance().createExpression();
                expression.setExpressionString(e.toString());
                return expression;
            })
            .map(e -> translateExpression(rule, e))
            .collect(Collectors.toList());

        return new ErrorMessage(errorMessage.getErrorCode(), template.getTemplateParts(), templateExpressions);
    }

    public CompiledExpression translateContextNavigationExpression(ContextNavigation contextNavigation) {
        try {
            Ast rewrittenAst = rewrite(AstBuilder.from(contextNavigation.getNavigationExpression(), Scope.dynamic()));
            return new CompiledExpression(
                    translateExpression(rewrittenAst),
                    convert(rewrittenAst.getAstType()),
                    rewrittenAst.getAstType() == AstType.LITERAL ? (Serializable) rewrittenAst.getCompiledLiteralValue() : null,
                    rewrittenAst.getAstType() == AstType.LITERAL ? rewrittenAst.getCompiledLiteralValueType() : null,
                    List.of()
            );
        } catch (AstBuildingException e) {
            throw new KrakenProjectConvertionException("Error while translating navigation expression: "
                    + contextNavigation.getNavigationExpression(), e);
        }
    }

    private Ast rewrite(Ast ast) {
        if(ast.getAstType() == AstType.LITERAL) {
            return ast;
        } else if(ast.getAstType() == AstType.PATH || ast.getAstType() == AstType.PROPERTY) {
            return pathExpressionRewriter.rewrite(ast);
        }
        return complexExpressionRewriter.rewrite(ast);
    }

    private String translateExpression(Ast ast) {
        return expressionLanguage.translate(ast);
    }

    private ExpressionType convert(AstType astType) {
        switch (astType) {
            case LITERAL:
                return ExpressionType.LITERAL;
            case PATH:
            case PROPERTY:
                return ExpressionType.PATH;
            case COMPLEX:
                return ExpressionType.COMPLEX;
        }
        return ExpressionType.COMPLEX;
    }
}
