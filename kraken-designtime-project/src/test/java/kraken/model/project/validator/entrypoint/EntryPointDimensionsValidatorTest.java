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

import static kraken.model.project.KrakenProjectMocks.krakenProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import kraken.model.Dimension;
import kraken.model.DimensionDataType;
import kraken.model.Metadata;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationSession;

/**
 * @author Tomas Dapkunas
 * @since 1.48.0
 */
public class EntryPointDimensionsValidatorTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();

    @Test
    public void shouldPassValidationsForValidEntryPoint() {
        Metadata metadata = createMetadata(Map.of("stringDimension", "string value", "numberDimension", 100));
        Dimension stringDimension = createDimension("stringDimension", DimensionDataType.STRING);
        Dimension numberDimension = createDimension("numberDimension", DimensionDataType.INTEGER);

        EntryPoint entryPoint = createEntryPoint("dimensionEp", metadata);

        KrakenProject krakenProject = createProject(
            List.of(entryPoint),
            List.of(stringDimension, numberDimension)
        );

        ValidationSession validationSession = doValidate(entryPoint, krakenProject);

        assertFalse(validationSession.hasEntryPointError());
        assertThat(validationSession.getValidationMessages(), hasSize(0));
    }

    @Test
    public void shouldPassValidationsForEntryPointWithNoDimensions() {
        Dimension stringDimension = createDimension("stringDimension", DimensionDataType.STRING);

        EntryPoint entryPoint = createEntryPoint("dimensionEp", null);

        KrakenProject krakenProject = createProject(
            List.of(entryPoint),
            List.of(stringDimension, stringDimension)
        );

        ValidationSession validationSession = doValidate(entryPoint, krakenProject);

        assertFalse(validationSession.hasEntryPointError());
        assertThat(validationSession.getValidationMessages(), hasSize(0));
    }

    @Test
    public void shouldValidateIncompatibleTypesString() {
        Metadata metadata = createMetadata(Map.of("stringDimension", LocalDate.of(2000, 1, 1)));
        Dimension dimension = createDimension("stringDimension", DimensionDataType.STRING);

        EntryPoint entryPoint = createEntryPoint("dimensionEp", metadata);

        KrakenProject krakenProject = createProject(
            List.of(entryPoint),
            List.of(dimension)
        );

        ValidationSession validationSession = doValidate(entryPoint, krakenProject);

        assertTrue(validationSession.hasEntryPointError());
        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.ERROR));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(), containsString(
            "Dimension 'stringDimension' value is '2000-01-01', "
                + "but such value cannot be set to this dimension. "
                + "Expected dimension value type is 'String'."
        ));
    }

    @Test
    public void shouldValidateIncompatibleTypesDateTime() {
        Metadata metadata = createMetadata(Map.of("dateTimeDimension", LocalDate.of(2000, 1, 1)));
        Dimension dimension = createDimension("dateTimeDimension", DimensionDataType.DATETIME);

        EntryPoint entryPoint = createEntryPoint("dimensionEp", metadata);

        KrakenProject krakenProject = createProject(
            List.of(entryPoint),
            List.of(dimension)
        );

        ValidationSession validationSession = doValidate(entryPoint, krakenProject);

        assertTrue(validationSession.hasEntryPointError());
        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.ERROR));
        assertThat(validationSession.getValidationMessages().get(0).getMessage(), containsString(
            "Dimension 'dateTimeDimension' value is '2000-01-01', "
                + "but such value cannot be set to this dimension. "
                + "Expected dimension value type is 'Datetime'."
        ));
    }

    private ValidationSession doValidate(EntryPoint entryPoint, KrakenProject krakenProject) {
        EntryPointDimensionsValidator entryPointDimensionsValidator
            = new EntryPointDimensionsValidator(krakenProject);
        ValidationSession validationSession = new ValidationSession();

        entryPointDimensionsValidator.validate(entryPoint, validationSession);

        return validationSession;
    }


    private Metadata createMetadata(Map<String, Object> properties) {
        Metadata metadata = factory.createMetadata();
        properties.forEach(metadata::setProperty);

        return metadata;
    }

    private EntryPoint createEntryPoint(String name, Metadata metadata) {
        EntryPoint entryPoint = factory.createEntryPoint();
        entryPoint.setName(name);
        entryPoint.setMetadata(metadata);

        return entryPoint;
    }

    private Dimension createDimension(String name, DimensionDataType dataType) {
        Dimension dimension = factory.createDimension();
        dimension.setName(name);
        dimension.setDataType(dataType);

        return dimension;
    }

    private KrakenProject createProject(List<EntryPoint> entryPointList, List<Dimension> dimensions) {
        return krakenProject(
            List.of(),
            null,
            List.of(),
            entryPointList,
            List.of(),
            List.of(),
            List.of(),
            dimensions
        );
    }

}
