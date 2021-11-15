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
package kraken.runtime.engine.context.info.navpath;

import kraken.runtime.engine.context.info.ContextInstanceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link ContextInstanceInfo} that has additional metadata as
 * a collection of navigation expressions, that is returned as {@link DataNavigationPath}
 *
 * @author psurinin
 * @since 1.0
 */
public class DataNavigationContextInstanceInfo implements ContextInstanceInfo {

    private final List<String> navigationExpressions;
    private final String name;
    private final String id;

    public DataNavigationContextInstanceInfo(
            String name,
            String id,
            String navigationExpression
    ) {
        this.name = name;
        this.id = id;
        navigationExpressions = new ArrayList<>();
        navigationExpressions.add(navigationExpression);
    }

    public DataNavigationContextInstanceInfo(String name, String id, List<String> navigationExpressions) {
        this.navigationExpressions = navigationExpressions;
        this.name = name;
        this.id = id;
    }

    public DataNavigationContextInstanceInfo(ContextInstanceInfo parentInfo) {
        this.name = parentInfo.getContextName();
        this.id = parentInfo.getContextInstanceId();
        this.navigationExpressions = new ArrayList<>(((DataNavigationContextInstanceInfo) parentInfo).navigationExpressions);
    }

    @Override
    public String getContextInstanceId() {
        return id;
    }

    @Override
    public String getContextName() {
        return name;
    }

    public static DataNavigationContextInstanceInfo fromParent(ContextInstanceInfo parentInfo) {
        return new DataNavigationContextInstanceInfo(parentInfo);
    }

    public DataNavigationContextInstanceInfo append(DataNavigationContextInstanceInfo info) {
        final ArrayList<String> dataNavigations = new ArrayList<>(this.navigationExpressions);
        dataNavigations.addAll(info.navigationExpressions);

        return new DataNavigationContextInstanceInfo(info.getContextName(), info.id, dataNavigations);
    }

    public DataNavigationPath getNavigation() {
        return new DataNavigationPath(navigationExpressions);
    }

}
