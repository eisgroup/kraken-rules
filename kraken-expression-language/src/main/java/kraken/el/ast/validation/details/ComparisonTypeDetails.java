/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.el.ast.validation.details;

import kraken.el.ast.Expression;

/**
 * Details to be added to {@code AstError} when {@code ComparisonOperation} validation
 * fails due to type incompatibility.
 *
 * @author Tomas Dapkunas
 * @since 1.29.0
 */
public final class ComparisonTypeDetails extends AstDetails {

    private final Expression left;
    private final Expression right;

    public ComparisonTypeDetails(Expression left, Expression right) {
        super(AstDetailsType.COMPARISON_TYPE_ERROR);
        this.left = left;
        this.right = right;
    }

    public Expression getLeft() {
        return left;
    }

    public Expression getRight() {
        return right;
    }

}
