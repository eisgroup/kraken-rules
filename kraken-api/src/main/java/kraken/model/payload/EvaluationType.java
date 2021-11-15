/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.model.payload;

import kraken.annotations.API;

/**
 * Enumerates all kinds of evaluation results that Kraken Engine supports
 *
 * @author mulevicius
 */
@API
public enum EvaluationType {

    /**
     * A result type for failed logical assertion on the state of the entity
     */
    VALIDATION,

    /**
     * A result type for value change applied on the entity
     */
    DEFAULT,

    /**
     * A result type for indicating if the field of the entity shall be displayed or hidden for the user
     */
    VISIBILITY,

    /**
     * A result type for indicating if the field of the entity shall be editable or not for the user
     */
    ACCESSIBILITY

}
