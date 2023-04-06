/*
 *  Copyright 2023 EIS Ltd and/or one of its affiliates.
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
package kraken.runtime.engine.handlers.trace;

import kraken.tracer.VoidOperation;

/**
 * Logs field value when it is being resolved for the purpose of validation in validation payload handlers
 *
 * @author Mindaugas Ulevicius
 */
public class FieldValueValidationOperation implements VoidOperation {

    private final Object fieldValue;

    public FieldValueValidationOperation(Object fieldValue) {
        this.fieldValue = fieldValue;
    }

    @Override
    public String describe() {
        return "Validating field which has value: " + FieldValueRenderer.render(fieldValue);
    }
}
