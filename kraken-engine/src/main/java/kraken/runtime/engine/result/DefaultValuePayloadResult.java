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

import kraken.annotations.API;
import kraken.model.derive.DerivePayload;
import kraken.runtime.engine.events.RuleEvent;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Payload result class to store results of {@link DerivePayload} execution
 *
 * @author rimas
 * @since 1.0
 */
@API
public class DefaultValuePayloadResult implements PayloadResult, ExceptionAwarePayloadResult{

    private List<RuleEvent> events;

    private Exception exception;

    public DefaultValuePayloadResult(Exception exception) {
        this.exception = exception;
        this.events = Collections.emptyList();
    }

    public DefaultValuePayloadResult(List<RuleEvent> events) {
        this.events = Objects.requireNonNull(events);
    }

    @Override
    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    public List<RuleEvent> getEvents() {
        return events;
    }

}
