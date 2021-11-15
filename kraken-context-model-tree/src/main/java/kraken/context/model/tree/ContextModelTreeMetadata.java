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

package kraken.context.model.tree;

import kraken.el.TargetEnvironment;

import java.io.Serializable;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class ContextModelTreeMetadata implements Serializable {

    private final String namespace;
    private final TargetEnvironment targetEnvironment;

    public ContextModelTreeMetadata(String namespace, TargetEnvironment targetEnvironment) {
        this.namespace = namespace;
        this.targetEnvironment = targetEnvironment;
    }

    public String getNamespace() {
        return namespace;
    }

    public TargetEnvironment getTargetEnvironment() {
        return targetEnvironment;
    }
}
