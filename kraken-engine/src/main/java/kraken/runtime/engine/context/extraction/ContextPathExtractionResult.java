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
package kraken.runtime.engine.context.extraction;

import kraken.context.path.ContextPath;
import kraken.runtime.model.context.RuntimeContextDefinition;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to have reverse path from {@link ContextPath}
 *
 * @author psurinin
 * @since 1.0
 */
class ContextPathExtractionResult {

    private final List<RuntimeContextDefinition> path;

    ContextPathExtractionResult(Stream<RuntimeContextDefinition> path) {
        this.path = path.collect(Collectors.toList());
    }

    List<RuntimeContextDefinition> getPath() {
        return path;
    }

}
