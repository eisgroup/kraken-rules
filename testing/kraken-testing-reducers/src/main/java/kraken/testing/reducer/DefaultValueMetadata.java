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

import kraken.runtime.engine.events.RuleEvent;
import kraken.runtime.engine.events.ValueChangedEvent;
import kraken.runtime.engine.result.DefaultValuePayloadResult;

import java.util.List;
import java.util.stream.Collectors;
/**
 * Kraken results reducer return type, for {@link KrakenReducers#DEFAULT} reducer.
 *
 * @author psurinin
 *
 * @see kraken.runtime.engine.result.reducers.EntryPointResultReducer
 * @see KrakenReducers
 * @since 1.0.38
 */
public class DefaultValueMetadata implements ResultMetadataContainer {
    private List<SerializableEvent> events;
    private ResultMetadata resultMetadata;
    private boolean evaluatedWithError;

    public DefaultValueMetadata(DefaultValuePayloadResult pr, ResultMetadata resultMetadata) {
        if (pr != null) {
            this.events = pr.getEvents().stream()
                    .filter(e -> e instanceof ValueChangedEvent)
                    .map(e -> new SerializableEvent(
                            ((ValueChangedEvent) e).getPreviousValue(),
                            ((ValueChangedEvent) e).getNewValue())
                    )
                    .collect(Collectors.toList());
            this.evaluatedWithError = pr.getException().isPresent();
        }
            this.resultMetadata = resultMetadata;
    }

    public List<SerializableEvent> getEvents() {
        return events;
    }

    public ResultMetadata getResultMetadata() {
        return resultMetadata;
    }

    public boolean isEvaluatedWithError() {
        return evaluatedWithError;
    }

    public static class SerializableEvent implements RuleEvent {

        private final Object previousValue;
        private final Object newValue;

        public SerializableEvent(Object previousValue, Object newValue) {

            this.previousValue = previousValue;
            this.newValue = newValue;
        }

        public Object getPreviousValue() {
            return previousValue;
        }

        public Object getNewValue() {
            return newValue;
        }
    }
}
