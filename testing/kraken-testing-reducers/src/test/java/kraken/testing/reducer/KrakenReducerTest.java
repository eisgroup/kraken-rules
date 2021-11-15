/*
 *  Copyright 2019 EIS Ltd and/or one of its affiliates.
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

package kraken.testing.reducer;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import kraken.model.payload.PayloadType;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.conditions.ConditionEvaluationResult;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.dto.ContextFieldInfo;
import kraken.runtime.engine.dto.FieldEvaluationResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.dto.RuleInfo;
import kraken.runtime.engine.events.ValueChangedEvent;
import kraken.runtime.engine.result.AccessibilityPayloadResult;
import kraken.runtime.engine.result.AssertionPayloadResult;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.engine.result.LengthPayloadResult;
import kraken.runtime.engine.result.VisibilityPayloadResult;
import kraken.runtime.model.rule.payload.validation.AssertionPayload;
import kraken.runtime.model.rule.payload.validation.ErrorMessage;
import kraken.runtime.model.rule.payload.validation.LengthPayload;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class KrakenReducerTest {

    private static final String TARGET_PATH = "field";

    @Test
    public void shouldReturnResultForIgnored() {
        final EntryPointResult entryPointResult = new EntryPointResult(Map.of(
                "context:id:field",
                new FieldEvaluationResult(
                        contextFieldInfo(),
                        List.of(
                                new RuleEvaluationResult(
                                        rule("visibility", PayloadType.VISIBILITY),
                                        null,
                                        new ConditionEvaluationResult(new RuntimeException()),
                                        null
                                )
                        )
                )),
                LocalDateTime.now()
        );
        final DataContext dataContext = getDataContext();
        final Collection<VisibilityMetadata> metadata = KrakenReducers.VISIBILITY.reduce(entryPointResult);
        assertThat(metadata, hasSize(1));
        assertThat(metadata.iterator().next().getVisible(), is(nullValue()));
        assertThat(metadata.iterator().next().getResultMetadata().getContextName(), is(dataContext.getContextName()));
        assertThat(metadata.iterator().next().getResultMetadata().getId(), is(dataContext.getContextId()));
        assertThat(metadata.iterator().next().getResultMetadata().getRuleName(), is("visibility"));
        assertThat(metadata.iterator().next().getResultMetadata().getConditionEvaluation(), is("ignored"));
        assertThat(metadata.iterator().next().getResultMetadata().getAttribute(), is(TARGET_PATH));
    }

    @Test
    public void shouldReturnHiddenResult() {
        final EntryPointResult entryPointResult = new EntryPointResult(Map.of(
                "context:id:field",
                new FieldEvaluationResult(
                        contextFieldInfo(),
                        List.of(
                                new RuleEvaluationResult(
                                        rule("visibility", PayloadType.VISIBILITY),
                                        new VisibilityPayloadResult(false),
                                        ConditionEvaluationResult.APPLICABLE,
                                        null
                                )
                        )
                )),
                LocalDateTime.now()
        );
        final DataContext dataContext = getDataContext();
        final Collection<VisibilityMetadata> metadata = KrakenReducers.VISIBILITY.reduce(entryPointResult);
        assertThat(metadata, hasSize(1));
        assertThat(metadata.iterator().next().getVisible(), is(false));
        assertThat(metadata.iterator().next().getResultMetadata().getContextName(), is(dataContext.getContextName()));
        assertThat(metadata.iterator().next().getResultMetadata().getId(), is(dataContext.getContextId()));
        assertThat(metadata.iterator().next().getResultMetadata().getRuleName(), is("visibility"));
    }

    @Test
    public void shouldReturnNotAccessibleResult() {
        final EntryPointResult entryPointResult = new EntryPointResult(Map.of(
                "context:id:field",
                new FieldEvaluationResult(
                        contextFieldInfo(),
                        List.of(
                                new RuleEvaluationResult(
                                        rule("accessibility", PayloadType.ACCESSIBILITY),
                                        new AccessibilityPayloadResult(false),
                                        ConditionEvaluationResult.APPLICABLE,
                                        null
                                )
                        )
                )),
                LocalDateTime.now()
        );
        final Collection<AccessibilityMetadata> metadata = KrakenReducers.ACCESSIBILITY.reduce(entryPointResult);
        assertThat(metadata, hasSize(1));
        assertThat(metadata.iterator().next().getAccessible(), is(false));
        assertThat(metadata.iterator().next().getResultMetadata(), is(notNullValue()));
    }

    @Test
    public void shouldReturnNotValidResult() {
        LengthPayload lengthPayload = mock(LengthPayload.class);
        when(lengthPayload.getErrorMessage()).thenReturn(new ErrorMessage("CODE", List.of("message"), List.of()));
        when(lengthPayload.getSeverity()).thenReturn(ValidationSeverity.critical);
        LengthPayloadResult payloadResult = new LengthPayloadResult(false, lengthPayload, List.of());

        AssertionPayload assertionPayload = mock(AssertionPayload.class);
        when(assertionPayload.getErrorMessage()).thenReturn(new ErrorMessage("CODE", List.of("message"), List.of()));
        when(assertionPayload.getSeverity()).thenReturn(ValidationSeverity.critical);
        AssertionPayloadResult assertionPayloadResult = new AssertionPayloadResult(new RuntimeException(), assertionPayload);

        EntryPointResult entryPointResult = new EntryPointResult(Map.of(
                "context:id:field",
                new FieldEvaluationResult(
                        contextFieldInfo(),
                        List.of(
                                new RuleEvaluationResult(
                                        rule("length", PayloadType.LENGTH),
                                        payloadResult,
                                        ConditionEvaluationResult.APPLICABLE,
                                        null
                                ),
                                new RuleEvaluationResult(
                                        rule("assert", PayloadType.ASSERTION),
                                        assertionPayloadResult,
                                        ConditionEvaluationResult.APPLICABLE,
                                        null
                                ),
                                new RuleEvaluationResult(
                                        rule("not applicable", PayloadType.ASSERTION),
                                        assertionPayloadResult,
                                        ConditionEvaluationResult.NOT_APPLICABLE,
                                        null
                                )
                        )
                )),
                LocalDateTime.now()
        );
        final Collection<ValidationMetadata> metadata = KrakenReducers.VALIDATION.reduce(entryPointResult);
        assertThat(
                KrakenReducers.forOneRule(metadata, "assert").iterator().next().getEvaluateWithError(),
                is(true)
        );
        assertThat(
                KrakenReducers.forOneRule(metadata, "not applicable").iterator().next().getResultMetadata().getConditionEvaluation(),
                is(KrakenReducers.ConditionResult.NOT_APPLICABLE)
        );
        assertThat(
                KrakenReducers.forOneRule(metadata, "length").iterator().next().getSuccess(),
                is(false)
        );
        assertThat(
                KrakenReducers.forOneRule(metadata, "length").iterator().next().getMessage(),
                is("message")
        );
        assertThat(
                KrakenReducers.forOneRule(metadata, "length").iterator().next().getMessageCode(),
                is("CODE")
        );
    }

    @Test
    public void shouldReturnDefaultResult() {

        final DefaultValuePayloadResult payloadResult = new DefaultValuePayloadResult(
                List.of(new ValueChangedEvent(getDataContext(), "txType", 1,2))
        );
        final EntryPointResult entryPointResult = new EntryPointResult(Map.of(
                "context:id:field",
                new FieldEvaluationResult(
                        contextFieldInfo(),
                        List.of(
                                new RuleEvaluationResult(
                                        rule("length", PayloadType.DEFAULT),
                                        payloadResult,
                                        ConditionEvaluationResult.APPLICABLE,
                                        null
                                )
                        )
                )),
                LocalDateTime.now()
        );
        final Collection<DefaultValueMetadata> metadata = KrakenReducers.DEFAULT.reduce(entryPointResult);
        assertThat(metadata, hasSize(1));
        assertThat(metadata.iterator().next().isEvaluatedWithError(), is(false));
        assertThat(metadata.iterator().next().getEvents().get(0).getPreviousValue(), is(1));
        assertThat(metadata.iterator().next().getEvents().get(0).getNewValue(), is(2));
    }

    @Test
    public void shouldReturnDefaultResultWithError() {
        final DefaultValuePayloadResult payloadResult = new DefaultValuePayloadResult(new RuntimeException());
        final EntryPointResult entryPointResult = new EntryPointResult(Map.of(
                "context:id:field",
                new FieldEvaluationResult(
                        contextFieldInfo(),
                        List.of(
                                new RuleEvaluationResult(
                                        rule("length", PayloadType.DEFAULT),
                                        payloadResult,
                                        ConditionEvaluationResult.APPLICABLE,
                                        null
                                )
                        )
                )),
                LocalDateTime.now()
        );
        final Collection<DefaultValueMetadata> metadata = KrakenReducers.DEFAULT.reduce(entryPointResult);
        assertThat(metadata, hasSize(1));
        assertThat(metadata.iterator().next().isEvaluatedWithError(), is(true));
    }

    @Test
    public void shouldReturnResultForOneRule() {
        final DefaultValuePayloadResult payloadResult = new DefaultValuePayloadResult(
                List.of(new ValueChangedEvent(getDataContext(), "txType", 1,2))
        );
        final EntryPointResult entryPointResult = new EntryPointResult(Map.of(
                "context:id:field",
                new FieldEvaluationResult(
                        contextFieldInfo(),
                        List.of(
                                new RuleEvaluationResult(
                                        rule("rule2", PayloadType.DEFAULT),
                                        payloadResult,
                                        ConditionEvaluationResult.APPLICABLE,
                                        null
                                ),
                                new RuleEvaluationResult(
                                        rule("rule1", PayloadType.DEFAULT),
                                        payloadResult,
                                        ConditionEvaluationResult.APPLICABLE,
                                        null
                                )
                        )
                )),
                LocalDateTime.now()
        );
        final Collection<DefaultValueMetadata> metadata = KrakenReducers.DEFAULT.reduce(entryPointResult);

        final Collection<DefaultValueMetadata> oneRule = KrakenReducers.forOneRule(metadata, "rule2");
        assertThat(oneRule, hasSize(1));
    }

    private DataContext getDataContext() {
        final DataContext dataContext = new DataContext();
        dataContext.setContextId("1");
        dataContext.setContextName("Policy");
        return dataContext;
    }

    private ContextFieldInfo contextFieldInfo() {
        return new ContextFieldInfo("1", "Policy", TARGET_PATH, TARGET_PATH);
    }

    private RuleInfo rule(String name, PayloadType payloadType) {
        return new RuleInfo(name, "Context", TARGET_PATH, payloadType);
    }
}
