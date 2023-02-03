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
package kraken.runtime.engine.evaluation.loop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import kraken.model.payload.PayloadType;
import kraken.runtime.EvaluationSession;
import kraken.runtime.KrakenRuntimeException;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.RulePayloadProcessor;
import kraken.runtime.engine.context.ContextDataProvider;
import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.core.EntryPointEvaluation;
import kraken.runtime.engine.dto.ContextFieldInfo;
import kraken.runtime.engine.dto.FieldEvaluationResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.dto.RuleEvaluationStatus;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.utils.TargetPathUtils;
import kraken.tracer.Operation;
import kraken.tracer.Tracer;
import kraken.tracer.VoidOperation;

/**
 * @author mulevicius
 * @since 1.40.0
 */
public class OrderedEvaluationLoop implements EvaluationLoop {

    private final RulePayloadProcessor rulePayloadProcessor;

    public OrderedEvaluationLoop(RulePayloadProcessor rulePayloadProcessor) {
        this.rulePayloadProcessor = rulePayloadProcessor;
    }

    @Override
    public EntryPointResult evaluate(EntryPointEvaluation entryPointEvaluation,
                                     ContextDataProvider contextDataProvider,
                                     EvaluationSession session) {
        var defaultResults = evaluateDefaultRules(entryPointEvaluation, contextDataProvider, session);
        var otherResults = evaluateRules(entryPointEvaluation, contextDataProvider, session);

        var results = new HashMap<String, FieldEvaluationResult>();
        defaultResults.forEach(result -> addResult(result, results));
        otherResults.forEach(result -> addResult(result, results));

        validateDefaultsOnOneField(results);
        return new EntryPointResult(results, session.getTimestamp());
    }

    private List<RuleOnInstanceEvaluationResult> evaluateRules(EntryPointEvaluation entryPointEvaluation,
                                                               ContextDataProvider contextDataProvider,
                                                               EvaluationSession session) {
        var rules = entryPointEvaluation.getRules().stream()
            .filter(r -> r.getPayload().getType() != PayloadType.DEFAULT)
            .collect(Collectors.toList());

        return doEvaluateRules(rules, contextDataProvider, session);
    }

    private List<RuleOnInstanceEvaluationResult> doEvaluateRules(List<RuntimeRule> rules,
                                                                 ContextDataProvider contextDataProvider,
                                                                 EvaluationSession session) {
        List<RuleOnInstanceEvaluationResult> allResults = new ArrayList<>();
        for(var rule : rules) {
            var results = evaluateRule(rule, contextDataProvider, session);
            allResults.addAll(results);
        }
        return allResults;
    }

    private List<RuleOnInstanceEvaluationResult> evaluateRule(RuntimeRule rule,
                                                              ContextDataProvider contextDataProvider,
                                                              EvaluationSession session) {
        return Tracer.doOperation(
            new RuleEvaluationOperation(rule),
            () -> doEvaluateRule(rule, contextDataProvider, session)
        );
    }

    private List<RuleOnInstanceEvaluationResult> doEvaluateRule(RuntimeRule rule,
                                                                ContextDataProvider contextDataProvider,
                                                                EvaluationSession session) {
        List<RuleOnInstanceEvaluationResult> allResults = new ArrayList<>();
        for(var context : resolveContexts(rule, contextDataProvider)) {
            var evaluation = new RuleEvaluationInstance(session.getNamespace(), rule, context);
            var result = evaluateRulePayload(evaluation, session, false);
            allResults.add(result);
        }
        return allResults;
    }

    private List<RuleOnInstanceEvaluationResult> evaluateDefaultRules(EntryPointEvaluation entryPointEvaluation,
                                                                      ContextDataProvider contextDataProvider,
                                                                      EvaluationSession session) {
        var fieldOrder = entryPointEvaluation.getFieldOrder();
        var defaultRules = entryPointEvaluation.getRules().stream()
            .filter(r -> r.getPayload().getType() == PayloadType.DEFAULT)
            .collect(Collectors.toList());

        if(!defaultRules.isEmpty()) {
            return Tracer.doOperation(
                new DefaultRulesEvaluationOperation(defaultRules),
                () -> doEvaluateDefaultRules(defaultRules, fieldOrder, contextDataProvider, session)
            );
        }
        return List.of();
    }

    private List<RuleOnInstanceEvaluationResult> doEvaluateDefaultRules(List<RuntimeRule> defaultRules,
                                                                        List<String> fieldOrder,
                                                                        ContextDataProvider contextDataProvider,
                                                                        EvaluationSession session) {
        var defaultRuleEvaluations = buildDefaultRuleEvaluations(defaultRules, contextDataProvider, session);

        List<RuleOnInstanceEvaluationResult> allResults = new ArrayList<>();
        for(var field : fieldOrder) {
            if(defaultRuleEvaluations.containsKey(field)) {
                for(var evaluations : defaultRuleEvaluations.get(field).getEvaluations().values()) {
                    var priorityOrderedEvaluations = new ArrayList<>(evaluations);
                    priorityOrderedEvaluations.sort(
                        Comparator.comparingInt(RuleEvaluationInstance::getPriority).reversed()
                    );
                    var results = evaluateDefaultRulesInPriorityOrder(priorityOrderedEvaluations, session);
                    allResults.addAll(results);
                }
            }
        }
        return allResults;
    }

    private HashMap<String, FieldEvaluation> buildDefaultRuleEvaluations(List<RuntimeRule> defaultRules,
                                                                         ContextDataProvider contextDataProvider,
                                                                         EvaluationSession session) {
        var defaultRuleEvaluations = new HashMap<String, FieldEvaluation>();
        for(var rule : defaultRules) {
            for(var context : resolveContexts(rule, contextDataProvider)) {
                var field = context.getContextName() + "." + rule.getTargetPath();
                var instance = new RuleEvaluationInstance(session.getNamespace(), rule, context);
                defaultRuleEvaluations.computeIfAbsent(field, FieldEvaluation::new);
                defaultRuleEvaluations.get(field).addRuleEvaluationInstance(instance);
            }
        }
        return defaultRuleEvaluations;
    }

    private List<RuleOnInstanceEvaluationResult> evaluateDefaultRulesInPriorityOrder(
        List<RuleEvaluationInstance> priorityOrderedEvaluations,
        EvaluationSession session
    ) {
        List<RuleOnInstanceEvaluationResult> results = new ArrayList<>();
        RuleEvaluationInstance appliedEvaluation = null;
        boolean prioritizedEvaluation = priorityOrderedEvaluations.size() > 1;
        for (var evaluation : priorityOrderedEvaluations) {
            if(appliedEvaluation != null && appliedEvaluation.getPriority() > evaluation.getPriority()) {
                Tracer.doOperation(new SuppressedRuleOnInstanceEvaluationOperation(evaluation, appliedEvaluation));
                continue;
            }
            var result = evaluateRulePayload(evaluation, session, prioritizedEvaluation);
            results.add(result);

            if(result.getResult().getRuleEvaluationStatus() == RuleEvaluationStatus.APPLIED) {
                appliedEvaluation = evaluation;
            }
        }
        return results;
    }

    private List<DataContext> resolveContexts(RuntimeRule rule, ContextDataProvider contextDataProvider) {
        return Tracer.doOperation(
            new ContextResolutionOperation(rule),
            () -> contextDataProvider.resolveContextData(rule.getContext(), rule.getDependencies())
        );
    }

    private void addResult(RuleOnInstanceEvaluationResult result, Map<String, FieldEvaluationResult> results) {
        var dataContext = result.getDataContext();
        var fieldName = result.getResult().getRuleInfo().getTargetPath();
        var id = toFieldId(dataContext, fieldName);
        results.computeIfAbsent(
            id,
            i -> {
                var contextId = dataContext.getContextId();
                var contextName = dataContext.getContextName();
                var fieldPath = TargetPathUtils.resolveTargetPath(fieldName, dataContext);
                var fieldInfo = new ContextFieldInfo(contextId, contextName, fieldName, fieldPath);
                return new FieldEvaluationResult(fieldInfo, new ArrayList<>());
            });
        results.get(id).getRuleResults().add(result.getResult());
    }

    private String toFieldId(DataContext dataContext, String fieldName) {
        return dataContext.getContextName() + ":" + dataContext.getContextId() + ":" + fieldName;
    }

    private RuleOnInstanceEvaluationResult evaluateRulePayload(RuleEvaluationInstance evaluation,
                                                               EvaluationSession session,
                                                               boolean prioritizedEvaluation) {
        RuleEvaluationResult result = Tracer.doOperation(
            new RuleOnInstanceEvaluationOperation(evaluation, prioritizedEvaluation),
            () -> rulePayloadProcessor.process(evaluation, session)
        );
        return new RuleOnInstanceEvaluationResult(evaluation.getDataContext(), result);
    }

    private void validateDefaultsOnOneField(Map<String, FieldEvaluationResult> results) {
        for (var fieldEvaluationResult : results.values()) {
            List<RuleEvaluationResult> rulesAppliedOnField = fieldEvaluationResult.getRuleResults().stream()
                .filter(rr -> rr.getRuleEvaluationStatus() == RuleEvaluationStatus.APPLIED)
                .filter(rr -> rr.getPayloadResult() instanceof DefaultValuePayloadResult)
                .collect(Collectors.toList());
            if (rulesAppliedOnField.size() > 1) {
                throw new KrakenRuntimeException(String.format(
                    "On field '%s' applied '%s' default rules: %s. "
                        + "Only one default rule can be applied on the same field.",
                    fieldEvaluationResult.getContextFieldInfo(),
                    rulesAppliedOnField.size(),
                    rulesAppliedOnField.stream()
                        .map(x -> x.getRuleInfo().getRuleName())
                        .collect(Collectors.joining(", "))
                ));
            }
        }
    }

    static class RuleOnInstanceEvaluationResult {

        private final DataContext dataContext;

        private final RuleEvaluationResult result;

        public RuleOnInstanceEvaluationResult(DataContext dataContext, RuleEvaluationResult result) {
            this.dataContext = dataContext;
            this.result = result;
        }

        public DataContext getDataContext() {
            return dataContext;
        }

        public RuleEvaluationResult getResult() {
            return result;
        }
    }

    static class FieldEvaluation {

        private final String field;
        private final Map<String, List<RuleEvaluationInstance>> evaluations = new HashMap<>();

        public FieldEvaluation(String field) {
            this.field = Objects.requireNonNull(field);
        }

        public void addRuleEvaluationInstance(RuleEvaluationInstance ruleEvaluationInstance) {
            String contextId = ruleEvaluationInstance.getDataContext().getContextId();
            evaluations.computeIfAbsent(contextId, id -> new ArrayList<>());
            evaluations.get(contextId).add(ruleEvaluationInstance);
        }

        public String getField() {
            return field;
        }

        public Map<String, List<RuleEvaluationInstance>> getEvaluations() {
            return Collections.unmodifiableMap(evaluations);
        }
    }

    public static class DefaultRulesEvaluationOperation implements Operation<List<RuleOnInstanceEvaluationResult>> {

        private final List<RuntimeRule> defaultRules;

        public DefaultRulesEvaluationOperation(List<RuntimeRule> defaultRules) {
            this.defaultRules = defaultRules;
        }

        @Override
        public String describe() {
            return "Evaluating default rules.";
        }

        @Override
        public String describeAfter(List<RuleOnInstanceEvaluationResult> results) {
            Map<String, List<RuleOnInstanceEvaluationResult>> resultsByRuleName = results.stream()
                .collect(Collectors.groupingBy(result -> result.getResult().getRuleInfo().getRuleName()));
            var ruleTemplate = "'%s' on a total of %s instances.";
            var ruleTemplateUnused = "'%s' on a total of 0 instances. Evaluation status - UNUSED.";
            String ruleListString = defaultRules.stream().map(defaultRule -> {
                int resultSize = resultsByRuleName.containsKey(defaultRule.getName())
                    ? resultsByRuleName.get(defaultRule.getName()).size()
                    : 0;
                return resultSize == 0
                    ? String.format(ruleTemplateUnused, defaultRule.getName(), resultSize)
                    : String.format(ruleTemplate, defaultRule.getName(), resultSize);
            }).collect(Collectors.joining(System.lineSeparator()));

            return "Evaluated default rules." + System.lineSeparator() + ruleListString;
        }
    }

    public static class RuleEvaluationOperation implements Operation<List<RuleOnInstanceEvaluationResult>> {

        private final RuntimeRule runtimeRule;

        public RuleEvaluationOperation(RuntimeRule runtimeRule) {
            this.runtimeRule = runtimeRule;
        }

        @Override
        public String describe() {
            var template = "Evaluating rule '%s'.";

            return String.format(template, runtimeRule.getName());
        }

        @Override
        public String describeAfter(List<RuleOnInstanceEvaluationResult> results) {
            var template = "Evaluated rule '%s' on a total of %s instances.";
            var templateUnused = "Evaluated rule '%s' on a total of 0 instances. Evaluation status - UNUSED.";

            return results.isEmpty()
                ? String.format(templateUnused, runtimeRule.getName())
                : String.format(template, runtimeRule.getName(), results.size());
        }

    }

    public static class SuppressedRuleOnInstanceEvaluationOperation implements VoidOperation {
        private final RuleEvaluationInstance suppressedEvaluation;
        private final RuleEvaluationInstance appliedEvaluation;

        public SuppressedRuleOnInstanceEvaluationOperation(RuleEvaluationInstance suppressedEvaluation,
                                                           RuleEvaluationInstance appliedEvaluation) {
            this.suppressedEvaluation = suppressedEvaluation;
            this.appliedEvaluation = appliedEvaluation;
        }

        @Override
        public String describe() {
            var template = "Suppressing rule '%s' with priority '%s' on %s because rule '%s' "
                + "with higher priority '%s' was applied. Evaluation status - UNUSED.";

            return String.format(template,
                suppressedEvaluation.getRule().getName(),
                suppressedEvaluation.getPriority(),
                appliedEvaluation.getRule().getName(),
                appliedEvaluation.getPriority(),
                describeEvaluation()
            );
        }

        private String describeEvaluation() {
            return suppressedEvaluation.getDataContext().getContextName() + ":"
                + suppressedEvaluation.getDataContext().getContextId() + ":"
                + suppressedEvaluation.getRule().getTargetPath();
        }
    }

    public static class RuleOnInstanceEvaluationOperation implements Operation<RuleEvaluationResult> {

        private final RuleEvaluationInstance evaluation;

        private final boolean prioritizedEvaluation;

        public RuleOnInstanceEvaluationOperation(RuleEvaluationInstance evaluation, boolean prioritizedEvaluation) {
            this.evaluation = evaluation;
            this.prioritizedEvaluation = prioritizedEvaluation;
        }

        @Override
        public String describe() {
            var rule = evaluation.getRule();
            if(rule.getPayload().getType().equals(PayloadType.DEFAULT) && prioritizedEvaluation) {
                var template = "Evaluating rule '%s' on %s with priority %s.";
                return String.format(template, rule.getName(), describeEvaluation(), evaluation.getPriority());
            } else {
                var template = "Evaluating rule '%s' on %s.";
                return String.format(template, rule.getName(), describeEvaluation());
            }
        }

        @Override
        public String describeAfter(RuleEvaluationResult result) {
            var template = "Evaluated rule '%s' on %s. Evaluation status - %s.";

            return String.format(template,
                evaluation.getRule().getName(),
                describeEvaluation(),
                result.getRuleEvaluationStatus());
        }

        private String describeEvaluation() {
            return evaluation.getDataContext().getContextName() + ":"
                + evaluation.getDataContext().getContextId() + ":"
                + evaluation.getRule().getTargetPath();
        }
    }

    public static class ContextResolutionOperation implements Operation<List<DataContext>> {

        private final RuntimeRule rule;

        public ContextResolutionOperation(RuntimeRule rule) {
            this.rule = rule;
        }

        @Override
        public String describe() {
            return "";
        }

        @Override
        public String describeAfter(List<DataContext> contexts) {
            var template = "Resolved %s data context(s) for rule '%s' target '%s':"
                + System.lineSeparator() + "%s.";
            var templateNoContexts = "Resolved 0 data context(s) for rule '%s' target '%s'. ";

            return contexts.isEmpty()
                ? String.format(templateNoContexts, rule.getName(), rule.getContext())
                : String.format(template, contexts.size(), rule.getName(), rule.getContext(), describe(contexts));
        }

        private String describe(List<DataContext> contexts) {
            return contexts.stream()
                .map(dataContext -> dataContext.getContextName() + ":" + dataContext.getContextId())
                .collect(Collectors.joining("," + System.lineSeparator()));
        }

    }
}
