/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
