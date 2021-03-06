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

import java.util.Map;

/**
 * @author mulevicius
 */
public class InvocationContextHolder {

    private static final ThreadLocal<InvocationContext> EXPRESSION_INVOCATION_CONTEXT = new ThreadLocal<>();

    public static InvocationContext getInvocationContext() {
        return EXPRESSION_INVOCATION_CONTEXT.get();
    }

    public static void setInvocationContext(InvocationContext invocationContext) {
        EXPRESSION_INVOCATION_CONTEXT.set(invocationContext);
    }

    public static class InvocationContext {

        private Object context;

        private Map<String, Object> variables;

        public InvocationContext(Object context, Map<String, Object> variables) {
            this.context = context;
            this.variables = variables;
        }

        public Object getContext() {
            return context;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }
    }
}
