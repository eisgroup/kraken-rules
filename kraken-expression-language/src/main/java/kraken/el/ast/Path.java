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

import kraken.el.ast.token.Token;
import kraken.el.scope.Scope;
import kraken.el.scope.ScopeType;
import kraken.el.scope.type.Type;

/**
 * @author mulevicius
 */
public class Path extends Reference {

    private final Reference object;

    private final Reference property;

    private final boolean nullSafe;

    public Path(Reference object, Reference property, boolean nullSafe, Scope scope, Type evaluationType, Token token) {
        super(NodeType.PATH, scope, evaluationType, token);

        this.object = object;
        this.property = property;
        this.nullSafe = nullSafe;
    }

    public Reference getObject() {
        return object;
    }

    public Reference getProperty() {
        return property;
    }

    public boolean isNullSafe() {
        return nullSafe;
    }

    @Override
    public boolean isReferenceInCurrentScope() {
        return object.isReferenceInCurrentScope();
    }

    @Override
    public boolean isReferenceInGlobalScope() {
        return object.isReferenceInGlobalScope();
    }

    @Override
    public ScopeType findScopeTypeOfReference() {
        return object.findScopeTypeOfReference();
    }

    /**
     *
     * @return true if this path is simple bean path expression that does not contain filters, folds, array access, ...
     */
    @Override
    public boolean isSimpleBeanPath() {
        return !object.getEvaluationType().isAssignableToArray()
            && object.isSimpleBeanPath() && property.isSimpleBeanPath();
    }

    @Override
    public boolean isSimplePath() {
        return object.isSimplePath() && property.isSimplePath();
    }

    @Override
    public Reference getFirstReference() {
        return object.getFirstReference();
    }

    @Override
    public String toString() {
        String pathSeparator = nullSafe ? "?." : ".";
        return object + pathSeparator + property;
    }
}
