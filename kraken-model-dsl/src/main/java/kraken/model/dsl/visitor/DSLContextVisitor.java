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
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.RuleContext;

/**
 * @author mulevicius
 */
public class DSLContextVisitor extends KrakenDSLBaseVisitor<DSLContext> {

    @Override
    public DSLContext visitContext(KrakenDSL.ContextContext ctx) {
        return ctx.systemContext() != null
            ? toSystemContext(ctx.systemContext())
            : toContext(ctx.modeledContext());
    }

    private DSLContext toSystemContext(KrakenDSL.SystemContextContext ctx) {
        String name = ctx.contextName().getText();
        Collection<DSLContextField> fields = ctx.field() != null
            ? ctx.field().stream()
                .map(DSLContextVisitor::toContextField)
                .collect(Collectors.toList())
            : Collections.emptyList();

        return new DSLContext(name, true, false, true, List.of(), fields, List.of());
    }

    private DSLContext toContext(KrakenDSL.ModeledContextContext ctx) {
        String name = ctx.contextName().getText();

        boolean strict = ctx.NOTSTRICT() == null;
        boolean isRoot = ctx.ROOT() != null;

        Collection<String> inheritedContexts = ctx.inheritedContexts() != null
            ? ctx.inheritedContexts().contextName().stream()
                .map(RuleContext::getText)
                .collect(Collectors.toList())
            : Collections.emptyList();

        Collection<DSLContextChild> children = ctx.child() != null
            ? ctx.child().stream()
                .map(this::toContextChild)
                .collect(Collectors.toList())
            : Collections.emptyList();

        Collection<DSLContextField> fields = ctx.field() != null
            ? ctx.field().stream()
                .map(DSLContextVisitor::toContextField)
                .collect(Collectors.toList())
            : Collections.emptyList();

        return new DSLContext(name, strict, isRoot, false, inheritedContexts, fields, children);
    }

    public static DSLContextField toContextField(KrakenDSL.FieldContext ctx) {
        String name = ctx.fieldName.getText();
        String type = ctx.fieldType().getText();

        if (PrimitiveFieldDataType.isPrimitiveType(type.toUpperCase()) || SystemDataTypes.isSystemDataType(
            type.toUpperCase())) {
            type = type.toUpperCase();
        }

        DSLCardinality cardinality = ctx.OP_MULT() != null ? DSLCardinality.MULTIPLE : DSLCardinality.SINGLE;
        String path = ctx.pathExpression() != null ? ctx.pathExpression().getText() : null;

        Boolean forbidTarget = null;
        if(ctx.EXTERNAL() != null || ctx.fieldModifiers() != null
            && ctx.fieldModifiers().fieldModifier().stream().anyMatch(m -> m.FORBID_TARGET() != null)) {
            forbidTarget = true;
        }

        Boolean forbidReference = null;
        if(ctx.fieldModifiers() != null
            && ctx.fieldModifiers().fieldModifier().stream().anyMatch(m -> m.FORBID_REFERENCE() != null)) {
            forbidReference = true;
        }

        return new DSLContextField(name, type, cardinality, path, forbidTarget, forbidReference);
    }

    private DSLContextChild toContextChild(KrakenDSL.ChildContext ctx) {
        String name = ctx.contextName().getText();
        DSLCardinality cardinality = ctx.OP_MULT() != null ? DSLCardinality.MULTIPLE : DSLCardinality.SINGLE;
        DSLExpression navigationExpression = ctx.inlineExpression() != null
                ? ExpressionReader.read(ctx.inlineExpression())
                : null;
        Boolean forbidReference = null;
        if(ctx.childModifiers() != null
            && ctx.childModifiers().childModifier().stream().anyMatch(m -> m.FORBID_REFERENCE() != null)) {
            forbidReference = true;
        }
        return new DSLContextChild(name, cardinality, navigationExpression, forbidReference);
    }

}
