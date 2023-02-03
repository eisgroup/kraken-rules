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
package kraken.el.functionregistry;

/**
 * Information about generics used in imported Java custom function
 *
 * @author mulevicius
 */
public class GenericTypeInfo {

    private final String generic;
    private final String bound;

    public GenericTypeInfo(String generic, String bound) {
        this.generic = generic;
        this.bound = bound;
    }

    public String getGeneric() {
        return generic;
    }

    public String getBound() {
        return bound;
    }
}
