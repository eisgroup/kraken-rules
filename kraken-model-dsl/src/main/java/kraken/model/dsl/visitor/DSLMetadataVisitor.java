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

import java.util.Map;

import kraken.el.EvaluationContext;
import kraken.el.Expression;
import kraken.el.ExpressionLanguage;
import kraken.el.KrakenKel;
import kraken.el.TargetEnvironment;
import kraken.el.ast.Ast;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.scope.Scope;
import kraken.model.dsl.KrakenDSL;
import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.model.DSLMetadata;

/**
 * @author mulevicius
 */
public class DSLMetadataVisitor extends KrakenDSLBaseVisitor<DSLMetadata> {

    private final EvaluationContext evaluationContext = new EvaluationContext();

    private final ExpressionLanguage expressionLanguage = KrakenKel.create(TargetEnvironment.JAVA);

    @Override
    public DSLMetadata visitMetadataEntry(KrakenDSL.MetadataEntryContext ctx) {
        if (ctx == null) {
            return null;
        }

        return new DSLMetadata(Map.of(key(ctx.metadataKey.getText()), value(ctx.metadataValue.getText())));
    }

    private String key(String expression) {
        return String.valueOf(expressionLanguage.evaluate(translated(expression), evaluationContext));
    }

    private Object value(String expression) {
        return expressionLanguage.evaluate(translated(expression), evaluationContext);
    }

    private Expression translated(String expression) {
        Ast ast = AstBuilder.from(expression, Scope.dynamic());
        return expressionLanguage.translate(ast);
    }

}
