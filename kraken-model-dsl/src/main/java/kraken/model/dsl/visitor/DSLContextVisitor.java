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

import kraken.model.context.PrimitiveFieldDataType;
import kraken.model.context.SystemDataTypes;
import kraken.model.dsl.KrakenDSLBaseVisitor;
import kraken.model.dsl.KrakenDSL;
import kraken.model.dsl.model.DSLCardinality;
import kraken.model.dsl.model.DSLContext;
import kraken.model.dsl.model.DSLContextChild;
import kraken.model.dsl.model.DSLContextField;
import kraken.model.dsl.model.DSLExpression;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * @author mulevicius
 */
public class DSLContextVisitor extends KrakenDSLBaseVisitor<DSLContext> {

    @Override
    public DSLContext visitContext(KrakenDSL.ContextContext ctx) {
        String name = ctx.contextName().getText();
        boolean strict = ctx.NOTSTRICT() == null;
        Collection<String> inheritedContexts = ctx.inheritedContexts() != null
                ? ctx.inheritedContexts().contextName().stream()
                    .map(contextNameContext -> contextNameContext.getText())
                    .collect(Collectors.toList())
                : Collections.emptyList();

        Collection<DSLContextChild> children = ctx.child() != null
                ? ctx.child().stream()
                    .map(childContext -> toContextChild(childContext))
                    .collect(Collectors.toList())
                : Collections.emptyList();

        Collection<DSLContextField> fields = ctx.field() != null
                ? ctx.field().stream()
                    .map(fieldContext -> toContextField(fieldContext))
                    .collect(Collectors.toList())
                : Collections.emptyList();

        final boolean isRoot = ctx.ROOT() != null;
        return new DSLContext(name, strict, isRoot, inheritedContexts, fields, children);
    }

    private DSLContextField toContextField(KrakenDSL.FieldContext ctx) {
        String name = ctx.fieldName.getText();
        String type = ctx.fieldType().getText();
        if(PrimitiveFieldDataType.isPrimitiveType(type.toUpperCase()) || SystemDataTypes.isSystemDataType(type.toUpperCase())) {
            type = type.toUpperCase();
        }
        DSLCardinality cardinality = ctx.OP_MULT() != null ? DSLCardinality.MULTIPLE : DSLCardinality.SINGLE;
        String path = ctx.pathExpression() != null ? ctx.pathExpression().getText() : null;
        boolean external = ctx.EXTERNAL() != null;
        return new DSLContextField(name, type, cardinality, path, external);
    }

    private DSLContextChild toContextChild(KrakenDSL.ChildContext ctx) {
        String name = ctx.contextName().getText();
        DSLCardinality cardinality = ctx.OP_MULT() != null ? DSLCardinality.MULTIPLE : DSLCardinality.SINGLE;
        DSLExpression navigationExpression = ctx.inlineExpression() != null
                ? ExpressionReader.read(ctx.inlineExpression())
                : null;
        return new DSLContextChild(name, cardinality, navigationExpression);
    }

}
