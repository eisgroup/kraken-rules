/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.tracer;

/**
 * An SPI for implementing void trace operation. A void operation is a specific subtype
 * of {@code Operation} which should be used when there is no result to describe
 * or result description is not needed.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public interface VoidOperation extends Operation<Void> {

    @Override
    default String describeAfter(Void result) {
        throw new IllegalStateException("Void operation has no result to describe!");
    }

}
