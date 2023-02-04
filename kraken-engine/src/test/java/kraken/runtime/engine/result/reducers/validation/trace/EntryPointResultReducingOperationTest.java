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
package kraken.runtime.engine.result.reducers.validation.trace;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import kraken.model.validation.ValidationSeverity;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.dto.ContextFieldInfo;
import kraken.runtime.engine.result.reducers.validation.ValidationResult;
import kraken.runtime.engine.result.reducers.validation.ValidationStatus;

/**
 * Unit tests for {@code EntryPointResultReducingOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
@RunWith(MockitoJUnitRunner.class)
public class EntryPointResultReducingOperationTest {

    @Mock
    private EntryPointResult entryPointResult;

    @Mock
    private ValidationStatus validationStatus;

    @Mock
    private ValidationResult info;

    @Mock
    private ValidationResult error;

    @Mock
    private ValidationResult warning;

    @Mock
    private ContextFieldInfo contextFieldInfo;

    @Test
    public void shouldCreateCorrectDescriptionForEntryPointReduction() {
        var dateTime = LocalDateTime.now();
        when(entryPointResult.getEvaluationTimeStamp()).thenReturn(dateTime);

        var epReductionOp = new EntryPointResultReducingOperation(entryPointResult);
        assertThat(epReductionOp.describe(),
            is("Reducing results of entry point evaluated at " + dateTime.toString() + "."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForReducedFieldResult() {
        when(validationStatus.getErrorResults()).thenReturn(List.of(error));
        when(validationStatus.getWarningResults()).thenReturn(List.of(warning));
        when(validationStatus.getInfoResults()).thenReturn(List.of(info));

        when(info.getContextFieldInfo()).thenReturn(contextFieldInfo);
        when(error.getContextFieldInfo()).thenReturn(contextFieldInfo);
        when(warning.getContextFieldInfo()).thenReturn(contextFieldInfo);

        when(contextFieldInfo.toString()).thenReturn("ctx:id:field");
        when(error.getSeverity()).thenReturn(ValidationSeverity.critical);
        when(error.getRuleName()).thenReturn("error rule");

        when(info.getSeverity()).thenReturn(ValidationSeverity.info);
        when(info.getRuleName()).thenReturn("info rule");

        when(warning.getSeverity()).thenReturn(ValidationSeverity.warning);
        when(warning.getRuleName()).thenReturn("warning rule");

        var epReductionOp = new EntryPointResultReducingOperation(entryPointResult);

        assertThat(epReductionOp.describeAfter(validationStatus),
            is("Reduced entry point results: "
                + System.lineSeparator()
                + "Field ctx:id:field has: "
                + System.lineSeparator()
                + "Rule 'error rule' with severity - error"
                + System.lineSeparator()
                + "Rule 'warning rule' with severity - warning"
                + System.lineSeparator()
                + "Rule 'info rule' with severity - info"));
    }

}
