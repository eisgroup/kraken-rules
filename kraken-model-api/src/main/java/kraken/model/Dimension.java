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
package kraken.model;

/**
 * Models a definition of a dimension used to variate Rules, Entry Points, etc. Dimension
 * definition consists of dimension name and data type.
 * <p>
 * A data type modeled for dimension must be a primitive data type compatible with one of
 * Kraken field primitive data type.
 *
 * @author Tomas Dapkunas
 * @since 1.48.0
 * @see kraken.model.context.PrimitiveFieldDataType
 */
public interface Dimension extends KrakenModelItem {

    /**
     * Returns a data type supported by {@code this} Dimension.
     */
    DimensionDataType getDataType();

    void setDataType(DimensionDataType dataType);

}
