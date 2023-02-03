/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.el;

/**
 * @author mulevicius
 */
public class ExpressionLanguageConfiguration {

    /**
     * Enables automatic iteration of function invocation.
     * Function invocation will be iterated over each collection parameter value
     * if function signature accepts singular value instead of collection.
     * Behavior can be switched of per function implementation by annotating
     * method parameter with {@link kraken.el.functionregistry.Iterable}
     */
    private final boolean allowAutomaticIterationWhenInvokingFunctions;

    /**
     * Indicates strict type validation at runtime for operators and bean paths.
     * If this is set to true then at runtime exception will be thrown
     * if types for operations (including path operation) are incorrect or if they are null.
     */
    private final boolean strictTypeMode;

    public ExpressionLanguageConfiguration(boolean allowAutomaticIterationWhenInvokingFunctions,
                                           boolean strictTypeMode) {
        this.allowAutomaticIterationWhenInvokingFunctions = allowAutomaticIterationWhenInvokingFunctions;
        this.strictTypeMode = strictTypeMode;
    }

    public boolean isAllowAutomaticIterationWhenInvokingFunctions() {
        return allowAutomaticIterationWhenInvokingFunctions;
    }

    public boolean isStrictTypeMode() {
        return strictTypeMode;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private boolean allowAutomaticIterationWhenInvokingFunctions;

        private boolean strictTypeMode;

        public Builder strictTypeMode() {
            this.strictTypeMode = true;
            return this;
        }

        public Builder allowAutomaticIterationWhenInvokingFunctions() {
            this.allowAutomaticIterationWhenInvokingFunctions = true;
            return this;
        }

        public ExpressionLanguageConfiguration build() {
            return new ExpressionLanguageConfiguration(
                allowAutomaticIterationWhenInvokingFunctions,
                strictTypeMode
            );
        }

    }

}
