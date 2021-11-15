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
package kraken.el.scope.type;

import java.util.Map;

import kraken.el.Value.ArrayTypeContext;
import kraken.el.Value.GenericTypeContext;
import kraken.el.Value.PlainTypeContext;
import kraken.el.Value.PlainTypePrecedenceContext;
import kraken.el.Value.UnionTypeContext;
import kraken.el.ValueBaseVisitor;

/**
 * Parses instance of a {@link Type} from type string token according to type syntax defined in Value.g4
 *
 * @author mulevicius
 */
public class TypeGeneratingVisitor extends ValueBaseVisitor<Type> {

    private final Map<String, Type> globalTypes;

    public TypeGeneratingVisitor(Map<String, Type> globalTypes) {
        this.globalTypes = globalTypes;
    }

    @Override
    public Type visitPlainType(PlainTypeContext ctx) {
        String typeToken = ctx.identifier().getText();
        if(Type.nativeTypes.containsKey(typeToken)) {
            return Type.nativeTypes.get(typeToken);
        }
        if(globalTypes.containsKey(typeToken)) {
            return globalTypes.get(typeToken);
        }
        return new Type(typeToken, false, false);
    }

    @Override
    public Type visitArrayType(ArrayTypeContext ctx) {
        return ArrayType.of(visit(ctx.type()));
    }

    @Override
    public Type visitGenericType(GenericTypeContext ctx) {
        return new GenericType(ctx.identifier().getText());
    }

    @Override
    public Type visitUnionType(UnionTypeContext ctx) {
        return new UnionType(visit(ctx.type(0)), visit(ctx.type(1)));
    }

    @Override
    public Type visitPlainTypePrecedence(PlainTypePrecedenceContext ctx) {
        return visit(ctx.type());
    }

}
