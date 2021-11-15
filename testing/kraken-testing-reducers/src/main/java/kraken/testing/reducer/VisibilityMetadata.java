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

import kraken.runtime.engine.result.VisibilityPayloadResult;

/**
 * Kraken results reducer return type, for {@link KrakenReducers#VISIBILITY} reducer.
 *
 * @author psurinin
 * @see kraken.runtime.engine.result.reducers.EntryPointResultReducer
 * @see KrakenReducers
 * @since 1.0.38
 */
public class VisibilityMetadata implements ResultMetadataContainer {

    private Boolean visible;
    private ResultMetadata resultMetadata;

    public VisibilityMetadata(VisibilityPayloadResult result, ResultMetadata resultMetadata) {
        if (result != null) {
            this.visible = result.getVisible();
        }
        this.resultMetadata = resultMetadata;
    }

    public Boolean getVisible() {
        return visible;
    }

    public ResultMetadata getResultMetadata() {
        return resultMetadata;
    }
}
