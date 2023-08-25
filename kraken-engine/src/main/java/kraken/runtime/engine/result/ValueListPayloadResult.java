/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.runtime.engine.result;

import java.util.List;

import javax.annotation.Nonnull;

import kraken.annotations.API;
import kraken.model.ValueList;
import kraken.runtime.model.rule.payload.validation.ValueListPayload;

/**
 * Result created as a result of executing {@link kraken.model.validation.ValueListPayload} in
 * {@link kraken.runtime.engine.handlers.ValueListPayloadHandler}.
 *
 * @author Tomas Dapkunas
 * @since 1.43.0
 */
@API
public class ValueListPayloadResult extends ValidationPayloadResult {

    private final ValueList valueList;

    public ValueListPayloadResult(Boolean success,
                                  ValueListPayload payload,
                                  List<Object> rawTemplateVariables) {
        super(success, payload, rawTemplateVariables);
        this.valueList = payload.getValueList();
    }

    @Nonnull
    public ValueList getValueList() {
        return valueList;
    }

}
