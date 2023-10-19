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
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import kraken.model.Metadata;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;
import kraken.model.project.KrakenProject;
import kraken.model.project.validator.Severity;
import kraken.model.project.validator.ValidationMessageBuilder.Message;
import kraken.model.project.validator.ValidationSession;

/**
 * @author Tomas Dapkunas
 * @since 1.54.0
 */
public class EntryPointVersionDuplicationValidatorTest {

    private static final RulesModelFactory FACTORY = RulesModelFactory.getInstance();

    @Test
    public void shouldNotAddWarningForEntryPointWithSameNameButDifferentDimensionValue() {
        EntryPoint firstEntryPointVersion = createEntryPoint("versioned ep", "firstId", createMetadata(
            DimensionEntry.of("first", LocalDate.now()))
        );
        EntryPoint secondEntryPointVersion = createEntryPoint("versioned ep", "secondId", createMetadata(
            DimensionEntry.of("first", LocalDate.now().plusDays(4)))
        );

        KrakenProject krakenProject = createProject(firstEntryPointVersion, secondEntryPointVersion);

        ValidationSession validationSession = doValidate(firstEntryPointVersion, krakenProject);

        assertTrue(validationSession.getValidationMessages().isEmpty());
    }

    @Test
    public void shouldNotAddWarningForEntryPointWithDifferentNameAndSameDimensionValue() {
        EntryPoint firstEntryPoint = createEntryPoint("an entry point", "firstId", createMetadata(
            DimensionEntry.of("first", "firstValue"))
        );
        EntryPoint secondEntryPoint = createEntryPoint("another entry point", "secondId", createMetadata(
            DimensionEntry.of("first", "firstValue"))
        );

        KrakenProject krakenProject = createProject(firstEntryPoint, secondEntryPoint);

        ValidationSession validationSession = doValidate(firstEntryPoint, krakenProject);

        assertTrue(validationSession.getValidationMessages().isEmpty());
    }

    @Test
    public void shouldNotAddWarningForEntryPointWithSameNameButDifferentDimensions() {
        EntryPoint firstEntryPointVersion = createEntryPoint("versioned ep", "firstId", createMetadata(
            DimensionEntry.of("first", "firstValue"))
        );
        EntryPoint secondEntryPointVersion = createEntryPoint("versioned ep", "secondId", null);

        KrakenProject krakenProject = createProject(firstEntryPointVersion, secondEntryPointVersion);

        ValidationSession validationSession = doValidate(firstEntryPointVersion, krakenProject);

        assertTrue(validationSession.getValidationMessages().isEmpty());
    }

    @Test
    public void shouldAddWarningForEntryPointWithSameNameAndDimensions() {
        EntryPoint firstEntryPointVersion = createEntryPoint("versioned ep", "firstId", createMetadata(
                DimensionEntry.of("boolean", false),
                DimensionEntry.of("integer", 25)
            )
        );
        EntryPoint secondEntryPointVersion = createEntryPoint("versioned ep", "secondId", createMetadata(
                DimensionEntry.of("boolean", false),
                DimensionEntry.of("integer", 25)
            )
        );

        KrakenProject krakenProject = createProject(firstEntryPointVersion, secondEntryPointVersion);

        ValidationSession validationSession = doValidate(firstEntryPointVersion, krakenProject);

        assertThat(validationSession.getValidationMessages(), hasSize(1));
        assertThat(validationSession.getValidationMessages().get(0).getSeverity(), is(Severity.WARNING));
        assertThat(validationSession.getValidationMessages().get(0).getCode(), is(Message.DUPLICATE_ENTRYPOINT_VERSION.getCode()));
    }

    private ValidationSession doValidate(EntryPoint entryPoint, KrakenProject krakenProject) {
        EntryPointVersionDuplicationValidator entryPointDuplicationValidator = new EntryPointVersionDuplicationValidator(
            krakenProject);
        ValidationSession validationSession = new ValidationSession();

        entryPointDuplicationValidator.validate(entryPoint, validationSession);

        return validationSession;
    }

    private EntryPoint createEntryPoint(String name, String variationId, Metadata metadata) {
        EntryPoint entryPoint = FACTORY.createEntryPoint();
        entryPoint.setName(name);
        entryPoint.setMetadata(metadata);
        entryPoint.setEntryPointVariationId(variationId);

        return entryPoint;
    }

    private Metadata createMetadata(DimensionEntry... dimensions) {
        Metadata metadata = FACTORY.createMetadata();

        for (DimensionEntry dimension : dimensions) {
            metadata.setProperty(dimension.getName(), dimension.getValue());
        }

        return metadata;
    }

    private KrakenProject createProject(EntryPoint... allEntryPointVersions) {
        return krakenProject(List.of(), Arrays.asList(allEntryPointVersions), List.of());
    }

    private static class DimensionEntry {

        private final String name;
        private final Object value;

        private DimensionEntry(String name, Object value) {
            this.name = name;
            this.value = value;
        }

        public static DimensionEntry of(String name, Object value) {
            return new DimensionEntry(name, value);
        }

        public String getName() {
            return name;
        }

        public Object getValue() {
            return value;
        }
    }

}
