package kraken.runtime.engine.evaluation.loop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kraken.runtime.EvaluationSession;
import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.RulePayloadProcessor;
import kraken.runtime.engine.context.ContextDataProvider;
import kraken.runtime.engine.core.EntryPointEvaluation;
import kraken.runtime.engine.dto.ContextFieldInfo;
import kraken.runtime.engine.dto.FieldEvaluationResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.utils.TargetPathUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Evaluates rules in order from {@link EntryPointEvaluation#getRules()} ()}
 *
 * @author psurinin@eisgroup.com
 * @since 1.0.29
 */
public class OrderedEvaluationLoop implements EvaluationLoop {

    private final Logger logger = LoggerFactory.getLogger(OrderedEvaluationLoop.class);
    private final RulePayloadProcessor rulePayloadProcessor;

    public OrderedEvaluationLoop(RulePayloadProcessor rulePayloadProcessor) {
        this.rulePayloadProcessor = rulePayloadProcessor;
    }

    @Override
    public EntryPointResult evaluate(EntryPointEvaluation entryPointEvaluation, ContextDataProvider contextDataProvider, EvaluationSession session) {
        Map<String, FieldEvaluationResult> results = entryPointEvaluation.getRules().stream()
                .peek(rule -> logger.debug("Evaluating rule: '{}'", rule.getName()))
                // extract context definition instances
                .flatMap(rule -> contextDataProvider.resolveContextData(rule.getContext(), rule.getDependencies())
                        .stream()
                        .map(dc -> new RuleEvaluationInstance(rule, dc))
                )
                // evaluate rules
                .peek(x -> logger.debug("Evaluating rule '{}' on '{}'", x.getRule().getName(), x.getDataContext().getContextId()))
                .map(ruleEvaluation -> new RuleEvaluationResultInstance(
                                ruleEvaluation.getDataContext(),
                                rulePayloadProcessor.process(ruleEvaluation, session)
                        )
                )
                // collect to map for EntryPointResult
                .collect(Collectors.toMap(
                        RuleEvaluationResultInstance::id,
                        ruleEvaluationInstance -> new FieldEvaluationResult(
                                contextFieldInfo(ruleEvaluationInstance),
                                list(ruleEvaluationInstance.result)
                        ),
                        (fer1, fer2) -> {
                            fer1.getRuleResults().addAll(fer2.getRuleResults());
                            return fer1;
                        }
                ));
        validateDefaultsOnOneField(results);
        return new EntryPointResult(results, session.getTimestamp());
    }

    private ContextFieldInfo contextFieldInfo(RuleEvaluationResultInstance result) {
        String contextId = result.getDataContext().getContextId();
        String contextName = result.getDataContext().getContextName();
        String fieldName = result.getResult().getRuleInfo().getTargetPath();
        String fieldPath = TargetPathUtils.resolveTargetPath(fieldName, result.getDataContext());

        return new ContextFieldInfo(contextId, contextName, fieldName, fieldPath);
    }

    private void validateDefaultsOnOneField(Map<String, FieldEvaluationResult> results) {
        for (Map.Entry<String, FieldEvaluationResult> entry : results.entrySet()) {
            String key = entry.getKey();
            FieldEvaluationResult value = entry.getValue();
            List<RuleEvaluationResult> rulesOnField = value.getRuleResults()
                    .stream()
                    .filter(rr -> rr.getConditionEvaluationResult().isApplicable())
                    .filter(rr -> rr.getPayloadResult() instanceof DefaultValuePayloadResult)
                    .filter(rr -> ((DefaultValuePayloadResult) rr.getPayloadResult()).getException().isEmpty())
                    .collect(Collectors.toList());
            if (rulesOnField.size() > 1) {
                throw new KrakenRuntimeException(String.format(
                        "On field '%s' evaluated '%s' default rules: '%s'. One default rule per field can be evaluated.",
                        key,
                        rulesOnField.size(),
                        rulesOnField.stream().map(x -> x.getRuleInfo().getRuleName()).collect(Collectors.joining(", "))
                ));
            }
        }
    }

    private List<RuleEvaluationResult> list(RuleEvaluationResult result) {
        List<RuleEvaluationResult> list = new ArrayList<>();
        list.add(result);
        return list;
    }

}
