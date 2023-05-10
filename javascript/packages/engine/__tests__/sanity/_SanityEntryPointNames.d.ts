export type EntryPointName = "ForRestrictionCache" 
	| "Dimensional" 
	| "policy number must be empty" 
	| "RuleOrder" 
	| "RuleOrderWithComplexField" 
	| "RuleOrderWithNoCycleInFunction" 
	| "Expressions-Filter" 
	| "AccessibilityAutoPolicy" 
	| "AssertionAutoPolicy" 
	| "Cross-1" 
	| "FunctionCheck-Assert-PolicyNumber" 
	| "Expressions_kel_functions" 
	| "ExpressionContextCheck-Assert-CCR" 
	| "ExpressionContextCheck-NestedScope" 
	| "ExpressionContextCheck-NestedFilter" 
	| "ExpressionContextCheck-FlatMapDynamicContext" 
	| "ExpressionContextCheck-Condition" 
	| "ExpressionContextCheck-Default" 
	| "ExpressionContextCheck-Assert" 
	| "FunctionCheck-Default-With-Count" 
	| "FunctionCheck-Default-With-Sum" 
	| "FunctionCheck-Default-With-Avg" 
	| "FunctionCheck-Default-With-Min" 
	| "FunctionCheck-Default-With-Max" 
	| "FunctionCheck-Default-With-Substring" 
	| "FunctionCheck-Default-PolicyNumber" 
	| "FunctionCheck-RulesUsingFunctionOrderCheck" 
	| "FunctionCheck-Flat" 
	| "FunctionCheck-FromMoney" 
	| "Expressions_Date_Nested_Functions" 
	| "Expressions_Flat_with_Predicate" 
	| "Expressions_default_with_value_from_CCR_collection_count" 
	| "Expressions_default_driverType_with_value_from_CCR_count_with_predicate" 
	| "Expressions_assert_with_proposition_operations" 
	| "Expressions_default_with_filter_count_result" 
	| "Expressions_default_to_with_value_from_CCR_with_predicate" 
	| "Expressions_default_with_flat_and_filter_result_count" 
	| "Expressions_default_to_policy_state_with_if" 
	| "Expressions_instanceof" 
	| "Expressions_typeof" 
	| "Expressions_nullsafe" 
	| "Expressions_variables" 
	| "Expressions_nested_for" 
	| "Expressions_nested_filter" 
	| "Expressions_nested_mixed" 
	| "Expressions_compare_sum" 
	| "Expressions_escapes" 
	| "InitAutoPolicy" 
	| "LengthAutoPolicy" 
	| "PolicyCombined" 
	| "R-CCR-assert-AutoPolicy-to-CreditCardInfo" 
	| "R-CCR-assert-AutoPolicy-toCreditCard" 
	| "R-CCR-assert-BillingAddress" 
	| "R-CCR-assert-BillingAddress-toCreditCard" 
	| "R-CCR-assert-CreditCardInfo" 
	| "R-CCR-assert-DriverInfo-CreditCardInfo" 
	| "R-CCR-assert-DriverInfo-PersonInfo" 
	| "R-CCR-assert-Party-Info" 
	| "R-CCR-assert-Vehicle-toAutoPolicy" 
	| "R-CCR-default-CreditCardInfo-fromAutoPolicy" 
	| "R-CCR-default-condition-Policy-CreditCardInfo" 
	| "R-CCR-Policy-PartyRole" 
	| "R-CCR-Policy-ExPolicy" 
	| "Inheritance-CCR" 
	| "RegExpAutoPolicy" 
	| "UsagePayloadAutoPolicy" 
	| "Usage-UnknownField" 
	| "SizePayload" 
	| "SizeRangePayload" 
	| "VisibilityAutoPolicy" 
	| "assert-money" 
	| "default-money" 
	| "complex-field-type-test" 
	| "MultipleInsureds-default" 
	| "ForEach_EntryPoint" 
	| "ForSome_ForEvery_EntryPoint" 
	| "SelfReference" 
	| "AccessibilityCarCoverage" 
	| "AssertionCarCoverage" 
	| "InitCarCoverage" 
	| "UsagePayloadCarCoverage" 
	| "VisibilityCarCoverage" 
	| "ValidateInheritedMandatoryFields" 
	| "ValidateInheritedDefaultField" 
	| "ValidateInheritedPresentationFields" 
	| "AddressInfoLocalAndInheritedFieldRules" 
	| "Math" 
	| "Math_DevTesting" 
	| "Templates" 
	| "DefaultRuleByPriority" 
	| "EvaluateWithoutDuplicatedResults" 
	| "ValueListPayload" 
	| "NumberSet" 
	| "ForbiddenField" 
	| "TracerSnapshotTest";