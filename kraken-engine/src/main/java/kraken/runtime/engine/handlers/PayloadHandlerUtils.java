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
package kraken.runtime.engine.handlers;

import java.util.Objects;

import kraken.runtime.engine.RulePayloadHandler;

/**
 * Utility methods for {@link RulePayloadHandler} implementations
 *
 * @author rimas
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess")
public class PayloadHandlerUtils {
    private PayloadHandlerUtils() {
    }

    /**
     * Determines if field value object is empty (null or empty string)
     *
     * @param value     field value object to test
     * @return          true if value is null or empty, false otherwise
     */
    public static boolean isEmptyValue(Object value) {
        return value == null || "".equals(value);
    }

    /**
     * Placeholder method for conversion of field object to string
     * Currently defers to basic toString() implementation
     *
     * @param object    field value object to convert
     * @return          converted string value.<br/>
     *                  null objects will be converted to empty string
     */
    public static String convertToString(Object object) {
        return Objects.toString(object, "");
    }
}
