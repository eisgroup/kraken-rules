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
package kraken.model.dsl;

import java.util.List;
import java.util.stream.Collectors;

import kraken.model.Dimension;
import kraken.model.DimensionDataType;
import kraken.model.dsl.model.DSLModel;
import kraken.model.factory.RulesModelFactory;

/**
 * Converts {@code DSLDimension} elements configured in {@code DSLModel} to {@code Dimension}
 * elements.
 *
 * @author Tomas Dapkunas
 * @since 1.48.0
 */
class KrakenDslModelDimensionConverter {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    static List<Dimension> convertDimensions(DSLModel dsl) {
        return dsl.getDimensions().stream()
            .map(dslDimension -> {
                Dimension dimension = factory.createDimension();
                dimension.setDataType(DimensionDataType.getDataType(dslDimension.getType()));
                dimension.setName(dslDimension.getName());
                dimension.setPhysicalNamespace(dsl.getNamespace());

                return dimension;
            })
            .collect(Collectors.toList());
    }

}
