/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.generator.function;

import java.util.List;

/**
 * @author mulevicius
 */
public class KelFunction {
    private final String name;
    private final List<FunctionParameter> parameters;
    private final String body;

    public KelFunction(String name, List<FunctionParameter> parameters, String body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<FunctionParameter> getParameters() {
        return parameters;
    }

    public String getBody() {
        return body;
    }
}
