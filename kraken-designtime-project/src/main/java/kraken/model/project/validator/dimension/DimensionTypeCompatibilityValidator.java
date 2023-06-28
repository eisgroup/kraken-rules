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
package kraken.model.project.validator.dimension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import kraken.model.Dimension;
import kraken.model.DimensionDataType;
import kraken.model.MetadataAware;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessage;
import kraken.utils.Dates;

/**
 * @author Tomas Dapkunas
 * @since 1.48.0
 */
public final class DimensionTypeCompatibilityValidator {

    private final KrakenProject krakenProject;

    public DimensionTypeCompatibilityValidator(KrakenProject krakenProject) {
        this.krakenProject = krakenProject;
    }

    public List<ValidationMessage> validateDimensionTypeCompatibility(MetadataAware metadataAware) {
        List<DimensionValue> dimensionValues = collectDimensionValues(metadataAware);

        return dimensionValues.stream()
            .filter(dimensionValue -> !isTypeCompatible(dimensionValue.getDimensionValue(), dimensionValue.getDimension()))
            .map(dimensionValue -> {
                String template = " has a value for dimension '%s' set to '%s', "
                    + "but such value cannot be set to this dimension. "
                    + "Expected dimension value type is '%s'.";

                String message = String.format(
                    template,
                    dimensionValue.getDimension().getName(),
                    render(dimensionValue.getDimensionValue()),
                    dimensionValue.getDimension().getDataType()
                );

                return new ValidationMessage(metadataAware, message, Severity.ERROR);
            })
            .collect(Collectors.toList());
    }

    private List<DimensionValue> collectDimensionValues(MetadataAware metadataAware) {
        return Optional.ofNullable(metadataAware.getMetadata())
            .map(metadata -> krakenProject.getDimensions().stream()
                .filter(dimension -> metadata.hasProperty(dimension.getName()))
                .map(dimension -> new DimensionValue(metadata.getProperty(dimension.getName()), dimension))
                .collect(Collectors.toList()))
            .orElse(List.of());
    }

    private boolean isTypeCompatible(Object object, Dimension dimension) {
        DimensionDataType dataType = dimension.getDataType();

        switch (dataType) {
            case DECIMAL:
            case INTEGER:
                return object instanceof Number;
            case DATE:
                return object instanceof LocalDate;
            case DATETIME:
                return object instanceof LocalDateTime;
            case STRING:
                return object instanceof String;
            case BOOLEAN:
                return object instanceof Boolean;
            default:
                throw new IllegalArgumentException("Unknown dimension data type: "
                    + dataType + " encountered for Dimension: " + dimension.getName());
        }
    }

    private String render(Object value) {
        if (value instanceof LocalDate) {
            return Dates.convertLocalDateToISO((LocalDate) value);
        } else if (value instanceof LocalDateTime) {
            return Dates.convertLocalDateTimeToISO((LocalDateTime) value);
        }
        return String.valueOf(value);
    }

    private static class DimensionValue {

        private final Object dimensionValue;
        private final Dimension dimension;

        public DimensionValue(Object dimensionValue, Dimension dimension) {
            this.dimensionValue = dimensionValue;
            this.dimension = dimension;
        }

        public Object getDimensionValue() {
            return dimensionValue;
        }

        public Dimension getDimension() {
            return dimension;
        }
    }

}
