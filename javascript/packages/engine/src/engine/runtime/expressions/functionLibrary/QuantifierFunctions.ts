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

export const quantifierFunctions = {
    Any,
    All,
}

/**
 * @param array
 * @return true if collection is not empty and at least one item is true
 */
function Any(array?: unknown[] | null): boolean {
    return (array || []).some(Boolean)
}

/**
 * @param array
 * @return true if collection is empty or every item is true
 */
function All(array?: unknown[] | null): boolean {
    return (array || []).every(Boolean)
}
