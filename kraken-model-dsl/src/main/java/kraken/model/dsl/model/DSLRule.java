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
public class DSLRule {

    private DSLMetadata metadata;

    private String name;

    private String description;

    private String contextName;

    private String fieldName;

    private DSLExpression condition;

    private DSLPayload payload;

    private Integer priority;

    private boolean serverSideOnly;

    public DSLRule(DSLMetadata metadata,
                   String name,
                   String description,
                   String contextName,
                   String fieldName,
                   DSLExpression condition,
                   DSLPayload payload,
                   Integer priority,
                   boolean serverSideOnly) {
        this.metadata = metadata;
        this.name = Objects.requireNonNull(name);
        this.description = description;
        this.contextName = Objects.requireNonNull(contextName);
        this.fieldName = Objects.requireNonNull(fieldName);
        this.condition = condition;
        this.payload = Objects.requireNonNull(payload);
        this.priority = priority;
        this.serverSideOnly = serverSideOnly;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getContextName() {
        return contextName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public DSLExpression getCondition() {
        return condition;
    }

    public DSLPayload getPayload() {
        return payload;
    }

    public DSLMetadata getMetadata() {
        return metadata;
    }

    public Integer getPriority() {
        return priority;
    }

    public boolean isServerSideOnly() {
        return serverSideOnly;
    }
}
