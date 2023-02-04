/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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
package kraken.model.validation;

import java.math.BigDecimal;

import kraken.annotations.API;
import kraken.model.context.PrimitiveFieldDataType;
import kraken.model.payload.PayloadType;

/**
 * NumberSetPayload allows to assert that the field value is in some number set.
 * Can only be applied on numerical fields:
 * {@link PrimitiveFieldDataType#INTEGER}, {@link PrimitiveFieldDataType#DECIMAL}, {@link PrimitiveFieldDataType#MONEY}.
 * Number set is defined by min, max and step parameters.
 * <p/>
 * Min specifies minimum value of a number set (inclusive).
 * If minimum value is not specified then it is a negative infinity.
 * <p>
 * Max specifies maximum value of a number set (inclusive).
 * If maximum value is not specified then it is a positive infinity.
 * <p>
 * Either minimum or maximum value must be specified.
 * If both minimum and maximum values are specified, then minimum value must be smaller than maximum value.
 * <p/>
 * Step specifies a distance between two adjacent numbers in a number set. Step must be a number larger than 0.
 * Step starts from minimum value. If minimum value is not specified, then step starts from maximum value.
 * If step is not specified, then number set is a set of continuous real numbers.
 *
 * @author Mindaugas Ulevicius
 */
@API
public interface NumberSetPayload extends ValidationPayload {

    BigDecimal getMin();

    void setMin(BigDecimal min);

    BigDecimal getMax();

    void setMax(BigDecimal max);

    BigDecimal getStep();

    void setStep(BigDecimal step);

    @Override
    default PayloadType getPayloadType() {
        return PayloadType.NUMBER_SET;
    }
}
