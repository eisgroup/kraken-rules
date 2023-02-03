/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.repository.filter.trace;

import kraken.tracer.VoidOperation;

/**
 * Operation to be added to trace if more than one versioned item remains after filtering.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public final class MultipleDimensionResultOperation implements VoidOperation {

    @Override
    public String describe() {
        return "More than one version remain after applying dimension filters."
            + " Only the first version of multiple will be returned";
    }

}
