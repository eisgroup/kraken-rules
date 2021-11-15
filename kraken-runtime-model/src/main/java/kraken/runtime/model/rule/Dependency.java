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

package kraken.runtime.model.rule;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class Dependency {

    private final String contextName;
    private final String targetPath;
    private final boolean isContextDependency;

    public Dependency(String contextName, String targetPath, boolean isContextDependency) {
        this.contextName = contextName;
        this.targetPath = targetPath;
        this.isContextDependency = isContextDependency;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public String getContextName() {
        return contextName;
    }

    public boolean isContextDependency() {
        return isContextDependency;
    }
}
