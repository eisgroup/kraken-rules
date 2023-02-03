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
package kraken.model;

import kraken.annotations.API;
import kraken.el.functionregistry.ParameterType;
import kraken.el.functionregistry.ReturnType;

/**
 * Models generic a single {@link #getBound()} of a {@link #getGeneric()}
 * as used by function parameter types or function return type in {@link Function}.
 * <p>
 * Generic is name of the generic type without generic token separators.
 * For example, if generic type is {@code &lt;T&gt;} then generic name is {@code T}.
 * <p>
 * Bound is a type which specifies upper bound of a generic type.
 * Bound is optional.
 * Bound itself cannot be generic type.
 * Type must follow the same rules as in {@link ReturnType} and {@link ParameterType}
 *
 * @author mulevicius
 */
@API
public interface GenericTypeBound {

    String getGeneric();

    void setGeneric(String generic);

    String getBound();

    void setBound(String bound);
}
