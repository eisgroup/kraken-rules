/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.el;

import java.time.ZoneId;

import kraken.annotations.API;

/**
 * @author Mindaugas Ulevicius
 */
@API
public class FunctionContextHolder {

    private static final ThreadLocal<FunctionContext> FUNCTION_CONTEXT = new ThreadLocal<>();

    /**
     *
     * @return A current evaluation session specific metadata
     */
    public static FunctionContext getFunctionContext() {
        return FUNCTION_CONTEXT.get();
    }

    public static void setFunctionContext(FunctionContext functionContext) {
        FUNCTION_CONTEXT.set(functionContext);
    }

    @API
    public static class FunctionContext {

        private final ZoneId zoneId;

        public FunctionContext(ZoneId zoneId) {
            this.zoneId = zoneId;
        }

        /**
         *
         * @return timezone of a current evaluation session
         */
        public ZoneId getZoneId() {
            return zoneId;
        }
    }
}
