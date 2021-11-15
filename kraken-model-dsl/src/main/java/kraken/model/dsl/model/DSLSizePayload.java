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
package kraken.model.dsl.model;

/**
 * @author psurinin
 */
public class DSLSizePayload extends DSLValidationPayload {

    private int size;
    private DSLSizeOrientation orientation;

    public DSLSizePayload(
            String code,
            String message,
            DSLSeverity severity,
            boolean isOverridable,
            String overrideGroup,
            int size, DSLSizeOrientation orientation) {
        super(code, message, severity, isOverridable, overrideGroup);
        this.size = size;
        this.orientation = orientation;
    }

    public int getSize() {
        return size;
    }

    public DSLSizeOrientation getOrientation() {
        return orientation;
    }
}
