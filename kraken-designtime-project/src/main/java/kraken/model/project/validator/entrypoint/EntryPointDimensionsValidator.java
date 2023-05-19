/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.model.project.validator.entrypoint;

import kraken.model.entrypoint.EntryPoint;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.ValidationSession;
import kraken.model.project.validator.dimension.DimensionTypeCompatibilityValidator;

/**
 * @author Tomas Dapkunas
 * @since 1.48.0
 */
public final class EntryPointDimensionsValidator {

    private final DimensionTypeCompatibilityValidator dimensionTypeCompatibilityValidator;

    public EntryPointDimensionsValidator(KrakenProject krakenProject) {
        this.dimensionTypeCompatibilityValidator = new DimensionTypeCompatibilityValidator(krakenProject);
    }

    public void validate(EntryPoint entryPoint, ValidationSession session) {
        session.addAll(dimensionTypeCompatibilityValidator.validateDimensionTypeCompatibility(entryPoint));
    }

}
