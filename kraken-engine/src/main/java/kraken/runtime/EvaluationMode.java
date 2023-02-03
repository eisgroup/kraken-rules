/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import kraken.annotations.API;
import kraken.model.payload.PayloadType;

/**
 * Enumerates all possible evaluation modes supported by Kraken engine. Evaluation mode
 * dictates what type of rules will be evaluated during a Kraken Engine call.
 *
 * @author Tomas Dapkunas
 * @since 1.36.0
 */
@API
public enum EvaluationMode {

    ALL(PayloadType.values()),

    INQUIRY(PayloadType.VISIBILITY, PayloadType.ACCESSIBILITY),

    PRESENTATIONAL(PayloadType.VISIBILITY, PayloadType.ACCESSIBILITY, PayloadType.DEFAULT, PayloadType.USAGE);

    private final Set<PayloadType> supportedTypes;

    EvaluationMode(PayloadType... supportedTypes) {
        Set<PayloadType> types = new HashSet<>();
        Collections.addAll(types, supportedTypes);

        this.supportedTypes = types;
    }

    /**
     * Performs a check whether this evaluation mode supports given payload type.
     *
     * @param payloadType    Payload type.
     * @return {@code true} if evaluation model support payload type, {@code false} otherwise.
     */
    public boolean isSupported(PayloadType payloadType) {
        return this.supportedTypes.contains(payloadType);
    }

}
