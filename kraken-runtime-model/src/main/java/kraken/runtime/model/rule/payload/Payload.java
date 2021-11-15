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
package kraken.runtime.model.rule.payload;

import kraken.model.payload.PayloadType;

/**
 * Payload defines logic for "action" part of the rule - which will be executed
 * if rules is activated and condition is evaluated to true
 *
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public interface Payload {

    /**
     * @return  payload type
     */
    PayloadType getType();
}
