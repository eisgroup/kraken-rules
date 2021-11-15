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

import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.model.context.RuntimeContextDefinition;

import java.util.Objects;

/**
 * Class to contain information about child extraction. is used in
 * {@link ContextDataExtractor}
 *
 * @author psurinin
 * @since 1.0
 */
public class ContextChildExtractionInfo {

    private final RuntimeContextDefinition from;

    private final DataContext parentDataContext;

    private final String childContextName;

    ContextChildExtractionInfo(RuntimeContextDefinition from, DataContext parentDataContext, String childContextName) {
        this.from = from;
        this.parentDataContext = parentDataContext;
        this.childContextName = childContextName;
    }

    public RuntimeContextDefinition getFrom() {
        return from;
    }

    public DataContext getParentDataContext() {
        return parentDataContext;
    }

    public String getChildContextName() {
        return childContextName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ContextChildExtractionInfo that = (ContextChildExtractionInfo) o;
        return Objects.equals(parentDataContext, that.parentDataContext) &&
                Objects.equals(childContextName, that.childContextName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentDataContext, childContextName);
    }

    @Override
    public String toString() {
        return "ExtractionInfo[" +
                from +
                "->" + childContextName +
                "]";
    }
}
