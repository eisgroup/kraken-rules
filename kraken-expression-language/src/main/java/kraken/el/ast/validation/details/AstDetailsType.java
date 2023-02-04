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
package kraken.el.ast.validation.details;

/**
 * Enumerates types used to identify {@code AstDetails}.
 *
 * @author Tomas Dapkunas
 * @since 1.29.0
 */
public enum AstDetailsType {

    /**
     * Indicates a mismatch of {@code Expression} types in {@code ComparisonOperation}.
     */
    COMPARISON_TYPE_ERROR,

    /**
     * Indicates a mismatch of {@code Expression} type and function parameter type.
     */
    FUNCTION_TYPE_ERROR,

}
