/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl.visitor;

import static kraken.el.ast.builder.Literals.escape;
import static kraken.el.ast.builder.Literals.stripQuotes;

import kraken.el.ast.builder.Literals;
import kraken.model.dsl.KrakenDSL;
import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.model.DSLAnnotation;
import kraken.model.dsl.model.DSLExpression;
import kraken.model.dsl.model.DSLPayload;
import kraken.model.dsl.model.DSLRule;

/**
 * @author mulevicius
 */
public class DSLRuleVisitor extends KrakenDSLBaseVisitor<DSLRule> {

    private final DSLPayloadVisitor payloadVisitor = new DSLPayloadVisitor();
    private final DSLAnnotationVisitor annotationVisitor = new DSLAnnotationVisitor();

    @Override
    public DSLRule visitARule(KrakenDSL.ARuleContext ctx) {
        DSLAnnotation annotation = ctx.annotations() != null
            ? annotationVisitor.visit(ctx.annotations())
            : DSLAnnotation.EMPTY;

        String name = escape(stripQuotes(ctx.ruleName().getText()));
        String description = parseRuleDescription(ctx);
        String contextName = ctx.contextName().getText();
        String fieldName = ctx.pathExpression().getText();

        DSLExpression condition = ctx.ruleCondition() != null
            ? ExpressionReader.read(ctx.ruleCondition().inlineExpression())
            : null;
        DSLPayload payload = payloadVisitor.visit(ctx.payload());

        Integer priority = parsePriority(ctx);

        return new DSLRule(annotation.getMetadata(), name, description, contextName, fieldName, condition, payload,
            priority);
    }

    private Integer parsePriority(KrakenDSL.ARuleContext ctx) {
        if(ctx.rulePriority() == null) {
            return null;
        }
        if(ctx.rulePriority().MAX() != null) {
            return Integer.MAX_VALUE;
        }
        if(ctx.rulePriority().MIN() != null) {
            return Integer.MIN_VALUE;
        }
        if(ctx.rulePriority().integerLiteral() != null) {
            return Literals.getInteger(ctx.rulePriority().integerLiteral().getText());
        }
        throw new IllegalStateException("Cannot parse rule priority from: " + ctx.rulePriority().getText());
    }

    private String parseRuleDescription(KrakenDSL.ARuleContext ctx) {
        return ctx.ruleDescription() != null
            ? escape(stripQuotes(ctx.ruleDescription().description.getText()))
            : null;
    }

}
