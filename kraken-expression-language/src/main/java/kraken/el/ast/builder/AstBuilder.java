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
package kraken.el.ast.builder;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.cache2k.Cache;
import org.cache2k.Cache2kBuilder;
import org.cache2k.integration.CacheLoaderException;

import kraken.el.Common;
import kraken.el.Kel;
import kraken.el.Kel.ExpressionContext;
import kraken.el.ast.Ast;
import kraken.el.ast.AstType;
import kraken.el.ast.Expression;
import kraken.el.ast.builder.KelErrorListener.KelError;
import kraken.el.scope.Scope;

/**
 * Builds Abstract Syntax Tree from Kraken Expression
 *
 * @author mulevicius
 */
public class AstBuilder {

    private AstBuilder() {
    }

    public static final String AST_CACHE_ENTRY_CAPACITY_PROP = "kraken.expression.ast.entryCapacity";
    public static final String AST_CACHE_EXPIRE_AFTER_WRITE_PROP = "kraken.expression.ast.expireAfterWrite";

    public static final String AST_CACHE_ENTRY_CAPACITY = System.getProperty(AST_CACHE_ENTRY_CAPACITY_PROP,
            System.getenv().getOrDefault(AST_CACHE_ENTRY_CAPACITY_PROP, "1000000"));

    public static final String AST_CACHE_EXPIRE_AFTER_WRITE = System.getProperty(AST_CACHE_EXPIRE_AFTER_WRITE_PROP,
            System.getenv().getOrDefault(AST_CACHE_EXPIRE_AFTER_WRITE_PROP, "86400"));

    private static final Cache<ExpressionKey, Ast> astCache = new Cache2kBuilder<ExpressionKey, Ast>() {}
            .name("AST")
            .entryCapacity(Long.valueOf(AST_CACHE_ENTRY_CAPACITY))
            .expireAfterWrite(Long.valueOf(AST_CACHE_EXPIRE_AFTER_WRITE), TimeUnit.SECONDS)
            .loader(key -> {
                ExpressionContext expressionContext = parse(key.getExpression());
                AstGeneratingVisitor astGeneratingVisitor = new AstGeneratingVisitor(key.getScope());
                Expression expressionNode = astGeneratingVisitor.visit(expressionContext);

                return new Ast(
                    expressionNode,
                    astGeneratingVisitor.getFunctions(),
                    astGeneratingVisitor.getReferences(),
                    astGeneratingVisitor.getGenerationErrors()
                );
            })
            .build();

    private static final Map<String, Ast> literalAstCache = new ConcurrentHashMap<>();

    public static Ast from(String expression, Scope scope) {
        try {
            Ast ast = literalAstCache.get(expression);
            if(ast == null) {
                ast = astCache.get(new ExpressionKey(expression, scope));
                if(ast.getAstType() == AstType.LITERAL) {
                    literalAstCache.putIfAbsent(expression, ast);
                }
            }
            return ast;
        } catch (ParseCancellationException e) {
            throw new AstBuildingException("Error while building Abstract Syntax Tree from expression: " + expression, e);
        } catch (CacheLoaderException e) {
            throw new AstBuildingException("Error while building Abstract Syntax Tree from expression: " + expression, e);
        }
    }

    private static ExpressionContext parse(String expression) {
        KelErrorListener listener = new KelErrorListener();
        Common lexer = lexerForExpression(expression);
        lexer.removeErrorListeners();
        lexer.addErrorListener(listener);
        TokenStream tokenStream = new CommonTokenStream(lexer);
        Kel parser = new Kel(tokenStream);
        parser.setErrorHandler(new KelErrorStrategy());
        parser.getInterpreter().setPredictionMode(PredictionMode.LL);
        parser.removeErrorListeners();
        parser.addErrorListener(listener);

        ExpressionContext expressionContext = parser.expression();

        if(!listener.getErrors().isEmpty()) {
            KelError error = listener.getErrors().get(0);
            throw new ParseCancellationException(error.getMessage());
        }

        return expressionContext;
    }

    private static Common lexerForExpression(String expression) {
        CharStream stream = CharStreams.fromString(expression);
        return new CommonLexer(stream);
    }

    public static class ExpressionKey {

        private String expression;

        private Scope scope;

        public ExpressionKey(String expression, Scope scope) {
            this.expression = expression;
            this.scope = scope;
        }

        public String getExpression() {
            return expression;
        }

        public Scope getScope() {
            return scope;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            ExpressionKey that = (ExpressionKey) o;
            return Objects.equals(expression, that.expression)
                    && Objects.equals(scope.getName(), that.scope.getName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(expression, scope.getName());
        }
    }
}
