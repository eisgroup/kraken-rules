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
package kraken.runtime.engine.handlers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.engine.RulePayloadHandler;
import kraken.runtime.engine.RulePayloadProcessor;
import kraken.runtime.engine.conditions.ConditionEvaluationResult;
import kraken.runtime.engine.conditions.RuleApplicabilityEvaluator;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.dto.OverridableRuleContextInfo;
import kraken.runtime.engine.dto.OverrideDependency;
import kraken.runtime.engine.dto.OverrideInfo;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.dto.RuleInfo;
import kraken.runtime.engine.evaluation.loop.RuleEvaluationInstance;
import kraken.runtime.engine.handlers.trace.RulePayloadEvaluatedOperation;
import kraken.runtime.engine.result.PayloadResult;
import kraken.runtime.engine.result.ValidationPayloadResult;
import kraken.runtime.engine.result.reducers.validation.OverrideDependencyExtractor;
import kraken.runtime.expressions.KrakenExpressionEvaluator;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.Payload;
import kraken.runtime.model.rule.payload.validation.ValidationPayload;
import kraken.tracer.Tracer;

/**
 * Default {@link RulePayloadProcessor} implementation
 *
 * @author rimas
 * @since 1.0
 */
public class RulePayloadProcessorImpl implements RulePayloadProcessor {

    private final static Logger logger = LoggerFactory.getLogger(RulePayloadProcessorImpl.class);
    private Map<PayloadType, RulePayloadHandler> payloadHandlers;
    private RuleApplicabilityEvaluator applicabilityEvaluator;
    private OverrideDependencyExtractor overrideDependencyExtractor;
    private KrakenExpressionEvaluator krakenExpressionEvaluator;

    private RulePayloadProcessorImpl(RuleApplicabilityEvaluator applicabilityEvaluator,
                                     KrakenExpressionEvaluator krakenExpressionEvaluator) {
        this.applicabilityEvaluator = applicabilityEvaluator;
        this.overrideDependencyExtractor = new OverrideDependencyExtractor(krakenExpressionEvaluator);
        this.krakenExpressionEvaluator = krakenExpressionEvaluator;
        this.payloadHandlers = new HashMap<>();
    }

    public static RulePayloadProcessorImpl create(KrakenExpressionEvaluator evaluator, RuleApplicabilityEvaluator applicabilityEvaluator) {
        RulePayloadProcessorImpl processor = new RulePayloadProcessorImpl(applicabilityEvaluator, evaluator);
        processor.addHandler(new DefaultValuePayloadHandler(evaluator));
        processor.addHandler(new UsagePayloadHandler(evaluator));
        processor.addHandler(new RegExpPayloadHandler(evaluator));
        processor.addHandler(new AssertionPayloadHandler(evaluator));
        processor.addHandler(new AccessibilityPayloadHandler());
        processor.addHandler(new VisibilityPayloadHandler());
        processor.addHandler(new LengthPayloadHandler(evaluator));
        processor.addHandler(new SizePayloadHandler(evaluator));
        processor.addHandler(new SizeRangePayloadHandler(evaluator));
        processor.addHandler(new NumberSetPayloadHandler(evaluator));
        processor.addHandler(new ValueListPayloadHandler(evaluator));
        return processor;
    }

    @Override
    public RuleEvaluationResult process(
            RuleEvaluationInstance ruleEvaluationInstance,
            EvaluationSession session
    ) {
        ConditionEvaluationResult conditionEvaluation
            = applicabilityEvaluator.evaluateCondition(ruleEvaluationInstance, session);

        RuleInfo ruleInfo = toRuleInfo(ruleEvaluationInstance);

        if (conditionEvaluation.isApplicable()) {
            PayloadResult payloadResult = evaluatePayload(session, ruleEvaluationInstance);

            return new RuleEvaluationResult(
                    ruleInfo,
                    payloadResult,
                    conditionEvaluation,
                    toOverrideInfo(ruleEvaluationInstance, payloadResult, session)
            );
        }

        logger.debug(
                "Rule '{}' is not applicable for field '{}', context instance with id:'{}'",
                ruleEvaluationInstance.getRule().getName(),
                ruleEvaluationInstance.getRule().getContext() + "." + ruleEvaluationInstance.getRule().getTargetPath(),
                ruleEvaluationInstance.getDataContext().getContextId()
        );
        return new RuleEvaluationResult(
                ruleInfo,
                null,
                conditionEvaluation,
                toOverrideInfo(ruleEvaluationInstance, null, session)
        );
    }

    private OverrideInfo toOverrideInfo(RuleEvaluationInstance ruleEvaluationInstance,
                                        PayloadResult payloadResult,
                                        EvaluationSession session) {
        if(ruleEvaluationInstance.getRule().getPayload() instanceof ValidationPayload) {
            ValidationPayload payload = (ValidationPayload) ruleEvaluationInstance.getRule().getPayload();
            boolean isOverridable = payload.isOverridable();
            String overrideGroup = payload.getOverrideGroup();

            OverridableRuleContextInfo overridableRuleContextInfo = null;
            if(isOverridable &&
                    payloadResult != null
                    && payloadResult instanceof ValidationPayloadResult
                    && BooleanUtils.isFalse(((ValidationPayloadResult) payloadResult).getSuccess())) {
                overridableRuleContextInfo = buildOverrideInfo(ruleEvaluationInstance, session.getTimestamp());
            }

            return new OverrideInfo(isOverridable, overrideGroup, overridableRuleContextInfo);
        }
        return new OverrideInfo(false, null, null);
    }

    private RuleInfo toRuleInfo(RuleEvaluationInstance ruleEvaluationInstance) {
        RuntimeRule rule = ruleEvaluationInstance.getRule();

        return new RuleInfo(
                rule.getName(),
                rule.getContext(),
                rule.getTargetPath(),
                rule.getPayload().getType()
        );
    }

    private OverridableRuleContextInfo buildOverrideInfo(RuleEvaluationInstance ruleEvaluationInstance,
                                                         LocalDateTime evaluationTimeStamp) {
        Map<String, OverrideDependency> overrideDependencies = overrideDependencyExtractor.extractOverrideDependencies(
                ruleEvaluationInstance.getRule(),
                ruleEvaluationInstance.getDataContext()
        );

        Object contextAttributeValue = krakenExpressionEvaluator.evaluateTargetField(
            ruleEvaluationInstance.getRule().getTargetPath(),
            ruleEvaluationInstance.getDataContext());

        return new OverridableRuleContextInfo(
                ruleEvaluationInstance.getNamespace(),
                ruleEvaluationInstance.getDataContext().getContextId(),
                getRootId(ruleEvaluationInstance.getDataContext()),
                ruleEvaluationInstance.getDataContext().getContextName(),
                contextAttributeValue,
                evaluationTimeStamp,
                overrideDependencies
        );
    }

    private String getRootId(DataContext dataContext) {
        if (Objects.isNull(dataContext.getParentDataContext())) {
            return dataContext.getContextId();
        } else {
            return getRootId(dataContext.getParentDataContext());
        }
    }

    private PayloadResult evaluatePayload(EvaluationSession session, RuleEvaluationInstance evaluation) {
        RulePayloadHandler handler = resolvePayloadHandler(evaluation.getRule().getPayload());
        return Tracer.doOperation(
            new RulePayloadEvaluatedOperation(evaluation.getRule().getPayload(), handler),
            () -> doEvaluatePayload(handler, session, evaluation)
        );
    }

    private PayloadResult doEvaluatePayload(RulePayloadHandler handler,
                                            EvaluationSession session, RuleEvaluationInstance evaluation) {
        logger.debug(
            "Evaluating Rule '{}' on field '{}', context instance with id:'{}'",
            evaluation.getRule().getName(),
            evaluation.getRule().getContext() + "." + evaluation.getRule().getTargetPath(),
            evaluation.getDataContext().getContextId()
        );

        return handler.executePayload(evaluation.getRule(), evaluation.getDataContext(), session);
    }

    private void addHandler(RulePayloadHandler handler) {
        PayloadType payloadType = handler.handlesPayloadType();
        logger.debug("Registered {} as handler for rule payload type {}",
                handler.getClass().getName(),
                payloadType);
        this.payloadHandlers.put(payloadType, handler);
    }

    private RulePayloadHandler resolvePayloadHandler(Payload payload) {
        return Optional.ofNullable(payloadHandlers.get(payload.getType()))
                .orElseThrow(() -> new IllegalArgumentException("Not supported payload type: " + payload.getClass()));
    }
}
