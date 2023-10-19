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
package kraken.message;

import java.text.MessageFormat;
import java.util.Objects;

/**
 * @author Mindaugas Ulevicius
 */
public class SystemMessageBuilder {

    /**
     * Enum collects every kraken system message with message code.
     * These message might be logged or thrown as part of exception message.
     * Message code has pattern kbs### where:
     * <ul>
     *     <li>kbs - Kraken Backend System</li>
     *     <li>### - padded message id number</li>
     * </ul>
     */
    public enum Message {
        DSL_NOT_VALID("kbs001", "Invalid DSL at ''{0}''."),
        DSL_CANNOT_COLLECT("kbs002", "Error while collecting rules DSL files from ''{0}'' by pattern ''{1}''."),
        DSL_CANNOT_READ("kbs003", "Failed to read rules DSL file from ''{0}''."),
        DSL_CANNOT_PARSE_URL("kbs004", "Error while parsing URI from URL: {0}."),
        CCR_NAVIGATION_NOT_FOUND("kbs005", "Couldn't find navigation from ''{0}'' to ''{1}''"),
        KRAKEN_PROJECT_NOT_VALID(
            "kbs006",
            "kraken project for namespace ''{0}'' is not valid. Validation errors: {1}"
        ),
        KRAKEN_PROJECT_DYNAMIC_RULE_NOT_VALID(
            "kbs007",
            "Dynamic Rule ''{0}'' is not valid for kraken project in namespace ''{1}''. Validation errors: {2}"
        ),
        CONTEXT_MODEL_TREE_CANNOT_COLLECT(
            "kbs008",
            "Error while collecting context model tree resources by pattern ''{1}''."
        ),
        CONTEXT_MODEL_TREE_CANNOT_READ(
            "kbs009",
            "Failed to read context model tree resource file from ''{0}''."
        ),
        KRAKEN_PROJECT_UNKNOWN_NAMESPACE(
            "kbs010",
            "Unknown namespace: ''{0}''. This indicates a Kraken Rule deployment gone wrong "
                + "or Kraken Engine is executed with namespace that does not exist."
        ),
        CONVERSION_MISSING_FUNCTION(
            "kbs011",
            "Critical error encountered while converting kraken project ''{0}'' "
                + "from design-time to runtime model: function signature ''{1}'' is defined "
                + "but implementation for this function does not exist in system."
        ),
        CONVERSION_INCOMPATIBLE_FUNCTION(
            "kbs012",
            "Critical error encountered while converting kraken project ''{0}'' "
                + "from design-time to runtime model: function signature ''{1}'' is defined "
                + "but implementation of this function is not compatible with defined signature."
        ),
        CONVERSION_CANNOT_TRANSLATE_EXPRESSION(
            "kbs013",
            "Error while translating expression: {0}."
        ),

        KRAKEN_PROJECT_BUILD_NAMESPACE_STRUCTURE_INVALID(
            "kbs014",
            "Namespace structure is invalid. Errors: {0}"
        ),
        KRAKEN_PROJECT_BUILD_NAMESPACE_UNKNOWN(
            "kbs015",
            "Trying to create kraken project for namespace that does not exist: {0}."
        ),
        KRAKEN_PROJECT_BUILD_NO_CONTEXT_DEFINITIONS(
            "kbs016",
            "kraken project for namespace {0} does not have any context definition defined. " +
                "At least one context definition is expected."
        ),
        KRAKEN_PROJECT_BUILD_NO_ROOT_CONTEXT_DEFINITION(
            "kbs017",
            "kraken project for namespace {0} does not have a root context definition defined."
        ),
        KRAKEN_PROJECT_BUILD_MULTIPLE_ROOT_CONTEXT_DEFINITION(
            "kbs018",
            "kraken project must have exactly one root context definition, "
                + "but multiple root context definitions found in kraken project for namespace {0}: {1}."
        ),
        KRAKEN_PROJECT_BUILD_RULE_NOT_IN_ENTRYPOINT(
            "kbs019",
            "Rule ''{0}'' is not included into any entry point and is unused in kraken project for namespace {1}. " +
                "This rule will be removed from kraken project and excluded from any further calculations. " +
                "Such kraken project configurations may not be supported in the future."
        ),
        KRAKEN_PROJECT_BUILD_FUNCTION_NOT_UNIQUE(
            "kbs020",
            "Functions defined in DSL must be unique by function name, "
                + "but duplicate functions are defined in namespace ''{0}'': {1} "
                + "Please review affected DSL resources and remove duplicated functions: {2}"
        ),
        KRAKEN_PROJECT_BUILD_FUNCTION_SIGNATURE_NOT_UNIQUE(
            "kbs021",
            "Function signatures defined in DSL must be unique by function name and parameter count, "
                + "but duplicate function signatures are defined in namespace ''{0}'': {1} "
                + "Please review affected DSL resources and remove duplicated function signatures: {2}"
        ),
        KRAKEN_PROJECT_BUILD_CONTEXT_DEFINITIONS_NOT_UNIQUE(
            "kbs022",
            "Duplicate context definitions definitions found in namespace ''{0}'': {1}."
        ),
        KRAKEN_PROJECT_BUILD_DIMENSIONS_NOT_UNIQUE(
            "kbs023",
            "Dimensions defined in DSL must be unique by dimension name, "
                + "but duplicate dimensions are defined in namespace ''{0}'': {1} "
                + "Please review affected DSL resources and remove duplicated dimensions: {2}"
        ),
        KRAKEN_PROJECT_BUILD_NAMESPACE_INCLUDE_UNKNOWN(
            "kbs024",
            "Cannot include namespace ''{0}'' to ''{1}'', because namespace does not exist."
        ),
        KRAKEN_PROJECT_BUILD_RULE_IMPORT_UNKNOWN(
            "kbs025",
            "Cannot import rule ''{0}'' from namespace ''{1}'' to ''{2}'', because rule does not exist."
        ),
        KRAKEN_PROJECT_BUILD_RULE_IMPORT_NAMESPACE_UNKNOWN(
            "kbs026",
            "Cannot import rule ''{0}'' from namespace ''{1}'' to ''{2}'', because namespace does not exist."
        ),
        KRAKEN_PROJECT_BUILD_RULE_IMPORT_DUPLICATE(
            "kbs027",
            "Cannot import rule ''{0}'' from namespace ''{1}'' to ''{2}'', because rule is already defined."
        ),
        KRAKEN_PROJECT_BUILD_RULE_IMPORT_AMBIGUOUS(
            "kbs028",
            "Ambiguous import found in namespace ''{0}'' for rule ''{1}'' to ''{2}'', "
                + "because it is imported from multiple namespaces: %s."
        ),
        KRAKEN_PROJECT_BUILD_EXTERNAL_CONTEXT_DUPLICATE(
            "kbs029",
            "Multiple conflicting external contexts defined in namespace ''{0}''. "
                + "Only one external context can be configured per namespace."
        ),
        KRAKEN_PROJECT_BUILD_NAMESPACE_INCLUDE_ERRORS(
            "kbs030",
            "Kraken project ''{0}'' has namespace include errors: {1}"
        ),
        KRAKEN_PROJECT_BUILD_NAMESPACE_INCLUDE_AMBIGUOUS(
            "kbs031",
            "Item ''{0}'' is ambiguous, because it is included from multiple namespaces: {1}."
        ),
        KRAKEN_PROJECT_BUILD_NAMESPACE_INCLUDE_CYCLE(
            "kbs032",
            "A cycle found between namespaces: {0}."
        ),
        KRAKEN_PROJECT_FIELD_IS_NULL(
            "kbs033",
            "{0} in kraken project ''{1}'' must not be null."
        ),
        KRAKEN_PROJECT_ROOT_CONTEXT_DEFINITION_UNKNOWN(
            "kbs034",
            "Root context definition is {0}, but such context definition does not exist in kraken project: {1}"
        ),
        KRAKEN_DSL_GLOBAL_NAMESPACE_INCLUDES_NAMESPACE(
            "kbs035",
            "Global namespace cannot include other namespaces."
        ),
        KRAKEN_DSL_GLOBAL_NAMESPACE_IMPORTS_RULE(
            "kbs036",
            "Global namespace cannot import rules from other namespaces."
        ),
        KRAKEN_DSL_GLOBAL_NAMESPACE_MULTIPLE_EXTERNAL_CONTEXT(
            "kbs037",
            "Only one root external context is allowed per namespace."
        ),
        CONTEXT_EXTRACTION_MISSING_EXTRACTION_PATH(
            "kbs038",
            "Could not find any extraction paths from {0} to {1}."
        ),
        CONTEXT_BUILD_INVALID_DATA(
            "kbs039",
            "Failed to build data context from object of type: {0}. Reason: {1}."
        ),
        EXPRESSION_CANNOT_EVALUATE_VALUE(
            "kbs040",
            "Error while evaluating value expression: {0}"
        ),
        EXPRESSION_CANNOT_EVALUATE_GET(
            "kbs041",
            "Error while evaluating get expression: {0}"
        ),
        EXPRESSION_CANNOT_EVALUATE_SET(
            "kbs042",
            "Error while evaluating set expression: {0}"
        ),
        DEFAULT_VALUE_PAYLOAD_INCOMPATIBLE_VALUE(
            "kbs043",
            "Cannot apply value ''{0} (instanceof {1})'' on ''{2}.{3}'' because value type is not assignable to "
                + "field type ''{4}''. Rule will be silently ignored."
        ),
        CONTEXT_PATH_EXTRACTION_MISSING(
            "kbs044",
            "Failed to find reference path from ''{0}'' to ''{1}''."
        ),
        DEFAULT_RULE_DEPENDENCY_CYCLE_DETECTED(
            "kbs045",
            "Cycle detected between fields: {0}. Involved rules are: {1}"
        ),
        @Documented
        DEFAULT_RULE_MULTIPLE_ON_SAME_FIELD(
            "kbs046",
            "Field ''{0}'' has more than one applicable default rule: {1}. "
                + "Only one default rule can be applied on the same field."
        ),
        VALUE_LIST_PAYLOAD_CANNOT_CONVERT_TO_NUMBER(
            "kbs047",
            "Unable to convert value list item value {0} to a number."
        ),
        VALUE_LIST_PAYLOAD_CANNOT_CONVERT_TO_STRING(
            "kbs048",
            "Unable to convert value list item value {0} to a string."
        ),
        DYNAMIC_RULE_MISSING_VARIATION_ID(
            "kbs049",
            "Dynamic Rule ''{0}'' does not have ruleVariationId defined. " +
                "Validation and caching will be skipped for this rule."
        ),
        KRAKEN_PROJECT_APPROXIMATE_CONTEXT_SCOPE(
            "kbs050",
            "Cannot build exact Scope of {0} in kraken project for namespace {1}, " +
                "because such context definition is not accessible from root context definition {2}. "
                + "An approximate Scope will be used."
        ),
        RULE_REPOSITORY_DUPLICATE_RULE(
            "kbs051",
            "More than one rule found with name ''{0}'' for entry point ''{1}''. "
                + " Only the first rule will be evaluated. "
                + "Additional rules with the same name will be ignored."
        ),
        RULE_REPOSITORY_FILTERING_MULTIPLE_RULES(
            "kbs052",
            "More than one rule found with name ''{0}'' for dimensions {1}. "
                + "Only the first rule will be evaluated."
                + "Additional rules with the same name will be ignored."
        ),
        RULE_REPOSITORY_FILTERING_MULTIPLE_ENTRYPOINTS(
            "kbs053",
            "More than one entry point found with name ''{0}'' for dimensions {1}. "
                + "Only the first entry point will be evaluated."
                + "Additional entry points with the same name will be ignored."
        ),
        RULE_CONDITION_EXPRESSION_EVALUATION_FAILURE(
            "kbs054",
            "Condition expression ''{0}'' failed with exception."
        ),
        RULE_DEFAULT_VALUE_EXPRESSION_EVALUATION_FAILURE(
            "kbs055",
            "Default value expression ''{0}'' of rule ''{1}'' failed with exception."
        ),
        RULE_ASSERTION_EXPRESSION_EVALUATION_FAILURE(
            "kbs056",
            "Assertion expression ''{0}'' of rule ''{1}'' failed with exception."
        ),
        ;
        private final String code;
        private final String messageTemplate;

        Message(String code, String messageTemplate) {
            this.code = code;
            this.messageTemplate = messageTemplate;
        }

        public String getCode() {
            return code;
        }

        public String getMessageTemplate() {
            return messageTemplate;
        }

    }

    private final Message message;

    private Object[] parameters;

    public SystemMessageBuilder(Message message) {
        this.message = Objects.requireNonNull(message);
    }

    public static SystemMessageBuilder create(Message message) {
        return new SystemMessageBuilder(message);
    }

    public SystemMessageBuilder parameters(Object... parameters) {
        this.parameters = parameters;

        return this;
    }

    public SystemMessage build() {
        String message = DocumentationAppender.append(
            this.message,
            MessageFormat.format(this.message.getMessageTemplate(), this.parameters));


        return new SystemMessage(
            this.message.getCode(),
            message
        );
    }

}
