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

/**
 * Get property by provided path as string separated by dot.
 * If path does not exist or property does not exist by that path,
 * then undefined will be returned
 * @param {object} object to access property
 * @param {string} path
 * @return property value by provided path or {@code undefined}
 */
function access(object: Record<string, any>, path: string): any {
    let currentObject = object;
    const pathArray = path.split(".");
    let pathIndex = 0;
    let canGoDeep = true;
    while (canGoDeep) {
        currentObject = currentObject[pathArray[pathIndex]];
        pathIndex++;
        if (pathIndex === pathArray.length || currentObject === undefined) {
            canGoDeep = false;
        }
    }
    return currentObject;
}
/**
 * Set property by provided path as string separated by dot.
 * If path does not exist or property does not exist by that path,
 * then undefined will be returned.
 * @param {object} object to access property
 * @param {string} path
 * @return new property value by provided path or {@code undefined}
 */
function accessAndSet(object: Record<string, any>, path: string, value: any): any {
    let currentObject = object;
    const pathArray = path.split(".");
    let pathIndex = 0;
    const canGoDeep = true;
    while (canGoDeep) {
        if (pathIndex === pathArray.length - 1) {
            currentObject[pathArray[pathIndex]] = value;
            return value;
        }
        currentObject = currentObject[pathArray[pathIndex]];
        pathIndex++;
        if (currentObject === undefined) {
            return undefined;
        }
    }
    return currentObject;
}

/**
 * Object with accessor function, that can access and set values by path
 * separated by dot.
 */
export const pathAccessor = {
    access, accessAndSet
};
