package kraken.model.dsl.converter;

import kraken.el.ast.builder.Literals;
import kraken.model.*;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.derive.DefaultingType;
import kraken.model.entrypoint.EntryPoint;
import kraken.model.factory.RulesModelFactory;
import kraken.model.resource.Resource;
import kraken.model.resource.builder.ResourceBuilder;
import kraken.model.state.AccessibilityPayload;
import kraken.model.state.VisibilityPayload;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.LengthPayload;
import kraken.model.validation.RegExpPayload;
import kraken.model.validation.SizeOrientation;
import kraken.model.validation.SizePayload;
import kraken.model.validation.SizeRangePayload;
import kraken.model.validation.UsagePayload;
import kraken.model.validation.UsageType;
import kraken.model.validation.ValidationSeverity;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertEquals;

public class RuleConversionTest {

    private static final RulesModelFactory factory = RulesModelFactory.getInstance();
    private static final String n = System.lineSeparator();

    private final DSLModelConverter converter = new DSLModelConverter();

    @Test
    public void shouldConvertAssertionRule(){
        String convertedRule = convert(createSimpleAssertionRule());
        assertEquals(
                        "Rule \"Driver_limitAmount_rule\" On Driver.limitAmount {" +
                        System.lineSeparator() +
                        "    Assert limitAmount.state = \"CA\"" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertRuleWithDescription() {
        Rule rule = factory.createRule();
        rule.setDescription("A rule which asserts a state of limit amount");
        rule.setPayload(createAssertionPayload("limitAmount.state = \"CA\""));
        rule.setContext("Driver");
        rule.setTargetPath("limitAmount");
        rule.setName("Driver_limitAmount_rule");
        rule.setPhysicalNamespace("whatever");

        String convertedRule = convert(rule);
        assertEquals(
                "Rule \"Driver_limitAmount_rule\" On Driver.limitAmount {" +
                        System.lineSeparator() +
                        "    Description \"A rule which asserts a state of limit amount\"" +
                        System.lineSeparator() +
                        "    Assert limitAmount.state = \"CA\"" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertAssertionRuleWithDimension(){
        final Rule simpleAssertionRule = createSimpleAssertionRule();
        final Metadata metadata = RulesModelFactory.getInstance().createMetadata();
        metadata.setProperty("Package", "cd");
        metadata.setProperty("Code", "js");
        simpleAssertionRule.setMetadata(metadata);
        String convertedRule = convert(simpleAssertionRule);
        assertEquals(
                        "@Dimension(\"Package\", \"cd\")" + System.lineSeparator() +
                        "@Dimension(\"Code\", \"js\")" + System.lineSeparator() +
                        "Rule \"Driver_limitAmount_rule\" On Driver.limitAmount {" +
                        System.lineSeparator() +
                        "    Assert limitAmount.state = \"CA\"" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertDimension_boolean(){
        final Rule simpleAssertionRule = createSimpleAssertionRule();
        final Metadata metadata = RulesModelFactory.getInstance().createMetadata();
        metadata.setProperty("boolean", false);
        simpleAssertionRule.setMetadata(metadata);
        String convertedRule = convert(simpleAssertionRule);
        assertEquals(
                        "@Dimension(\"boolean\", false)" + n +
                        "Rule \"Driver_limitAmount_rule\" On Driver.limitAmount {" + n +
                        "    Assert limitAmount.state = \"CA\"" + n +
                        "}" + n + n,
                convertedRule
        );
    }

    @Test
    public void shouldConvertDimension_decimal(){
        final Rule simpleAssertionRule = createSimpleAssertionRule();
        final Metadata metadata = RulesModelFactory.getInstance().createMetadata();
        metadata.setProperty("decimal", BigDecimal.valueOf(1.1D));
        simpleAssertionRule.setMetadata(metadata);
        String convertedRule = convert(simpleAssertionRule);
        assertEquals(
                        "@Dimension(\"decimal\", 1.1)" + n +
                        "Rule \"Driver_limitAmount_rule\" On Driver.limitAmount {" + n +
                        "    Assert limitAmount.state = \"CA\"" + n +
                        "}" + n + n,
                convertedRule
        );
    }

    @Test
    public void shouldConvertDimension_number(){
        final Rule simpleAssertionRule = createSimpleAssertionRule();
        final Metadata metadata = RulesModelFactory.getInstance().createMetadata();
        metadata.setProperty("number", BigDecimal.valueOf(1));
        simpleAssertionRule.setMetadata(metadata);
        String convertedRule = convert(simpleAssertionRule);
        assertEquals(
                        "@Dimension(\"number\", 1)" + n +
                        "Rule \"Driver_limitAmount_rule\" On Driver.limitAmount {" + n +
                        "    Assert limitAmount.state = \"CA\"" + n +
                        "}" + n + n,
                convertedRule
        );
    }

    @Test
    public void shouldConvertDimension_date(){
        final Rule simpleAssertionRule = createSimpleAssertionRule();
        final Metadata metadata = RulesModelFactory.getInstance().createMetadata();
        metadata.setProperty("date", LocalDate.of(2020,2,2));
        simpleAssertionRule.setMetadata(metadata);
        String convertedRule = convert(simpleAssertionRule);
        assertEquals(
                        "@Dimension(\"date\", 2020-02-02)" + n +
                        "Rule \"Driver_limitAmount_rule\" On Driver.limitAmount {" + n +
                        "    Assert limitAmount.state = \"CA\"" + n +
                        "}" + n + n,
                convertedRule
        );
    }

    @Test
    public void shouldConvertDimension_datetime(){
        final Rule simpleAssertionRule = createSimpleAssertionRule();
        final Metadata metadata = RulesModelFactory.getInstance().createMetadata();
        metadata.setProperty("datetime", Literals.getDateTime("2020-02-02T01:01:01Z"));
        simpleAssertionRule.setMetadata(metadata);
        String convertedRule = convert(simpleAssertionRule);
        assertEquals(
                        "@Dimension(\"datetime\", 2020-02-02T01:01:01Z)" + n +
                        "Rule \"Driver_limitAmount_rule\" On Driver.limitAmount {" + n +
                        "    Assert limitAmount.state = \"CA\"" + n +
                        "}" + n + n,
                convertedRule
        );
    }

    @Test
    public void shouldConvertDimension_money() {
        final Rule simpleAssertionRule = createSimpleAssertionRule();
        final Metadata metadata = RulesModelFactory.getInstance().createMetadata();
        metadata.setProperty("money", 200);
        simpleAssertionRule.setMetadata(metadata);
        String convertedRule = convert(simpleAssertionRule);
        assertEquals(
                        "@Dimension(\"money\", 200)" + n +
                        "Rule \"Driver_limitAmount_rule\" On Driver.limitAmount {" + n +
                        "    Assert limitAmount.state = \"CA\"" + n +
                        "}" + n + n,
                convertedRule
        );
    }

    @Test
    public void shouldConvertDimension_null(){
        final Rule simpleAssertionRule = createSimpleAssertionRule();
        final Metadata metadata = RulesModelFactory.getInstance().createMetadata();
        metadata.setProperty("null", null);
        simpleAssertionRule.setMetadata(metadata);
        String convertedRule = convert(simpleAssertionRule);
        assertEquals(
                        "Rule \"Driver_limitAmount_rule\" On Driver.limitAmount {" + n +
                        "    Assert limitAmount.state = \"CA\"" + n +
                        "}" + n + n,
                convertedRule
        );
    }

    @Test
    public void shouldConvertEntryPointWithDimension(){
        final EntryPoint entryPoint = factory.createEntryPoint();
        entryPoint.setIncludedEntryPointNames(List.of("Include A", "Include B"));
        entryPoint.setRuleNames(List.of("Rule A", "Rule B"));
        final Metadata metadata = factory.createMetadata();
        metadata.setProperty("Package", "cd");
        metadata.setProperty("Code", "js");
        entryPoint.setMetadata(metadata);
        entryPoint.setName("Validate");

        String convertedRule = convert(entryPoint);
        assertEquals(
                        "@Dimension(\"Package\", \"cd\")" + System.lineSeparator() +
                        "@Dimension(\"Code\", \"js\")" + System.lineSeparator() +
                        "EntryPoint \"Validate\" {" + System.lineSeparator() +
                        "    EntryPoint \"Include A\"," + System.lineSeparator() +
                        "    EntryPoint \"Include B\"," + System.lineSeparator() +
                        "    \"Rule A\"," + System.lineSeparator() +
                        "    \"Rule B\"" + System.lineSeparator() +
                        "}" + System.lineSeparator() + System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertEntryPointWithoutIncludes(){
        final EntryPoint entryPoint = factory.createEntryPoint();
        entryPoint.setRuleNames(List.of("Rule A"));
        entryPoint.setName("Validate");

        String convertedRule = convert(entryPoint);
        assertEquals(
                        "EntryPoint \"Validate\" {" + System.lineSeparator() +
                        "    \"Rule A\"" + System.lineSeparator() +
                        "}" + System.lineSeparator() + System.lineSeparator(),
                convertedRule
        );
    }

    private String convert(EntryPoint entryPoint){
        Resource model = ResourceBuilder.getInstance()
                .addEntryPoint(entryPoint)
                .build();
        return converter.convert(model);
    }


    private String convert(Rule rule){
        Resource model = ResourceBuilder.getInstance()
                .addRule(rule)
                .build();
        return converter.convert(model);
    }

    private String convert(List<Rule> rules){
        Resource model = ResourceBuilder.getInstance()
                .addRules(rules)
                .build();
        return converter.convert(model);
    }

    @Test
    public void shouldConvertAssertionRuleWithConditionAndSeverity(){
        String convertedRule = convert(createAssertionRule());
        assertEquals(
                        "Rule \"Driver_limitAmount_rule\" On Driver.limitAmount {" +
                        System.lineSeparator() +
                        "    When Count(riskItem[*].location) > 5 && zip = \"DGD\""+
                        System.lineSeparator() +
                        "    Assert limitAmount.state = \"CA\"" +
                        System.lineSeparator() +
                        "    Error \"\\\"1313\\\"\" : \"Error in \\\"AssertionRule\\\"\"" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertRegExpRule(){
        String convertedRule = convert(createRegExpRule("^[a-zA-Z]*"));
        assertEquals(
                        "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Assert Matches \"^[a-zA-Z]*\"" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertRegExpRuleWithConditionAndSeverity() {
        Rule rule = factory.createRule();
        rule.setCondition(createCondition("Condition == null"));
        RegExpPayload payload = createRegExpPayload("^[a-zA-Z]*");
        payload.setErrorMessage(createErrorMessage("code", "message"));
        rule.setPayload(payload);
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");

        String convertedRule = convert(rule);
        assertEquals(
                "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    When Condition == null" +
                        System.lineSeparator() +
                        "    Assert Matches \"^[a-zA-Z]*\"" +
                        System.lineSeparator() +
                        "    Error \"code\" : \"message\"" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertRegExpRuleWithDescriptionAndOverridable() {
        Rule rule = factory.createRule();
        rule.setDescription("A RegExp rule. Can be overridden");
        RegExpPayload payload = factory.createRegExpPayload();
        payload.setRegExp("^[a-zA-Z]*");
        payload.setOverridable(true);
        rule.setPayload(payload);
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");

        String convertedRule = convert(rule);
        assertEquals(
                "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Description \"A RegExp rule. Can be overridden\"" +
                        System.lineSeparator() +
                        "    Assert Matches \"^[a-zA-Z]*\"" +
                        System.lineSeparator() +
                        "    Overridable" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertLengthRule(){
        String convertedRule = convert(createSimpleLengthRule(15));
        assertEquals(
                        "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Assert Length 15" +
                        System.lineSeparator() +
                        "    Overridable" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertLengthRuleWithDescriptionAndValidationMessage() {
        Rule rule = factory.createRule();
        LengthPayload lengthPayload = createLengthPayload(15);
        lengthPayload.setOverridable(false);
        lengthPayload.setErrorMessage(createErrorMessage("code", "Not able to create length rule"));
        rule.setPayload(lengthPayload);
        rule.setDescription("Rule for length");
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");

        String convertedRule = convert(rule);
        assertEquals(
                "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Description \"Rule for length\"" +
                        System.lineSeparator() +
                        "    Assert Length 15" +
                        System.lineSeparator() +
                        "    Error \"code\" : \"Not able to create length rule\"" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertLengthRuleWithConditionAndSeverity(){
        String convertedRule = convert(createLengthRule(15));
        assertEquals(
                        "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    When Length rule condition" +
                        System.lineSeparator() +
                        "    Assert Length 15" +
                        System.lineSeparator() +
                        "    Error \"code\"" +
                        System.lineSeparator() +
                        "    Overridable" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertUsageRules(){
        String rules = convert(Arrays.asList(
                createUsageRule(UsageType.mandatory, ValidationSeverity.critical),
                createUsageRule(UsageType.mustBeEmpty, ValidationSeverity.info)
        ));
        assertEquals(
                        "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Set Mandatory" +
                        System.lineSeparator() +
                        "    Error \"code\" : \"message\"" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator() +
                        "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Assert Empty" +
                        System.lineSeparator() +
                        "    Info \"code\" : \"message\"" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                rules
        );
    }

    @Test
    public void shouldConvertUsageRuleMandatoryWithDescriptionConditionAndOverridable() {
        UsagePayload payload = factory.createUsagePayload();
        payload.setSeverity(ValidationSeverity.warning);
        payload.setUsageType(UsageType.mandatory);
        payload.setOverridable(true);
        payload.setErrorMessage(createErrorMessage("code", "message"));

        Rule rule = factory.createRule();
        rule.setPayload(payload);
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");
        rule.setDescription("Mandatory rule");
        rule.setCondition(createCondition("Driver.riskItem > 10"));
        String convertedRule = convert(rule);
        assertEquals(
                "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Description \"Mandatory rule\"" +
                        System.lineSeparator() +
                        "    When Driver.riskItem > 10" +
                        System.lineSeparator() +
                        "    Set Mandatory" +
                        System.lineSeparator() +
                        "    Warn \"code\" : \"message\"" +
                        System.lineSeparator() +
                        "    Overridable" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertUsageRuleEmptyWithDescriptionConditionAndOverridable() {
        UsagePayload payload = factory.createUsagePayload();
        payload.setSeverity(ValidationSeverity.info);
        payload.setUsageType(UsageType.mustBeEmpty);
        payload.setOverridable(true);
        payload.setErrorMessage(createErrorMessage("code", "message"));

        Rule rule = factory.createRule();
        rule.setPayload(payload);
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");
        rule.setDescription("Mandatory rule");
        rule.setCondition(createCondition("Driver.riskItem == null || Driver.riskItem == 0"));
        String convertedRule = convert(rule);
        assertEquals(
                "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Description \"Mandatory rule\"" +
                        System.lineSeparator() +
                        "    When Driver.riskItem == null || Driver.riskItem == 0" +
                        System.lineSeparator() +
                        "    Assert Empty" +
                        System.lineSeparator() +
                        "    Info \"code\" : \"message\"" +
                        System.lineSeparator() +
                        "    Overridable" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertVisibilityRule(){
        String convertedRule = convert(createVisibilityRule());
        assertEquals(
                        "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Set Hidden" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertVisibilityRuleWithDescriptionAndCondition() {
        Rule rule = factory.createRule();
        rule.setPayload(createVisibilityPayload());
        rule.setDescription("Visibility rule");
        rule.setCondition(createCondition("applicable = true"));
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");

        String convertedRule = convert(rule);
        assertEquals(
                "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Description \"Visibility rule\"" +
                        System.lineSeparator() +
                        "    When applicable = true" +
                        System.lineSeparator() +
                        "    Set Hidden" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertAccessibilityRule(){
        String convertedRule = convert(createAccessibilityRule());
        assertEquals(
                        "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Set Disabled" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertAccessibilityRuleWithDescriptionAndCondition() {
        Rule rule = factory.createRule();
        rule.setPayload(createAccessibilityPayload());
        rule.setDescription("Accessibility rule");
        rule.setCondition(createCondition("applicable = true"));
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");

        String convertedRule = convert(rule);
        assertEquals(
                "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Description \"Accessibility rule\"" +
                        System.lineSeparator() +
                        "    When applicable = true" +
                        System.lineSeparator() +
                        "    Set Disabled" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertRulesCollection() {
        String rules = convert(Arrays.asList(
                createDefaultRule(DefaultingType.defaultValue, "null"),
                createDefaultRule(DefaultingType.resetValue, "20")
        ));

        assertEquals(
                        "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Default To null" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator() +
                        "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Reset To 20" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                rules
        );
    }

    @Test
    public void shouldConvertDefaultRules(){
        String rules = convert(Arrays.asList(
                createDefaultRule(DefaultingType.defaultValue,"null"),
                createDefaultRule(DefaultingType.resetValue, "20")
        ));
        assertEquals(
                        "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Default To null" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator() +
                        "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Reset To 20" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                rules
        );
    }

    @Test
    public void shouldConvertDefaultRuleWithPriority(){
        var rule = createDefaultRule(DefaultingType.defaultValue, "10");
        rule.setPriority(10);
        String rules = convert(List.of(rule));
        assertEquals(
            "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                System.lineSeparator() +
                "    Priority 10" +
                System.lineSeparator() +
                "    Default To 10" +
                System.lineSeparator() +
                "}" +
                System.lineSeparator() +
                System.lineSeparator(),
            rules
        );
    }

    @Test
    public void shouldConvertDefaultRuleWithDescriptionAndCondition() {
        Rule rule = factory.createRule();
        rule.setPayload(createDefaultPayload(DefaultingType.defaultValue, "200"));
        rule.setDescription("Default to rule");
        rule.setCondition(createCondition("riskItems > 5"));
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");

        String rules = convert(rule);
        assertEquals(
                "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Description \"Default to rule\"" +
                        System.lineSeparator() +
                        "    When riskItems > 5" +
                        System.lineSeparator() +
                        "    Default To 200" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                rules
        );
    }

    @Test
    public void shouldConvertDefaultResetRuleWithDescriptionAndCondition() {
        Rule rule = factory.createRule();
        rule.setPayload(createDefaultPayload(DefaultingType.resetValue, "200"));
        rule.setDescription("Reset To rule");
        rule.setCondition(createCondition("riskItems > 5"));
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");

        String rules = convert(rule);
        assertEquals(
                "Rule \"Driver_riskItem_rule\" On Driver.riskItem {" +
                        System.lineSeparator() +
                        "    Description \"Reset To rule\"" +
                        System.lineSeparator() +
                        "    When riskItems > 5" +
                        System.lineSeparator() +
                        "    Reset To 200" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                rules
        );
    }

    @Test
    public void shouldConvertSizeMinRuleWithDescriptionAndCondition() {

        Rule rule = factory.createRule();
        rule.setPayload(createSizePayload(SizeOrientation.MIN, 20, ValidationSeverity.info));
        rule.setDescription("Size rule");
        rule.setCondition(createCondition("riskItems > 5"));
        rule.setContext("Driver");
        rule.setTargetPath("riskItems");
        rule.setName("Driver_riskItems_rule");

        String convertedRule = convert(rule);
        assertEquals(
                "Rule \"Driver_riskItems_rule\" On Driver.riskItems {" +
                        System.lineSeparator() +
                        "    Description \"Size rule\"" +
                        System.lineSeparator() +
                        "    When riskItems > 5" +
                        System.lineSeparator() +
                        "    Assert Size Min 20" +
                        System.lineSeparator() +
                        "    Info \"SizeCode\" : \"SizeMessage\"" +
                        System.lineSeparator() +
                        "    Overridable \"OverrideGroup\"" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertSizeMaxRuleWithDescriptionAndCondition() {
        Rule rule = factory.createRule();
        rule.setPayload(createSizePayload(SizeOrientation.MAX, 20, ValidationSeverity.critical));
        rule.setDescription("Size rule");
        rule.setCondition(createCondition("Driver.age > 21"));
        rule.setContext("Driver");
        rule.setTargetPath("riskItems");
        rule.setName("Driver_riskItems_rule");

        String convertedRule = convert(rule);
        assertEquals(
                "Rule \"Driver_riskItems_rule\" On Driver.riskItems {" +
                        System.lineSeparator() +
                        "    Description \"Size rule\"" +
                        System.lineSeparator() +
                        "    When Driver.age > 21" +
                        System.lineSeparator() +
                        "    Assert Size Max 20" +
                        System.lineSeparator() +
                        "    Error \"SizeCode\" : \"SizeMessage\"" +
                        System.lineSeparator() +
                        "    Overridable \"OverrideGroup\"" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertSizeRuleWithDescriptionAndCondition() {
        Rule rule = factory.createRule();
        rule.setPayload(createSizePayload(SizeOrientation.EQUALS, 20, ValidationSeverity.warning));
        rule.setDescription("Size rule");
        rule.setCondition(createCondition("Driver.age = 21"));
        rule.setContext("Driver");
        rule.setTargetPath("riskItems");
        rule.setName("Driver_riskItems_rule");

        String convertedRule = convert(rule);
        assertEquals(
                "Rule \"Driver_riskItems_rule\" On Driver.riskItems {" +
                        System.lineSeparator() +
                        "    Description \"Size rule\"" +
                        System.lineSeparator() +
                        "    When Driver.age = 21" +
                        System.lineSeparator() +
                        "    Assert Size 20" +
                        System.lineSeparator() +
                        "    Warn \"SizeCode\" : \"SizeMessage\"" +
                        System.lineSeparator() +
                        "    Overridable \"OverrideGroup\"" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertSizeRangeRuleWithDescriptionAndCondition() {
        Rule rule = factory.createRule();
        rule.setPayload(createSizeRangePayload(20, 55, ValidationSeverity.critical));
        rule.setDescription("Size rule");
        rule.setCondition(createCondition("Driver.age > 60"));
        rule.setContext("Driver");
        rule.setTargetPath("riskItems");
        rule.setName("Driver_riskItems_rule");

        String convertedRule = convert(rule);
        assertEquals(
                "Rule \"Driver_riskItems_rule\" On Driver.riskItems {" +
                        System.lineSeparator() +
                        "    Description \"Size rule\"" +
                        System.lineSeparator() +
                        "    When Driver.age > 60" +
                        System.lineSeparator() +
                        "    Assert Size Min 20 Max 55" +
                        System.lineSeparator() +
                        "    Error \"SizeCode\" : \"SizeMessage\"" +
                        System.lineSeparator() +
                        "    Overridable \"OverrideGroup\"" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                convertedRule
        );
    }

    @Test
    public void shouldConvertSizeRules(){
        String rules = convert(Arrays.asList(
                createSizeRule(SizeOrientation.MAX, 20, ValidationSeverity.info),
                createSizeRule(SizeOrientation.MIN, 20, ValidationSeverity.critical),
                createSizeRule(SizeOrientation.EQUALS, 20, ValidationSeverity.warning),
                createSizeRangeRule(20, 55, ValidationSeverity.info)
        ));
        assertEquals(
                        "Rule \"Driver_riskItems_rule\" On Driver.riskItems {" +
                        System.lineSeparator() +
                        "    Assert Size Max 20" +
                        System.lineSeparator() +
                        "    Info \"SizeCode\" : \"SizeMessage\"" +
                        System.lineSeparator() +
                        "    Overridable \"OverrideGroup\"" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator() +
                        "Rule \"Driver_riskItems_rule\" On Driver.riskItems {" +
                        System.lineSeparator() +
                        "    Assert Size Min 20" +
                        System.lineSeparator() +
                        "    Error \"SizeCode\" : \"SizeMessage\"" +
                        System.lineSeparator() +
                        "    Overridable \"OverrideGroup\"" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator() +
                        "Rule \"Driver_riskItems_rule\" On Driver.riskItems {" +
                        System.lineSeparator() +
                        "    Assert Size 20" +
                        System.lineSeparator() +
                        "    Warn \"SizeCode\" : \"SizeMessage\"" +
                        System.lineSeparator() +
                        "    Overridable \"OverrideGroup\"" +
                        System.lineSeparator() +
                        "}" +
                        System.lineSeparator() +
                        "Rule \"Driver_riskItems_rule\" On Driver.riskItems {" +
                        System.lineSeparator() +
                        "    Assert Size Min 20 Max 55" +
                        System.lineSeparator() +
                        "    Info \"SizeCode\" : \"SizeMessage\"" +
                        System.lineSeparator() +
                        "    Overridable \"OverrideGroup\"" +
                        System.lineSeparator() +
                        "}" + System.lineSeparator() +
                        System.lineSeparator(),
                rules
        );
    }

    @Test
    public void shouldConvertRuleWithDoubleQuotes(){
        Rule rule = factory.createRule();
        rule.setName("double\"quote");
        rule.setContext("Context");
        rule.setTargetPath("attribute");
        rule.setDescription("double\"quote");

        AssertionPayload payload = factory.createAssertionPayload();
        Expression expression = factory.createExpression();
        expression.setExpressionString("code = \"double\\\"quote\"");
        payload.setAssertionExpression(expression);
        payload.setOverridable(true);
        payload.setOverrideGroup("double\"quote");
        ErrorMessage errorMessage = factory.createErrorMessage();
        errorMessage.setErrorCode("double\"quote");
        errorMessage.setErrorMessage("double\"quote");
        payload.setErrorMessage(errorMessage);
        payload.setSeverity(ValidationSeverity.critical);
        rule.setPayload(payload);
        Metadata metadata = factory.createMetadata();
        metadata.setProperty("Package", "double\"quote");
        rule.setMetadata(metadata);

        String convertedRule = convert(rule);
        assertEquals(
            "@Dimension(\"Package\", \"double\\\"quote\")" + System.lineSeparator() +
            "Rule \"double\\\"quote\" On Context.attribute {" +
                System.lineSeparator() +
                "    Description \"double\\\"quote\"" +
                System.lineSeparator() +
                "    Assert code = \"double\\\"quote\"" +
                System.lineSeparator() +
                "    Error \"double\\\"quote\" : \"double\\\"quote\"" +
                System.lineSeparator() +
                "    Overridable \"double\\\"quote\"" +
                System.lineSeparator() +
                "}" + System.lineSeparator() +
                System.lineSeparator(),
            convertedRule
        );
    }

    private Rule createRegExpRule(String regExp) {
        Rule rule = factory.createRule();
        rule.setPayload(createRegExpPayload(regExp));
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");
        return rule;
    }

    private Rule createSimpleLengthRule(int length) {
        Rule rule = factory.createRule();
        rule.setPayload(createLengthPayload(length));
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");
        return rule;
    }

    private Rule createLengthRule(int length) {
        Rule rule = factory.createRule();
        LengthPayload lengthPayload = createLengthPayload(length);
        lengthPayload.setErrorMessage(createErrorMessage("code", null));
        rule.setPayload(lengthPayload);
        rule.setCondition(createCondition("Length rule condition"));
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");
        return rule;
    }

    private RegExpPayload createRegExpPayload(String regExp) {
        RegExpPayload payload = factory.createRegExpPayload();
        payload.setRegExp(regExp);
        payload.setOverrideGroup("OverridableGroup");
        return payload;
    }

    private LengthPayload createLengthPayload(int length) {
        LengthPayload payload = factory.createLengthPayload();
        payload.setLength(length);
        payload.setOverridable(true);
        return payload;
    }

    private Rule createUsageRule(UsageType usageType, ValidationSeverity severity) {
        Rule rule = factory.createRule();
        rule.setPayload(createUsagePayload(usageType, severity));
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");
        return rule;
    }

    private Payload createUsagePayload(UsageType usageType, ValidationSeverity severity) {
        UsagePayload payload = factory.createUsagePayload();
        payload.setSeverity(severity);
        payload.setUsageType(usageType);
        payload.setOverridable(false);
        payload.setErrorMessage(createErrorMessage("code", "message"));
        return payload;
    }

    private Rule createVisibilityRule() {
        Rule rule = factory.createRule();
        rule.setPayload(createVisibilityPayload());
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");
        return rule;
    }

    private Payload createVisibilityPayload() {
        VisibilityPayload payload = factory.createVisibilityPayload();
        payload.setVisible(true);
        return payload;
    }

    private Rule createAccessibilityRule() {
        Rule rule = factory.createRule();
        rule.setPayload(createAccessibilityPayload());
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");
        return rule;
    }

    private Payload createAccessibilityPayload() {
        AccessibilityPayload payload = factory.createAccessibilityPayload();
        payload.setAccessible(true);
        return payload;
    }

    private Rule createDefaultRule(DefaultingType defaultingType, String defaultExpression) {
        Rule rule = factory.createRule();
        rule.setPayload(createDefaultPayload(defaultingType, defaultExpression));
        rule.setContext("Driver");
        rule.setTargetPath("riskItem");
        rule.setName("Driver_riskItem_rule");
        return rule;
    }

    private Payload createDefaultPayload(DefaultingType defaultingType, String defaultExpression) {
        DefaultValuePayload payload = factory.createDefaultValuePayload();
        payload.setDefaultingType(defaultingType);
        payload.setValueExpression(createExpression(defaultExpression));
        return payload;
    }


    private Rule createSizeRule(SizeOrientation orientation, Integer size, ValidationSeverity severity){
        Rule rule = factory.createRule();
        rule.setPayload(createSizePayload(orientation, size, severity));
        rule.setContext("Driver");
        rule.setTargetPath("riskItems");
        rule.setName("Driver_riskItems_rule");
        return rule;
    }

    private Rule createSizeRangeRule(Integer minSize, Integer maxSize, ValidationSeverity severity){
        Rule rule = factory.createRule();
        rule.setPayload(createSizeRangePayload(minSize, maxSize, severity));
        rule.setContext("Driver");
        rule.setTargetPath("riskItems");
        rule.setName("Driver_riskItems_rule");
        return rule;
    }

    private Payload createSizePayload(SizeOrientation orientation, Integer size, ValidationSeverity severity) {
        SizePayload payload = factory.createSizePayload();
        payload.setOverrideGroup("OverrideGroup");
        payload.setOverridable(true);
        payload.setErrorMessage(createErrorMessage("SizeCode", "SizeMessage"));
        payload.setSeverity(severity);
        payload.setOrientation(orientation);
        payload.setSize(size);
        return payload;
    }

    private Payload createSizeRangePayload(Integer minSize, Integer maxSize, ValidationSeverity severity) {
        SizeRangePayload payload = factory.createSizeRangePayload();
        payload.setOverrideGroup("OverrideGroup");
        payload.setOverridable(true);
        payload.setErrorMessage(createErrorMessage("SizeCode", "SizeMessage"));
        payload.setSeverity(severity);
        payload.setMin(minSize);
        payload.setMax(maxSize);
        return payload;
    }

    private Rule createSimpleAssertionRule(){
        Rule rule = factory.createRule();
        rule.setPayload(createAssertionPayload("limitAmount.state = \"CA\""));
        rule.setContext("Driver");
        rule.setTargetPath("limitAmount");
        rule.setName("Driver_limitAmount_rule");
        rule.setPhysicalNamespace("whatever");
        return rule;
    }

    private Rule createAssertionRule(){
        Rule rule = factory.createRule();
        rule.setCondition(createCondition("Count(riskItem[*].location) > 5 && zip = \"DGD\""));
        AssertionPayload assertionPayload = createAssertionPayload("limitAmount.state = \"CA\"");
        assertionPayload.setErrorMessage(createErrorMessage("\"1313\"", "Error in \"AssertionRule\""));
        rule.setPayload(assertionPayload);
        rule.setContext("Driver");
        rule.setTargetPath("limitAmount");
        rule.setName("Driver_limitAmount_rule");
        rule.setPhysicalNamespace("whatever");
        return rule;
    }

    private AssertionPayload createAssertionPayload(String assertionString){
        AssertionPayload payload = factory.createAssertionPayload();
        payload.setAssertionExpression(createExpression(assertionString));
        return payload;
    }

    private Condition createCondition(String conditionString){
        Condition condition = factory.createCondition();
        condition.setExpression(createExpression(conditionString));
        return condition;
    }

    private Expression createExpression(String expresssion){
        Expression expression = factory.createExpression();
        expression.setExpressionString(expresssion);
        return expression;
    }

    private ErrorMessage createErrorMessage(String code, String msg){
        ErrorMessage errorMessage = factory.createErrorMessage();
        errorMessage.setErrorCode(code);
        errorMessage.setErrorMessage(msg);
        return errorMessage;
    }
}
