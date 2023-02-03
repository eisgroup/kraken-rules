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

/**
 * Models a documentation for a single parameter of a particular {@link Function}.
 * <p>
 * Each parameter of a {@link Function} can only have a single {@link ParameterDocumentation} defined.
 * If more than one documentation is defined for the same parameter then error is reported during model validation.
 * <p>
 * Also, a parameter must exist in a {@link Function} for each {@link ParameterDocumentation}.
 * If documentation is defined for a parameter that does not exist in a function,
 * then error is reported during model validation.
 *
 *
 * @author mulevicius
 */
@API
public interface ParameterDocumentation {

    String getParameterName();

    void setParameterName(String parameterName);

    String getDescription();

    void setDescription(String description);
}
