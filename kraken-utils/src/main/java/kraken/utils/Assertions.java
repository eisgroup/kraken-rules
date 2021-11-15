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
package kraken.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Utility class for parameter value checks
 *
 * @author rimas
 * @since 1.0
 */
public class Assertions {
    private Assertions() {
    }

    public static void assertNotNull(Object value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " cannot be null");
        }
    }

    public static void assertNotEmpty(String value, String name) {
        assertNotNull(value, name);
        if (value.length() == 0) {
            throw new IllegalArgumentException(name + " cannot be empty string");
        }
    }

    public static void assertNotEmpty(Collection value, String name) {
        assertNotNull(value, name);
        if (value.isEmpty()) {
            throw new IllegalArgumentException(name + " cannot be empty collection");
        }
    }

    public static void assertNotEmpty(Map value, String name) {
        assertNotNull(value, name);
        if (value.size() == 0) {
            throw new IllegalArgumentException(name + " cannot be empty map");
        }
    }
}
