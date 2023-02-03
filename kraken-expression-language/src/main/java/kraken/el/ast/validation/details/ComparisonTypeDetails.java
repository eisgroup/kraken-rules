/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S. copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified, or incorporated into any other
 * media without EIS Group prior written consent.
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
