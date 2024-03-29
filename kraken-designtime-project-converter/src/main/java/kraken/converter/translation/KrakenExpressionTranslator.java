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
import static kraken.message.SystemMessageBuilder.Message.CONVERSION_CANNOT_TRANSLATE_EXPRESSION;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import kraken.converter.KrakenProjectConversionException;
import kraken.converter.translation.ast.AstRewriter;
import kraken.converter.translation.ast.JavaRewriter;
import kraken.converter.translation.ast.JavaScriptRewriter;
import kraken.converter.translation.ast.SimplePathRewriter;
import kraken.el.ExpressionLanguage;
import kraken.el.KrakenKel;
import kraken.el.TargetEnvironment;
import kraken.el.ast.Ast;
import kraken.el.ast.AstType;
import kraken.el.ast.Template;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.ast.builder.AstBuildingException;
import kraken.el.scope.Scope;
import kraken.message.SystemMessageBuilder;
import kraken.model.Expression;
import kraken.model.Function;
import kraken.model.Rule;
import kraken.model.context.ContextDefinition;
import kraken.model.context.ContextNavigation;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.dependencies.FieldDependency;
import kraken.model.project.dependencies.RuleDependencyExtractor;
import kraken.model.project.scope.ScopeBuilder;
import kraken.model.project.scope.ScopeBuilderProvider;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.expression.ExpressionType;
import kraken.runtime.model.expression.ExpressionVariable;
import kraken.runtime.model.expression.ExpressionVariableType;
import kraken.runtime.model.rule.payload.validation.ErrorMessage;

/**
 * Translates designtime expression to runtime expression in {@link TargetEnvironment}
 *
 * @author mulevicius
 */
public class KrakenExpressionTranslator {

    private final ExpressionLanguage expressionLanguage;
    private final AstRewriter complexExpressionRewriter;
    private final AstRewriter pathExpressionRewriter;
    private final RuleDependencyExtractor ruleDependencyExtractor;
    private final KrakenProject krakenProject;
    private final ScopeBuilder scopeBuilder;

    public KrakenExpressionTranslator(KrakenProject krakenProject,
                                      TargetEnvironment targetEnvironment,
                                      RuleDependencyExtractor ruleDependencyExtractor) {

        this.expressionLanguage = KrakenKel.create(targetEnvironment);
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

    public CompiledExpression translateFunctionExpression(Function function) {
        try {
            Scope scope = scopeBuilder.buildFunctionScope(function);

            Ast ast = AstBuilder.from(function.getBody().getExpressionString(), scope);
            Ast rewrittenAst = rewrite(ast);
            kraken.el.Expression e = translateExpression(rewrittenAst);
            return new CompiledExpression(
                e.getExpression(),
                function.getBody().getExpressionString(),
                convert(rewrittenAst.getAstType()),
                rewrittenAst.getAstType() == AstType.LITERAL ? (Serializable) rewrittenAst.getCompiledLiteralValue() : null,
                rewrittenAst.getExpression().getEvaluationType().getName(),
                List.of(),
                e.getAst()
            );
        } catch (AstBuildingException e) {
            var message = SystemMessageBuilder.create(CONVERSION_CANNOT_TRANSLATE_EXPRESSION)
                .parameters(function.getBody().getExpressionString())
                .build();
            throw new KrakenProjectConversionException(message, e);
        }
    }

    public CompiledExpression translateExpression(Rule rule, Expression expression) {
        try {
            ContextDefinition contextDefinition = krakenProject.getContextDefinitions().get(rule.getContext());
            Scope scope = scopeBuilder.buildScope(contextDefinition);

            var dependencies = ruleDependencyExtractor.extractDependencies(expression, rule, scope);

            List<ExpressionVariable> expressionVariables = dependencies.stream()
                    .filter(FieldDependency::isCcrDependency)
                    .map(FieldDependency::getContextName)
                    .distinct()
                    .map(contextName -> new ExpressionVariable(contextName, ExpressionVariableType.CROSS_CONTEXT))
                    .collect(Collectors.toList());

            Ast ast = AstBuilder.from(expression.getExpressionString(), scope);
            Ast rewrittenAst = rewrite(ast);
            kraken.el.Expression e = translateExpression(rewrittenAst);
            return new CompiledExpression(
                e.getExpression(),
                expression.getExpressionString(),
                convert(rewrittenAst.getAstType()),
                rewrittenAst.getAstType() == AstType.LITERAL ? (Serializable) rewrittenAst.getCompiledLiteralValue() : null,
                rewrittenAst.getExpression().getEvaluationType().getName(),
                expressionVariables,
                e.getAst()
            );
        } catch (AstBuildingException e) {
            var message = SystemMessageBuilder.create(CONVERSION_CANNOT_TRANSLATE_EXPRESSION)
                .parameters(expression.getExpressionString())
                .build();
            throw new KrakenProjectConversionException(message, e);
        }
    }

    public ErrorMessage translateErrorMessage(Rule rule, kraken.model.ErrorMessage errorMessage) {
        if(errorMessage == null) {
            return null;
        }
        if(errorMessage.getErrorMessage() == null) {
            return new ErrorMessage(errorMessage.getErrorCode(), List.of(), List.of());
        }
        ContextDefinition contextDefinition = krakenProject.getContextDefinitions().get(rule.getContext());
        Scope scope = scopeBuilder.buildScope(contextDefinition);

        Template template = AstBuilder.from(asTemplateExpression(errorMessage.getErrorMessage()), scope).asTemplate();

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
            Ast ast = AstBuilder.from(contextNavigation.getNavigationExpression(), Scope.dynamic());
            Ast rewrittenAst = rewrite(ast);
            kraken.el.Expression e = translateExpression(rewrittenAst);
            return new CompiledExpression(
                e.getExpression(),
                contextNavigation.getNavigationExpression(),
                convert(rewrittenAst.getAstType()),
                rewrittenAst.getAstType() == AstType.LITERAL ? (Serializable) rewrittenAst.getCompiledLiteralValue() : null,
                // navigation expressions are dynamic path expressions and evaluation type is irrelevant
                null,
                List.of(),
                e.getAst()
            );
        } catch (AstBuildingException e) {
            var message = SystemMessageBuilder.create(CONVERSION_CANNOT_TRANSLATE_EXPRESSION)
                .parameters(contextNavigation.getNavigationExpression())
                .build();
            throw new KrakenProjectConversionException(message, e);
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

    private kraken.el.Expression translateExpression(Ast ast) {
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
