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

import java.util.Optional;

import kraken.el.ast.Expression;
import kraken.el.ast.Identifier;
import kraken.el.ast.visitor.AstRewritingVisitor;
import kraken.model.context.ContextField;
import kraken.model.project.KrakenProject;

/**
 * Rewrites each field reference to full path as defined in {@link ContextField#getFieldPath()}
 *
 * @author mulevicius
 */
public class FieldPathRewritingVisitor extends AstRewritingVisitor {

    private KrakenProject krakenProject;

    FieldPathRewritingVisitor(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    @Override
    public Expression visit(Identifier identifier) {
        String contextDefinitionName = identifier.getScope().getType().unwrapArrayType().getName();
        return Optional.ofNullable(krakenProject.getContextProjection(contextDefinitionName))
                .filter(c -> c.getContextFields().containsKey(identifier.getIdentifierToken()))
                .map(c -> c.getContextFields().get(identifier.getIdentifierToken()))
                .filter(p -> !identifier.getIdentifier().equals(p.getFieldPath()))
                .<Expression>map(p ->
                    new Identifier(
                        identifier.getIdentifierToken(),
                        p.getFieldPath(),
                        identifier.getScope(),
                        identifier.getEvaluationType(),
                        identifier.getToken()
                    ))
                .orElseGet(() -> super.visit(identifier));
    }

}
