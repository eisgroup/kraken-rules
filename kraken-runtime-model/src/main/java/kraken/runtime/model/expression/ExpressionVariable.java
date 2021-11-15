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

package kraken.runtime.model.expression;

import java.io.Serializable;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class ExpressionVariable implements Serializable {

    private static final long serialVersionUID = 1015046082949584931L;

    private final String name;
    private final ExpressionVariableType type;

    public ExpressionVariable(String name, ExpressionVariableType type) {
        this.name = name;
        this.type = type;
    }

    /**
     * @return name of the variable that kraken expression evaluation context can supply when evaluating expression
     */
    public String getName() {
        return name;
    }

    /**
     * @return type of expression variable that kraken must be supplied; currently only CCR variables are supported
     */
    public ExpressionVariableType getType() {
        return type;
    }
}
