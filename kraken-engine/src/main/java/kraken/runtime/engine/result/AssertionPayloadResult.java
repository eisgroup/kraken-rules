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
package kraken.runtime.engine.result;

import java.util.List;
import java.util.Optional;

import kraken.annotations.API;
import kraken.runtime.model.rule.payload.validation.AssertionPayload;

@API
public class AssertionPayloadResult extends ValidationPayloadResult implements ExceptionAwarePayloadResult {

    private Exception exception;

    public AssertionPayloadResult(Boolean success, AssertionPayload payload, List<Object> templateVariables) {
        super(success, payload, templateVariables);
    }

    public AssertionPayloadResult(Exception exception, AssertionPayload payload) {
        super(null, payload, List.of());
        this.exception = exception;
    }

    @Override
    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

}
