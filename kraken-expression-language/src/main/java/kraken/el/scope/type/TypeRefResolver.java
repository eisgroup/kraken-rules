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
package kraken.el.scope.type;

/**
 * SPI that must be provided if subsystem uses {@link TypeRef} when constructing {@link kraken.el.scope.Scope}.
 *
 * @author mulevicius
 */
public interface TypeRefResolver {

    /**
     * Resolves concrete type which exists in the system.
     * Used for expanding {@link TypeRef} to concrete implementation of {@link Type}
     * <p/>
     * Note, that concrete {@link Type} may not be immediately resolvable at the time when {@link TypeRef} is created.
     * Soft reference can exists before actual {@link Type} is constructed.
     * The only expectation is that {@link Type} must be resolvable <b>after</b> {@link kraken.el.scope.Scope} is created.
     *
     * @param typeName
     * @return expanded
     */
    Type resolveType(String typeName);
}
