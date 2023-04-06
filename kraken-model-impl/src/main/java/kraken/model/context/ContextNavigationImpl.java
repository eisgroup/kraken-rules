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
package kraken.model.context;

import java.io.Serializable;

public class ContextNavigationImpl implements ContextNavigation, Serializable {

    private String targetName;

    private String navigationExpression;

    private Cardinality cardinality;

    private Boolean forbidReference;

    @Override public String getTargetName() {
        return targetName;
    }

    @Override public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    @Override public String getNavigationExpression() {
        return navigationExpression;
    }

    @Override public void setNavigationExpression(String navigationExpression) {
        this.navigationExpression = navigationExpression;
    }

    @Override
    public Cardinality getCardinality() {
        return cardinality;
    }

    @Override
    public void setCardinality(Cardinality cardinality) {
        this.cardinality = cardinality;
    }

    @Override
    public Boolean getForbidReference() {
        return forbidReference;
    }

    @Override
    public void setForbidReference(Boolean forbidReference) {
        this.forbidReference = forbidReference;
    }
}
