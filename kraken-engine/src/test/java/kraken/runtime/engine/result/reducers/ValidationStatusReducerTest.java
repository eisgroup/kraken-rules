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
package kraken.runtime.engine.result.reducers;

import static kraken.runtime.model.expression.ExpressionType.COMPLEX;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsEqual.equalTo;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import kraken.TestRuleBuilder;
import kraken.el.ast.Ast;
import kraken.el.ast.builder.AstBuilder;
import kraken.el.scope.Scope;
import kraken.model.validation.UsageType;
import kraken.model.validation.ValidationSeverity;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.conditions.ConditionEvaluationResult;
import kraken.runtime.engine.dto.ContextFieldInfo;
import kraken.runtime.engine.dto.FieldEvaluationResult;
import kraken.runtime.engine.dto.OverridableRuleContextInfo;
import kraken.runtime.engine.dto.OverrideInfo;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.dto.RuleInfo;
import kraken.runtime.engine.result.UsagePayloadResult;
import kraken.runtime.engine.result.reducers.validation.RuleOverrideStatusResolver;
import kraken.runtime.engine.result.reducers.validation.ValidationResult;
import kraken.runtime.engine.result.reducers.validation.ValidationStatus;
import kraken.runtime.engine.result.reducers.validation.ValidationStatusReducer;
import kraken.runtime.model.expression.CompiledExpression;
import kraken.runtime.model.expression.ExpressionType;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.model.rule.payload.validation.ErrorMessage;
import kraken.runtime.model.rule.payload.validation.UsagePayload;

public class ValidationStatusReducerTest {

    private EntryPointResult entryPointResult;

    @Before
    public void setUp(){
        entryPointResult = entryPointResult(
                rule("R01", ValidationSeverity.critical),
                rule("R02", ValidationSeverity.critical),
                rule("R03", ValidationSeverity.critical)
        );
    }

    @Test
    public void shouldNotFilterOutRules(){
        List<ValidationResult> errorMessages = new ValidationStatusReducer(
                (RuleInfo rule, OverridableRuleContextInfo contextInfo) -> false
        ).reduce(entryPointResult).getErrorResults();

        assertThat(errorMessages, hasSize(3));
        List<String> ruleNames = errorMessages.stream().map(ValidationResult::getRuleName).collect(Collectors.toList());
        assertThat(ruleNames, containsInAnyOrder("R01", "R02", "R03") );
    }

    @Test
    public void shouldFilterOutOverriddenRule(){
        List<ValidationResult> errorMessages = new ValidationStatusReducer(
                (RuleInfo rule, OverridableRuleContextInfo contextInfo) -> rule.getRuleName().equals("R01")
        ).reduce(entryPointResult).getErrorResults();

        assertThat(errorMessages, hasSize(2));
        List<String> ruleNames = errorMessages.stream().map(ValidationResult::getRuleName).collect(Collectors.toList());
        assertThat(ruleNames, containsInAnyOrder( "R02", "R03"));
    }

    @Test
    public void shouldResolveOverridableRuleContextInfo() {
        RuleOverrideStatusResolver resolver = (RuleInfo rule, OverridableRuleContextInfo contextInfo) -> {
            assertThat(contextInfo.getContextAttributeValue() instanceof String, is(true));
            assertThat(contextInfo.getContextAttributeValue(), is("Number"));
            assertThat(contextInfo.getContextId(), is("contextId01"));
            assertThat(contextInfo.getRootContextId(), is("contextId01"));
            assertThat(contextInfo.getContextName(), is("Policy"));
            return rule.getRuleName().equals("R01");
        };
        new ValidationStatusReducer(resolver).reduce(entryPointResult);
    }

    @Test
    public void shouldReturnResultsSeparated(){
        EntryPointResult entryPointResult = entryPointResult(
                rule("R01", ValidationSeverity.critical),
                rule("R01", ValidationSeverity.warning),
                rule("R02", ValidationSeverity.warning),
                rule("R02", ValidationSeverity.info),
                rule("R03", ValidationSeverity.info),
                rule("R03", ValidationSeverity.critical)
        );

        List<ValidationResult> errorResults = new ValidationStatusReducer().reduce(entryPointResult).getErrorResults();
        List<ValidationResult> warningResults = new ValidationStatusReducer().reduce(entryPointResult).getWarningResults();
        List<ValidationResult> infoResults = new ValidationStatusReducer().reduce(entryPointResult).getInfoResults();

        assertThat(errorResults, hasSize(2));
        assertThat(warningResults, hasSize(2));
        assertThat(infoResults, hasSize(2));
        List<String> errorResultRuleNames = errorResults.stream()
                .map(ValidationResult::getRuleName)
                .distinct()
                .collect(Collectors.toList());
        List<String> warningResultRuleNames = warningResults.stream()
                .map(ValidationResult::getRuleName)
                .distinct()
                .collect(Collectors.toList());
        List<String> infoResultRuleNames = infoResults.stream()
                .map(ValidationResult::getRuleName)
                .distinct()
                .collect(Collectors.toList());
        assertThat(errorResultRuleNames, containsInAnyOrder("R01", "R03") );
        assertThat(warningResultRuleNames, containsInAnyOrder("R01", "R02") );
        assertThat(infoResultRuleNames, containsInAnyOrder("R02", "R03") );
    }

    @Test
    public void shouldTakeFieldPathAndNameFromRule() {
        EntryPointResult entryPointResult = entryPointResult(
                rule("R01", ValidationSeverity.critical)
        );
        final ValidationStatus validationStatus = new ValidationStatusReducer().reduce(entryPointResult);
        assertThat(validationStatus.getErrorResults().get(0).getContextFieldInfo().getFieldName(), is("policyNo"));
        assertThat(validationStatus.getErrorResults().get(0).getContextFieldInfo().getFieldPath(), is("policyNo"));
    }

    @Test
    public void shouldTakeFieldPathAndNameFromProjection() {
        EntryPointResult entryPointResult = entryPointResult(
                "policy.details.number",
                rule("R01", ValidationSeverity.critical)
        );

        final ValidationStatus validationStatus = new ValidationStatusReducer().reduce(entryPointResult);
        assertThat(validationStatus.getErrorResults().get(0).getContextFieldInfo().getFieldName(), is("policyNo"));
        assertThat(validationStatus.getErrorResults().get(0).getContextFieldInfo().getFieldPath(), is("policy.details.number"));
    }

    @Test
    public void shouldReduceMessageWithEscapedMessageFormatSymbols() {
        String VALIDATION_MESSAGE = "Policy's data is duplicated because 'number': 'No01' is already { present }";
        EntryPointResult entryPointResult = entryPointResult(ruleWithErrorMessage("R01", ValidationSeverity.critical,
            createErrorMessage(List.of("Policy's data is duplicated because 'number': '", "' is already { present }"),
                "No01")));

        final ValidationStatus validationStatus = new ValidationStatusReducer().reduce(entryPointResult);
        ValidationResult result = validationStatus.getErrorResults().get(0);
        assertThat(result.getMessageCode(), equalTo("code"));
        assertThat(result.getMessage(), equalTo(VALIDATION_MESSAGE));
        assertThat(result.getMessageTemplate(),
            equalTo("Policy''s data is duplicated because ''number'': ''{0}'' is already '{' present '}'"));

        String formatted = MessageFormat.format(result.getMessageTemplate(), "No01");
        assertThat(formatted, equalTo(VALIDATION_MESSAGE));
    }

    private EntryPointResult entryPointResult(String fieldPath, RuntimeRule...rules) {
        ContextFieldInfo contextFieldInfo = new ContextFieldInfo("contextId01", "Policy", "policyNo", fieldPath);

        List<RuleEvaluationResult> results = Arrays.stream(rules).map(rule -> evaluationResult(rule))
                .collect(Collectors.toList());

        FieldEvaluationResult fieldEvaluationResult = new FieldEvaluationResult(contextFieldInfo, results);

        return new EntryPointResult(Map.of("contextId01", fieldEvaluationResult), LocalDateTime.now(), ZoneId.systemDefault());
    }

    private EntryPointResult entryPointResult(RuntimeRule... rules) {
        return entryPointResult("policyNo", rules);
    }

    private RuleEvaluationResult evaluationResult(RuntimeRule rule){
        return new RuleEvaluationResult(
                new RuleInfo(
                        rule.getName(),
                        rule.getContext(),
                        rule.getTargetPath(),
                        rule.getPayload().getType()
                ),
                new UsagePayloadResult(
                        false,
                        (UsagePayload) rule.getPayload(),
                        ((UsagePayload) rule.getPayload()).getErrorMessage() != null
                            ? ((UsagePayload) rule.getPayload()).getErrorMessage().getTemplateExpressions().stream()
                                .map(CompiledExpression::getExpressionString)
                                .collect(Collectors.toList())
                            : List.of()
                ),
                ConditionEvaluationResult.APPLICABLE,
                new OverrideInfo(
                        ((UsagePayload) rule.getPayload()).isOverridable(),
                        ((UsagePayload) rule.getPayload()).getOverrideGroup(),
                        new OverridableRuleContextInfo(
                                "Policy",
                                "contextId01",
                                "contextId01",
                                "Policy",
                                "Number",
                                LocalDateTime.now(),
                                Map.of()
                        )
                )
        );
    }

    private ErrorMessage createErrorMessage(List<String> templateParts, String expression) {
        List<CompiledExpression> templateExpressions = new ArrayList<>();
        Ast ast = AstBuilder.from(expression, Scope.dynamic());
        templateExpressions.add(new CompiledExpression(expression, expression, COMPLEX, null, null, List.of(), ast));
        return new ErrorMessage("code", templateParts, templateExpressions);
    }

    private RuntimeRule ruleWithErrorMessage(String name, ValidationSeverity severity, ErrorMessage message){
        return TestRuleBuilder.getInstance().payload(
                new UsagePayload(
                    message,
                    severity,
                    true,
                    null,
                    UsageType.mandatory
                )
            )
            .targetPath("policyNo")
            .name(name)
            .build();
    }

    private RuntimeRule rule(String name, ValidationSeverity severity){
        return TestRuleBuilder.getInstance().payload(
            new UsagePayload(
                    null,
                    severity,
                    true,
                    null,
                    UsageType.mandatory
            )
        )
                .targetPath("policyNo")
                .name(name)
                .build();
    }

}
