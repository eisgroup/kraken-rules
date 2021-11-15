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
package kraken.converter.translation.ast;

import java.util.Collection;

import kraken.el.ast.Ast;
import kraken.el.ast.Expression;
import kraken.el.ast.visitor.AstRewritingVisitor;

/**
 *
 * @author mulevicius
 */
public class VisitingRewriter implements AstRewriter {

    private Collection<AstRewritingVisitor> astRewritingVisitors;

    VisitingRewriter(Collection<AstRewritingVisitor> astRewritingVisitors) {
        this.astRewritingVisitors = astRewritingVisitors;
    }

    @Override
    public Ast rewrite(Ast ast) {
        Expression expression = ast.getExpression();
        for(AstRewritingVisitor visitor : astRewritingVisitors) {
            expression = visitor.visit(expression);
        }
        return new Ast(expression, ast.getFunctions(), ast.getReferences());
    }
}
