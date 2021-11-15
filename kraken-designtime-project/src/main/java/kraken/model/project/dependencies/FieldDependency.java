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
package kraken.model.project.dependencies;

import java.util.Objects;

/**
 * Indicates detected dependency between two fields
 *
 * @author rimas
 * @since 1.0
 */
public class FieldDependency {

    private String contextName;

    private String path;

    private boolean contextDependency;

    public FieldDependency(String contextName, String path, boolean contextDependency) {
        this.contextName = contextName;
        this.path = path;
        this.contextDependency = contextDependency;
    }

    public String getContextName() {
        return contextName;
    }

    public String getPath() {
        return path;
    }

    public boolean isContextDependency() {
        return contextDependency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FieldDependency that = (FieldDependency) o;
        return Objects.equals(contextName, that.contextName) &&
                Objects.equals(path, that.path) &&
                Objects.equals(contextDependency, that.contextDependency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(contextName, path, contextDependency);
    }

    @Override
    public String toString() {
        return contextName + '.' + path;
    }
}
