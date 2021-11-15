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

package kraken.runtime.model.context;

import kraken.model.context.Cardinality;

import java.io.Serializable;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class ContextField implements Serializable {
    private final String name;
    private final String fieldType;
    private final String fieldPath;
    private final Cardinality cardinality;

    public ContextField(String name, String fieldType, String fieldPath, Cardinality cardinality) {
        this.name = name;
        this.fieldType = fieldType;
        this.fieldPath = fieldPath;
        this.cardinality = cardinality;
    }

    public String getName() {
        return name;
    }

    public String getFieldType() {
        return fieldType;
    }

    public String getFieldPath() {
        return fieldPath;
    }

    public Cardinality getCardinality() {
        return cardinality;
    }
}
