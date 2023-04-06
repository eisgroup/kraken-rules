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

import kraken.annotations.API;
import kraken.model.ExpressionType;

/**
 * Defines logic of "navigation" between data contexts, modeling relationships between
 *
 * @author rimas
 * @since 1.0
 */
@API
public interface ContextNavigation {
    /**
     * Name of target context definition
     */
    String getTargetName();

    void setTargetName(String targetName);

    String getNavigationExpression();

    void setNavigationExpression(String navigationExpression);

    Cardinality getCardinality();

    void setCardinality(Cardinality cardinality);

    /**
     *
     * @return true if references to this context through cross context reference should not be allowed
     */
    Boolean getForbidReference();

    void setForbidReference(Boolean forbidReference);
}
