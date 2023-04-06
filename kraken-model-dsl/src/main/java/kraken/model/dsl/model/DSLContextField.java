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

import java.util.Objects;

/**
 * @author mulevicius
 */
public class DSLContextField {

    private String name;

    private String type;

    private DSLCardinality cardinality;

    private String path;

    private Boolean forbidTarget;

    private Boolean forbidReference;

    public DSLContextField(String name,
                           String type,
                           DSLCardinality cardinality,
                           String path,
                           Boolean forbidTarget,
                           Boolean forbidReference
    ) {
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.cardinality = Objects.requireNonNull(cardinality);
        this.path = path;
        this.forbidTarget = forbidTarget;
        this.forbidReference = forbidReference;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public DSLCardinality getCardinality() {
        return cardinality;
    }

    public String getPath() {
        return path;
    }

    public Boolean getForbidTarget() {
        return forbidTarget;
    }

    public Boolean getForbidReference() {
        return forbidReference;
    }
}
