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

import kraken.el.ast.Expression;
import kraken.el.ast.Identifier;
import kraken.el.ast.visitor.AstRewritingVisitor;
import kraken.el.scope.type.Type;
import kraken.model.project.KrakenProject;

/**
 * Visitor that prepends '__references__' for variables that are supplied as references
 * in {@link kraken.el.TargetEnvironment#JAVASCRIPT} environment
 *
 * @author mulevicius
 */
public class ReferencesAppendingRewritingVisitor extends AstRewritingVisitor {

    private KrakenProject krakenProject;

    public ReferencesAppendingRewritingVisitor(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    @Override
    public Expression visit(Identifier identifier) {
        Type evaluationType = identifier.getEvaluationType().unwrapArrayType();

        if(!evaluationType.isPrimitive()
                && !evaluationType.isDynamic()
                && krakenProject.getContextDefinitions().containsKey(identifier.getIdentifierToken())
                && identifier.isReferenceInGlobalScope()) {
            return new Identifier(
                identifier.getIdentifierToken(),
                "__references__." + identifier.getIdentifier(),
                identifier.getScope(),
                identifier.getEvaluationType(),
                identifier.getToken()
            );
        }

        return super.visit(identifier);
    }
}
