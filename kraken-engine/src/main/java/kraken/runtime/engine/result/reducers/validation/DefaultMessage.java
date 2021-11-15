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
package kraken.runtime.engine.result.reducers.validation;

import kraken.annotations.API;

import java.util.Objects;

/**
 * Represents default message that will be set in rule validation result when rule itself do not have specific message specified
 *
 * @author mulevicius
 */
@API
public class DefaultMessage {

    private String code;

    private String message;

    public DefaultMessage(String code, String message) {
        this.code = Objects.requireNonNull(code);
        this.message = Objects.requireNonNull(message);
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
