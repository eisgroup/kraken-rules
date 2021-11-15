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
package kraken.el.ast.visitor;

import java.util.ArrayDeque;
import java.util.Deque;

import kraken.el.ast.Expression;
import kraken.el.ast.ReferenceValue;

/**
 * Ast visitor that tracks parent ReferenceValue node and also all parent AST nodes
 *
 * @author mulevicius
 */
public abstract class QueuedAstVisitor<T> extends BaseAstVisitor<T> {

    /**
     * ReferenceValue node that current visited node is nested in
     */
    protected ReferenceValue currentReferenceValue;

    /**
     * AST node chain stack
     */
    protected Deque<Expression> astNodeQueue = new ArrayDeque<>();

    @Override
    public T visit(Expression expression) {
        astNodeQueue.push(expression);

        ReferenceValue unconsumedReferenceValue = null;
        if(expression instanceof ReferenceValue) {
            unconsumedReferenceValue = currentReferenceValue;
            currentReferenceValue = (ReferenceValue) expression;
        }

        T t = super.visit(expression);

        if(expression instanceof ReferenceValue) {
            currentReferenceValue = unconsumedReferenceValue;
        }

        astNodeQueue.pop();

        return t;
    }
}
