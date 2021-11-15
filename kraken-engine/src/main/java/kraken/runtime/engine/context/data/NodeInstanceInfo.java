/*
 *  Copyright 2020 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.engine.context.data;

import kraken.runtime.engine.context.info.ContextInstanceInfo;

import java.util.Objects;

/**
 * DTO for node data object metadata.
 *
 * @author Tomas Dapkunas
 * @since 1.0.40
 */
public final class NodeInstanceInfo {

    private final ContextInstanceInfo contextInstanceInfo;
    private final int hashCode;

    private NodeInstanceInfo(ContextInstanceInfo contextInstanceInfo) {
        this.contextInstanceInfo = contextInstanceInfo;
        this.hashCode = computeHashCode();
    }

    /**
     * Creates and returns an instance of {@code NodeInstanceInfo} with given parameters.
     *
     * @param contextInstanceInfo Node instance metadata.
     * @return Instance of {@code NodeDataContext}.
     */
    public static NodeInstanceInfo from(ContextInstanceInfo contextInstanceInfo) {
        return new NodeInstanceInfo(contextInstanceInfo);
    }

    public String getContextName() {
        return contextInstanceInfo.getContextName();
    }

    public String getContextId() {
        return contextInstanceInfo.getContextInstanceId();
    }

    private int computeHashCode() {
        return Objects.hash(contextInstanceInfo.getContextName(), contextInstanceInfo.getContextInstanceId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NodeInstanceInfo that = (NodeInstanceInfo) o;

        return Objects.equals(contextInstanceInfo.getContextName(), that.contextInstanceInfo.getContextName()) &&
                Objects.equals(contextInstanceInfo.getContextInstanceId(), that.contextInstanceInfo.getContextInstanceId());
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

}
