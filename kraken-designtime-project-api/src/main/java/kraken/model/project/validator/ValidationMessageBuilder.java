/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.model.project.validator;

import static kraken.model.project.validator.Severity.ERROR;
import static kraken.model.project.validator.Severity.INFO;
import static kraken.model.project.validator.Severity.WARNING;

import java.text.MessageFormat;
import java.util.Objects;

import kraken.message.DocumentationAppender;
import kraken.model.KrakenModelItem;
import kraken.namespace.Namespaced;

/**
 * @author Mindaugas Ulevicius
 */
public class ValidationMessageBuilder {

    /**
     * Enum collects every kraken mode validation message with message code.
     * <p>
     * Message code has pattern kvM### where:
     * <ul>
     *     <li>kv - Kraken Validation</li>
     *     <li>M - single letter indicating kraken model type that is being validated. Possible values are:
     *         <ul>
     *             <li>c - context definition</li>
     *             <li>r - rule</li>
     *             <li>e - entry point</li>
     *             <li>f - function and function signature</li>
     *             <li>d - dimension</li>
     *             <li>x - external context and external context definition</li>
     *             <li>n - generic namespace related message</li>
     *         </ul>
     *     </li>
     *     <li>### - padded message id number</li>
     * </ul>
     *
     */
    public enum Message implements ValidationMessageDefinition {

        CONTEXT_NAME_IS_NULL(
            "kvc001",
            "Name is not defined.",
            ERROR
        ),
        CONTEXT_NAVIGATION_WRONG_MAP_KEY("kvc002",
            "Children map has key that is different from ContextNavigation.targetName.",
            ERROR
        ),
        CONTEXT_NAVIGATION_TARGET_IS_NULL("kvc003",
            "ContextNavigation.targetName is missing.",
            ERROR
        ),
        CONTEXT_NAVIGATION_CARDINALITY_IS_NULL("kvc004",
            "ContextNavigation.cardinality is missing.",
            ERROR
        ),
        CONTEXT_NAVIGATION_EXPRESSION_IS_NULL("kvc005",
            "ContextNavigation.navigationExpression is missing.",
            ERROR
        ),
        CONTEXT_NAVIGATION_UNKNOWN_CHILDREN("kvc006",
            "Child ''{0}'' is not valid because such context definition does not exist.",
            ERROR
        ),
        CONTEXT_NAVIGATION_IS_SYSTEM_CONTEXT("kvc007",
            "Child ''{0}'' is not valid because it is a system context definition. "
                + "System context definition cannot be used as a child in another context.",
            ERROR
        ),
        CONTEXT_NAVIGATION_IN_SYSTEM_CONTEXT("kvc008",
            "Child contexts are not allowed for system context definitions.",
            ERROR
        ),
        CONTEXT_FIELD_WRONG_MAP_KEY("kvc009",
            "ContextFields map has has key that is different from ContextField.name.",
            ERROR
        ),
        CONTEXT_FIELD_NAME_IS_NULL("kvc010",
            "ContextField.name is missing.",
            ERROR
        ),
        CONTEXT_FIELD_TYPE_IS_NULL("kvc011",
            "ContextField.fieldType is missing.",
            ERROR
        ),
        CONTEXT_FIELD_CARDINALITY_IS_NULL("kvc012",
            "ContextField.cardinality is missing.",
            ERROR
        ),
        CONTEXT_FIELD_PATH_IS_NULL("kvc013",
            "ContextField.fieldPath is missing.",
            ERROR
        ),
        CONTEXT_PARENT_DUPLICATE(
            "kvc014",
            "Inherited context definition ''{0}'' is specified twice - please remove duplicate.",
            ERROR
        ),
        CONTEXT_PARENT_UNKNOWN(
            "kvc015",
            "Inherited context definition ''{0}'' is not valid because "
                + "such context definition does not exist.",
            ERROR
        ),
        CONTEXT_PARENT_WRONG_STRICTNESS(
            "kvc016",
            "Context definition is strict but inherited context definition ''{0}'' is not.",
            ERROR
        ),
        CONTEXT_PARENT_IS_SYSTEM_CONTEXT(
            "kvc017",
            "Inherited context definition ''{0}'' is not valid because it is a system context definition.",
            ERROR
        ),
        CONTEXT_PARENT_IN_SYSTEM_CONTEXT(
            "kvc018",
            "Inherited context definition is not allowed for system context definition.",
            ERROR
        ),
        CONTEXT_PARENT_WRONG_FIELD_TYPE(
            "kvc019",
            "Field ''{0}'' is overridden but it has a different type "
                + "in inherited context definition ''{1}''.",
            ERROR
        ),

        EXTERNAL_CONTEXT_ROOT_MISSING(
            "kvx001",
            "Root external context definition should be empty or have ONE element named 'context', "
                + "but found: {0}.",
            ERROR
        ),
        EXTERNAL_CONTEXT_REFERENCE_MISSING(
            "kvx002",
            "Referenced external context definition must exist in kraken project, "
                + "but the following referenced contexts are not found: {0}.",
            ERROR
        ),
        EXTERNAL_CONTEXT_CHILD_CLASH(
            "kvx003",
            "Naming clash between external context definitions and child external context found, "
                + "clashing values: {0}.",
            ERROR
        ),
        EXTERNAL_CONTEXT_UNKNOWN_FIELD_TYPE(
            "kvx004",
            "Type ''{0}'' of field ''{1}'' is unknown or not supported.",
            ERROR
        ),

        DIMENSION_VALUE_TYPE_INCOMPATIBLE(
            "kvd001",
            "Dimension ''{0}'' value is ''{1}'', but such value cannot be set to this dimension. "
                + "Expected dimension value type is ''{2}''.",
            ERROR
        ),

        ENTRYPOINT_NAME_IS_NULL(
            "kve001",
            "Name is not defined.",
            ERROR
        ),
        ENTRYPOINT_UNKNOWN_INCLUDE(
            "kve002",
            "Included entry point ''{0}'' does not exist.",
            ERROR
        ),
        ENTRYPOINT_DUPLICATE_INCLUDE(
            "kve003",
            "Included entry point ''{0}'' has one or more rule with the same name: ''{1}''.",
            WARNING
        ),
        ENTRYPOINT_NESTED_INCLUDE(
            "kve004",
            "Included entry point ''{0}'' has includes. It is not allowed to have nested entry point "
                + "includes.",
            ERROR
        ),
        ENTRYPOINT_UNKNOWN_RULE(
            "kve005",
            "Rule is included in entry point, but such rule does not exist: {0}.",
            ERROR
        ),
        ENTRYPOINT_INCONSISTENT_VERSION_SERVER_SIDE_ONLY(
            "kve006",
            "Entry point version is misconfigured, because it is not annotated as @ServerSideOnly, "
                + "but there are another entry point version that is annotated as @ServerSideOnly. "
                + "All versions of the same entry point must be consistently annotated as @ServerSideOnly.",
            ERROR
        ),
        ENTRYPOINT_INCONSISTENT_RULE_SERVER_SIDE_ONLY(
            "kve007",
            "Entry point is not annotated as @ServerSideOnly, "
                + "but includes one or more rule annotated as @ServerSideOnly: {0}.",
            ERROR
        ),
        ENTRYPOINT_INCONSISTENT_INCLUDE_SERVER_SIDE_ONLY(
            "kve008",
            "Entry point is not annotated as @ServerSideOnly, "
                + "but includes one or more entry point annotated as @ServerSideOnly: {0}.",
            ERROR
        ),

        FUNCTION_NAME_DUPLICATE(
            "kvf001",
            "Function is not valid because there are more than one function defined "
                + "with the same name: {0}.",
            ERROR
        ),
        FUNCTION_NAME_DUPLICATE_WITH_SIGNATURE(
            "kvf002",
            "Function is not valid because function signature with the same name is defined: {0}.",
            ERROR
        ),
        FUNCTION_NATIVE_DUPLICATE(
            "kvf003",
            "Function is not valid because native function with the same name exists: {0}.",
            ERROR
        ),
        FUNCTION_GENERIC_BOUND_DUPLICATE(
            "kvf004",
            "Function is not valid because there are more than one generic bound for "
                + "the same generic type name: {0}.",
            ERROR
        ),
        FUNCTION_GENERIC_BOUND_IS_ITSELF_GENERIC(
            "kvf005",
            "Function is not valid because generic type bound ''{0}'' for generic ''{1}'' "
                + "is itself a generic type.",
            ERROR
        ),
        FUNCTION_RETURN_TYPE_UNKNOWN(
            "kvf006",
            "Function is not valid because return type ''{0}'' does not exist.",
            ERROR
        ),
        FUNCTION_RETURN_TYPE_UNION_GENERIC_MIX(
            "kvf007",
            "Function is not valid because return type ''{0}'' is a mix of union type and generic type. "
                + "Such type definition is not supported.",
            ERROR
        ),
        FUNCTION_PARAMETER_DUPLICATE(
            "kvf008",
            "Function is not valid because there are more than one parameter with the same name defined: {0}.",
            ERROR
        ),
        FUNCTION_PARAMETER_TYPE_UNKNOWN(
            "kvf009",
            "Function is not valid because parameter type ''{0}'' does not exist",
            ERROR
        ),
        FUNCTION_PARAMETER_TYPE_UNION_GENERIC_MIX(
            "kvf010",
            "Function is not valid because parameter type ''{0}'' is a mix of union type and generic type. "
                + "Such type definition is not supported.",
            ERROR
        ),
        FUNCTION_BODY_WRONG_RETURN_TYPE(
            "kvf011",
            "Evaluation type of function body expression is ''{0}'' and it is not assignable to "
                + "function return type ''{1}''.",
            ERROR
        ),
        FUNCTION_BODY_NOT_PARSEABLE(
            "kvf012",
            "Function body expression cannot be parsed, because there is an error in expression syntax.",
            ERROR
        ),
        FUNCTION_BODY_IS_LOGICALLY_EMPTY(
            "kvf013",
            "Function body expression is logically empty. "
                + "Please check if there are unintentional spaces, new lines or comments remaining.",
            ERROR
        ),
        FUNCTION_DOCUMENTATION_PARAMETER_DUPLICATE(
            "kvf014",
            "Error in function documentation. Found more than one description for parameter: {0}.",
            ERROR
        ),
        FUNCTION_DOCUMENTATION_PARAMETER_UNKNOWN(
            "kvf015",
            "Error in function documentation. Parameter is documented but does not exist: {0}. "
                + "Function has defined only these parameters: {1}.",
            ERROR
        ),
        FUNCTION_SIGNATURE_DUPLICATE(
            "kvf016",
            "Function signature is not valid because there are more than one function signature "
                + "defined with the same header: {0}.",
            ERROR
        ),
        FUNCTION_SIGNATURE_GENERIC_BOUND_DUPLICATE(
            "kvf017",
            "Function signature is not valid because there are more than one generic bound for the "
                + "same generic type name: {0}.",
            ERROR
        ),
        FUNCTION_SIGNATURE_GENERIC_BOUND_IS_ITSELF_GENERIC(
            "kvf018",
            "Function signature is not valid because generic type bound ''{0}'' for generic ''{1}'' "
                + "is itself a generic type.",
            ERROR
        ),
        FUNCTION_SIGNATURE_RETURN_TYPE_UNKNOWN(
            "kvf019",
            "Function signature is not valid because return type ''{0}'' does not exist.",
            ERROR
        ),
        FUNCTION_SIGNATURE_RETURN_TYPE_UNION_GENERIC_MIX(
            "kvf020",
            "Function signature is not valid because return type ''{0}'' is a mix of union type "
                + "and generic type. "
                + "Such type definition is not supported.",
            ERROR
        ),
        FUNCTION_SIGNATURE_PARAMETER_TYPE_UNKNOWN(
            "kvf021",
            "Function signature is not valid because parameter type ''{0}'' does not exist.",
            ERROR
        ),
        FUNCTION_SIGNATURE_PARAMETER_TYPE_UNION_GENERIC_MIX(
            "kvf022",
            "Function signature is not valid because parameter type ''{0}'' is a mix of union type "
                + "and generic type. Such type definition is not supported.",
            ERROR
        ),
        FUNCTION_BODY_EXPRESSION_SYNTAX_ERROR(
            "kvf023",
            "Function body expression has error in ''{1}''. {2}",
            ERROR
        ),
        FUNCTION_BODY_EXPRESSION_SYNTAX_WARNING(
            "kvf024",
            "Function body expression has warning message about ''{1}''. {2}",
            WARNING
        ),
        FUNCTION_BODY_EXPRESSION_SYNTAX_INFO(
            "kvf025",
            "Function body expression has information message about ''{1}''. {2}",
            INFO
        ),

        NAMESPACED_NAME_HAS_FORBIDDEN_SYMBOLS(
            "kvn001",
            "Name cannot contain '" + Namespaced.SEPARATOR + "' symbol",
            ERROR
        ),

        RULE_NAME_IS_NULL(
            "kvr001",
            "Rule name is not defined.",
            ERROR
        ),
        RULE_CONTEXT_IS_NULL(
            "kvr002",
            "Rule context is not defined.",
            ERROR
        ),
        RULE_TARGET_PATH_IS_NULL(
            "kvr003",
            "Rule targetPath is not defined.",
            ERROR
        ),
        RULE_CONDITION_EXPRESSION_IS_NULL(
            "kvr004",
            "Rule condition exists but condition expression is not defined.",
            ERROR
        ),
        RULE_PAYLOAD_IS_NULL(
            "kvr005",
            "Rule payload is not defined.",
            ERROR
        ),
        RULE_VALIDATION_SEVERITY_IS_NULL(
            "kvr006",
            "Rule validation severity is not defined.",
            ERROR
        ),
        RULE_VALIDATION_CODE_IS_NULL(
            "kvr007",
            "Rule validation message code is not defined.",
            ERROR
        ),
        RULE_ASSERTION_EXPRESSION_IS_NULL(
            "kvr008",
            "Rule assertion expression not defined.",
            ERROR
        ),
        RULE_DEFAULT_TYPE_IS_NULL(
            "kvr009",
            "Rule defaulting type is not defined.",
            ERROR
        ),
        RULE_DEFAULT_EXPRESSION_IS_NULL(
            "kvr010",
            "Rule default value expression is not defined.",
            ERROR
        ),
        RULE_SIZE_MIN_IS_NOT_POSITIVE(
            "kvr011",
            "Min must be positive.",
            ERROR
        ),
        RULE_SIZE_MAX_IS_NOT_POSITIVE(
            "kvr012",
            "Max must be positive.",
            ERROR
        ),
        RULE_SIZE_MIN_NOT_LESS_THAN_MAX(
            "kvr013",
            "Min must be less than max.",
            ERROR
        ),
        RULE_SIZE_ORIENTATION_IS_NULL(
            "kvr014",
            "Orientation is not defined.",
            ERROR
        ),
        RULE_SIZE_IS_NOT_POSITIVE(
            "kvr015",
            "Size must be positive.",
            ERROR
        ),
        RULE_REGEXP_IS_NULL(
            "kvr016",
            "Regular expression is not defined.",
            ERROR
        ),
        RULE_USAGE_TYPE_IS_NULL(
            "kvr017",
            "Usage type is not defined.",
            ERROR
        ),
        RULE_LENGTH_IS_NOT_POSITIVE(
            "kvr018",
            "Length must be positive.",
            ERROR
        ),
        RULE_NUMBER_SET_MIN_AND_MAX_NOT_SET(
            "kvr019",
            "Min or max must be set.",
            ERROR
        ),
        RULE_NUMBER_SET_MIN_NOT_SMALLER_THAN_MAX(
            "kvr020",
            "Min must be smaller than max.",
            ERROR
        ),
        RULE_NUMBER_SET_STEP_NOT_MORE_THAN_ZERO(
            "kvr021",
            "Step must be more than zero.",
            ERROR
        ),
        RULE_PRIORITY_NOT_IN_DEFAULT(
            "kvr022",
            "Priority cannot be set because rule payload type is {0}"
                + " - priority is supported only for defaulting rules.",
            ERROR
        ),
        RULE_VALUE_LIST_IS_NULL(
            "kvr023",
            "Value list must be set.",
            ERROR
        ),
        RULE_VALUE_LIST_TYPE_IS_NULL(
            "kvr024",
            "Value list data type must be set.",
            ERROR
        ),
        RULE_VALUE_LIST_IS_EMPTY(
            "kvr025",
            "Value list should contain at least one value.",
            ERROR
        ),
        RULE_DECIMAL64_PRECISION_LOSS(
            "kvr026",
            "{0} value ''{1}'' cannot be encoded as a decimal64 without a loss of precision. "
                + "Actual number at runtime would be rounded to ''{2}''.",
            WARNING
        ),

        RULE_TARGET_CONTEXT_UNKNOWN(
            "kvr027",
            "Missing context definition with name ''{0}''.",
            ERROR
        ),
        RULE_TARGET_CONTEXT_SYSTEM(
            "kvr028",
            "Cannot be applied on system context definition ''{0}''.",
            ERROR
        ),
        RULE_TARGET_CONTEXT_FIELD_UNKNOWN(
            "kvr029",
            "Context definition ''{0}'' doesn''t have field ''{1}''.",
            ERROR
        ),
        RULE_TARGET_CONTEXT_FIELD_FORBIDDEN(
            "kvr030",
            "Cannot be applied on a field ''{0}'' because it is forbidden to be a rule target.",
            ERROR
        ),
        RULE_INCONSISTENT_VERSION_TARGET(
            "kvr031",
            "Rule has version applied on different context or attribute: {0}.",
            ERROR
        ),
        RULE_NOT_IN_ENTRYPOINT(
            "kvr032",
            "Rule is not added to any entry point.",
            WARNING
        ),
        RULE_PAYLOAD_NOT_COMPATIBLE(
            "kvr033",
            "Rule payload {0} cannot be applied on {1}.{2} because type ''{3}'' and cardinality ''{4}'' "
                + "is incompatible with the payload type.",
            ERROR
        ),
        RULE_PAYLOAD_NOT_COMPATIBLE_WARNING(
            "kvr034",
            "Rule payload {0} cannot be applied on {1}.{2} because type ''{3}'' and cardinality ''{4}'' "
                + "is incompatible with the payload type. In the future, such configuration will not be supported.",
            WARNING
        ),
        RULE_TARGET_CONTEXT_DANGLING(
            "kvr035",
            "Rule is applied on context definition ''{0}'' "
                + "which is not related to root context definition ''{1}''.",
            ERROR
        ),
        RULE_TARGET_CONTEXT_IN_CYCLE(
            "kvr036",
            "Rule is defined on context definition ''{0}'', " +
                "which is included in a recursive data structure between: ''{1}''. " +
                "Defining rules on recursive context definition is not supported.",
            ERROR
        ),
        RULE_INCONSISTENT_VERSION_SERVER_SIDE_ONLY(
            "kvr037",
            "Rule version is misconfigured, because it is not marked as @ServerSideOnly, "
                + "but there are another rule version that is marked as @ServerSideOnly. "
                + "All versions of the same rule must be consistently marked as @ServerSideOnly.",
            ERROR
        ),
        RULE_CCR_IS_AMBIGUOUS(
            "kvr038",
            "Cross context reference ''{0}'' from ''{1}'' is ambiguous. "
                + "Cannot distinguish between references: {2}.",
            ERROR
        ),
        RULE_CCR_DIFFERENT_CARDINALITIES(
            "kvr039",
            "Cross context reference from ''{0}'' to ''{1}'' resolves to different cardinalities in "
                + "different parts of the model. {2}",
            ERROR
        ),

        RULE_TEMPLATE_RETURN_TYPE_NOT_PRIMITIVE(
            "kvr040",
            "Return type of expression ''{0}'' in validation message template must be primitive "
                + "or array of primitives, but found: {1}.",
            ERROR
        ),
        RULE_ASSERTION_RETURN_TYPE_NOT_BOOLEAN(
            "kvr041",
            "Return type of assertion expression must be BOOLEAN, but found: {0}.",
            ERROR
        ),
        RULE_EXPRESSION_COERCE_DATETIME_TO_DATE(
            "kvr042",
            "Return type of default expression must be compatible with field type which is {0}, "
                + "but expression return type is {1}. {1} value will be automatically converted to {0} value as a "
                + "date in local locale at that moment in time. "
                + "Automatic conversion should be avoided because it is a lossy operation "
                + "and the converted value depends on the local locale "
                + "which may produce inconsistent rule evaluation results.",
            WARNING
        ),
        RULE_EXPRESSION_COERCE_DATE_TO_DATETIME(
            "kvr043",
            "Return type of default expression must be compatible with field type which is {0}, "
                + "but expression return type is {1}. {1} value will be automatically converted to {0} value as a "
                + "moment in time at the start of the day in local locale. "
                + "Automatic conversion should be avoided because it is a lossy operation "
                + "and the converted value depends on the local locale "
                + "which may produce inconsistent rule evaluation results.",
            WARNING
        ),
        RULE_DEFAULT_RETURN_TYPE_NOT_COMPATIBLE(
            "kvr044",
            "Return type of default expression must be compatible with field type which is {0}, "
                + "but expression return type is {1}.",
            ERROR
        ),
        RULE_CONDITION_RETURN_TYPE_NOT_BOOLEAN(
            "kvr045",
            "Return type of condition expression must be BOOLEAN, but found: {0}.",
            ERROR
        ),
        RULE_CONDITION_REDUNDANT_TRUE(
            "kvr046",
            "Redundant literal value 'true' in rule condition expression. "
                + "An empty condition expression is 'true' by default.",
            INFO
        ),

        RULE_EXPRESSION_IS_LOGICALLY_EMPTY(
            "kvr047",
            "{0} expression is logically empty. "
                + "Please check if there are unintentional spaces, new lines or comments remaining.",
            ERROR
        ),
        RULE_EXPRESSION_IS_NOT_PARSEABLE(
            "kvr048",
            "{0} expression cannot be parsed, because there is an error in expression syntax.",
            ERROR
        ),
        RULE_EXPRESSION_SYNTAX_ERROR(
            "kvr049",
            "{0} expression has error in ''{1}''. {2}",
            ERROR
        ),
        RULE_EXPRESSION_SYNTAX_WARNING(
            "kvr050",
            "{0} expression has warning message about ''{1}''. {2}",
            WARNING
        ),
        RULE_EXPRESSION_SYNTAX_INFO(
            "kvr051",
            "{0} expression has information message about ''{1}''. {2}",
            INFO
        ),
        DYNAMIC_RULE_SERVER_SIDE_ONLY_IN_REGULAR_ENTRYPOINT(
            "kvr052",
            "Rule annotated as @ServerSideOnly is included into an entry point ''{0}'' "
                + "which is not annotated as @ServerSideOnly",
            ERROR
        ),
        DUPLICATE_RULE_VERSION(
            "kvr053",
            "Rule version has duplicates. "
                + "Rule version is uniquely identified by rule name and dimensions. "
                + "If more than one rule version with the same name and dimensions is present, "
                + "then only one of duplicated rules versions will be selected for evaluation. "
                + "Review all rule definitions and remove duplicates.",
            WARNING
        ),
        DUPLICATE_ENTRYPOINT_VERSION(
            "kvr054",
            "Entry point version has duplicates. "
                + "Entry point version is uniquely identified by entry point name and dimensions. "
                + "If more than one entry point version with the same name and dimensions is present, "
                + "then only one of duplicated entry points versions will be selected for evaluation. "
                + "Review all entry point definitions and remove duplicates.",
            WARNING
        ),
        ;

        private final String code;
        private final String messageTemplate;
        private final Severity severity;

        Message(String code, String messageTemplate, Severity severity) {
            this.code = code;
            this.messageTemplate = messageTemplate;
            this.severity = severity;
        }

        public String getCode() {
            return code;
        }

        public String getMessageTemplate() {
            return messageTemplate;
        }

        public Severity getSeverity() {
            return severity;
        }

    }

    private final ValidationMessageDefinition message;

    private final KrakenModelItem item;

    private Object[] parameters;

    public ValidationMessageBuilder(ValidationMessageDefinition message, KrakenModelItem item) {
        this.message = Objects.requireNonNull(message);
        this.item = Objects.requireNonNull(item);
    }

    public static ValidationMessageBuilder create(ValidationMessageDefinition message, KrakenModelItem item) {
        return new ValidationMessageBuilder(message, item);
    }

    public ValidationMessageBuilder parameters(Object... parameters) {
        this.parameters = parameters;

        return this;
    }

    public ValidationMessage build() {
        String message = MessageFormat.format(this.message.getMessageTemplate(), this.parameters);

        if (this.message instanceof Enum<?>) {
            message = DocumentationAppender.append((Enum<?>) this.message, message);
        }

        return new ValidationMessage(
            this.item,
            this.message.getCode(),
            message,
            this.message.getMessageTemplate(),
            this.parameters,
            this.message.getSeverity()
        );
    }
}
