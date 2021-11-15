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

package kraken.testing.reducer;

import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.runtime.engine.result.ValidationPayloadResult;

/**
 * Kraken results reducer return type, for {@link KrakenReducers#VALIDATION} reducer.
 *
 * @author psurinin
 * @see kraken.runtime.engine.result.reducers.EntryPointResultReducer
 * @see KrakenReducers
 * @since 1.0.38
 */
public class ValidationMetadata implements ResultMetadataContainer {
    private Boolean success;
    private ResultMetadata resultMetadata;
    private String messageCode;
    private String message;
    private Boolean evaluateWithError;

    public ValidationMetadata(ValidationPayloadResult validationPayloadResult, ResultMetadata resultMetadata) {
        if (validationPayloadResult != null) {
            this.success = validationPayloadResult.getSuccess();
            this.messageCode = validationPayloadResult.getMessageCode();
            this.message = validationPayloadResult.getMessage();
            if (validationPayloadResult instanceof AssertionPayloadResult) {
                this.evaluateWithError = ((AssertionPayloadResult) validationPayloadResult).getException().isPresent();
            }
        }
        this.resultMetadata = resultMetadata;
    }

    public ResultMetadata getResultMetadata() {
        return resultMetadata;
    }

    public Boolean getSuccess() {
        return success;
    }

    public Boolean getEvaluateWithError() {
        return evaluateWithError;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public String getMessage() {
        return message;
    }
}
