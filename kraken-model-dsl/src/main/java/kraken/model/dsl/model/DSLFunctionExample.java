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
package kraken.model.dsl.model;

import java.util.Objects;

import javax.annotation.Nullable;

/**
 * @author mulevicius
 */
public class DSLFunctionExample {

    private final String example;
    private final String result;
    private final boolean valid;

    public DSLFunctionExample(String example, @Nullable String result, boolean valid) {
        this.example = Objects.requireNonNull(example);
        this.result = result;
        this.valid = valid;
    }

    public String getExample() {
        return example;
    }

    @Nullable
    public String getResult() {
        return result;
    }

    public boolean isValid() {
        return valid;
    }
}
