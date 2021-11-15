/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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

/**
 * @author mulevicius
 */
public class DSLRegExpValidationPayload extends DSLValidationPayload {

    private String regExp;

    public DSLRegExpValidationPayload(String code, String message, DSLSeverity severity, String regExp, boolean isOverridable, String overrideGroup) {
        super(code, message, severity, isOverridable, overrideGroup);
        this.regExp = Objects.requireNonNull(regExp);
    }

    public String getRegExp() {
        return regExp;
    }
}
