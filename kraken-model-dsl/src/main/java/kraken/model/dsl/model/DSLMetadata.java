/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author mulevicius
 */
public class DSLMetadata {

    public static final DSLMetadata EMPTY = new DSLMetadata(Map.of());

    private Map<String, Object> metadata;

    public DSLMetadata(Map<String, Object> metadata) {
        this.metadata = Objects.requireNonNull(metadata);
    }

    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }
}
