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
package kraken.el.ast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import kraken.el.ast.validation.AstError;
import kraken.el.ast.validation.AstValidatingVisitor;

/**
 * Root Abstract Syntax Tree node that wraps child node for convenience.
 *
 * @author mulevicius
 */
public class Ast {

    private final Expression expression;

    private final AstType astType;

    private final Map<String, Function> functions;

    private final Collection<Reference> references;

    private final AtomicReference<Collection<AstError>> syntaxErrorsReference = new AtomicReference<>();

    public Ast(Expression expression) {
        this.expression = expression;
        this.functions = new HashMap<>();
        this.references = new ArrayList<>();

        this.astType = determineAstType(expression);
    }

    public Ast(Expression expression, Map<String, Function> functions, Collection<Reference> references) {
        this.functions = Objects.requireNonNull(functions);
        this.references = Objects.requireNonNull(references);
        this.expression = Objects.requireNonNull(expression);

        this.astType = determineAstType(expression);
    }

    public Expression getExpression() {
        return expression;
    }

    public AstType getAstType() {
        return astType;
    }

    /**
     *
     * @return all global function references parsed and collected from expression
     */
    public Map<String, Function> getFunctions() {
        return functions;
    }

    /**
     *
     * @return all global path references parsed and collected from expression
     */
    public Collection<Reference> getReferences() {
        return references;
    }

    public Object getCompiledLiteralValue() {
        if(AstType.LITERAL == astType) {
            return ((LiteralExpression) expression).getValue();
        }
        return null;
    }

    public String getCompiledLiteralValueType() {
        if(AstType.LITERAL == astType) {
            if(expression instanceof Null) {
                return null;
            }
            return ((LiteralExpression) expression).getEvaluationType().getName();
        }
        return null;
    }

    public Collection<AstError> getSyntaxErrors() {
        Collection<AstError> syntaxErrors = syntaxErrorsReference.get();
        if(syntaxErrors == null) {
            AstValidatingVisitor validatingVisitor = new AstValidatingVisitor();
            validatingVisitor.visit(this.expression);
            syntaxErrors = validatingVisitor.getSyntaxErrors();
            if (!syntaxErrorsReference.compareAndSet(null, syntaxErrors)) {
                return syntaxErrorsReference.get();
            }
        }
        return syntaxErrors;
    }

    private AstType determineAstType(Expression expression) {
        if(expression instanceof ReferenceValue) {
            return determineAstType(((ReferenceValue) expression).getReference());
        } else if (expression instanceof LiteralExpression) {
            return AstType.LITERAL;
        } else if (expression instanceof Identifier
                && ((Identifier) expression).isSimpleBeanPath()
                && ((Identifier) expression).isReferenceInCurrentScope()) {
            return AstType.PROPERTY;
        } else if (expression instanceof Path
                && ((Path) expression).isSimpleBeanPath()
                && ((Path) expression).isReferenceInCurrentScope()) {
            return AstType.PATH;
        }
        return AstType.COMPLEX;
    }

}
