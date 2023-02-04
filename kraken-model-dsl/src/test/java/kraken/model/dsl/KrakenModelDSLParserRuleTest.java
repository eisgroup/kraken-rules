/*
 *  Copyright 2018 EIS Ltd and/or one of its affiliates.
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
package kraken.model.dsl;

import java.math.BigDecimal;
import java.time.LocalDate;

import kraken.model.Rule;
import kraken.model.ValueList;
import kraken.model.ValueList.DataType;
import kraken.model.derive.DefaultValuePayload;
import kraken.model.derive.DefaultingType;
import kraken.model.dsl.error.LineParseCancellationException;
import kraken.model.payload.PayloadType;
import kraken.model.resource.Resource;
import kraken.model.state.AccessibilityPayload;
import kraken.model.validation.AssertionPayload;
import kraken.model.validation.LengthPayload;
import kraken.model.validation.NumberSetPayload;
import kraken.model.validation.RegExpPayload;
import kraken.model.validation.SizeOrientation;
import kraken.model.validation.SizePayload;
import kraken.model.validation.SizeRangePayload;
import kraken.model.validation.UsagePayload;
import kraken.model.validation.UsageType;
import kraken.model.validation.ValidationSeverity;
import kraken.model.validation.ValueListPayload;

import org.junit.Test;

import static kraken.model.dsl.KrakenDSLModelParser.parseResource;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

/**
 * Rule parsing test for {@link KrakenDSLModelParser} that verifies if {@link Rule} are parsed correctly
 *
 * @author mulevicius
 */
public class KrakenModelDSLParserRuleTest {

    @Test
    public void shouldParseRule() {
        Resource model = parseResource("Rules{Rule 'rule1' On Coverage.limitAmount { Set Disabled }}");

        assertThat(model.getRules(), hasSize(1));
        Rule rule = model.getRules().get(0);

        assertThat(rule.getName(), equalTo("rule1"));
        assertThat(rule.getContext(), equalTo("Coverage"));
        assertThat(rule.getTargetPath(), equalTo("limitAmount"));
        assertThat(rule.getCondition(), nullValue());
        assertThat(rule.getPayload(), instanceOf(AccessibilityPayload.class));
        assertThat(rule.getRuleVariationId(), notNullValue());
        assertThat(rule.getPriority(), nullValue());
        assertThat(rule.getMetadata().getUri(), equalTo(model.getUri()));
    }

    @Test
    public void shouldParseRuleNameWithEscapedCharacters() {
        Resource model = parseResource("Rules{Rule 'Tom\\'s rule' On Coverage.limitAmount { Set Disabled }}");
        assertThat(model.getRules().get(0).getName(), equalTo("Tom's rule"));
    }

    @Test
    public void shouldParseRuleWithCondition() {
        Resource model = parseResource("Rules{Rule 'rule1' On Coverage.limitAmount { When limitAmount = 10 Set Disabled }}");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getCondition().getExpression().getExpressionString(), equalTo("limitAmount = 10"));
    }

    @Test
    public void shouldParseRuleWithDescription() {
        Resource model = parseResource("Rules{Rule 'rule1' On Coverage.limitAmount { Description 'Rule description' When limitAmount = 10 Set Disabled }}");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getDescription(), equalTo("Rule description"));
    }

    @Test
    public void shouldParseRuleWithDescriptionAndPriority() {
        Resource model = parseResource("Rule 'rule1' On Coverage.limitAmount "
            + "{ Description 'Rule description' Priority 1 Default To 100 }");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getDescription(), equalTo("Rule description"));
        assertThat(rule.getPriority(), equalTo(1));
    }

    @Test
    public void shouldParseRuleWithMinPriority() {
        Resource model = parseResource("Rule 'rule1' On Coverage.limitAmount { Priority MIN Default To 100 }");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPriority(), equalTo(Integer.MIN_VALUE));
    }

    @Test
    public void shouldParseRuleWithMaxPriority() {
        Resource model = parseResource("Rule 'rule1' On Coverage.limitAmount { Priority MAX Default To 100 }");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPriority(), equalTo(Integer.MAX_VALUE));
    }

    @Test
    public void shouldParseRuleWithPositivePriority() {
        Resource model = parseResource("Rule 'rule1' On Coverage.limitAmount { Priority 10 Default To 100 }");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPriority(), equalTo(10));
    }

    @Test
    public void shouldParseRuleWithNegativePriority() {
        Resource model = parseResource("Rule 'rule1' On Coverage.limitAmount { Priority -10 Default To 100 }");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPriority(), equalTo(-10));
    }

    @Test
    public void shouldParseUsageEmptyPayload() {
        Resource model = parseResource("Rules { Rule 'rule1' On Coverage.limitAmount { Assert Empty } }");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPayload(), instanceOf(UsagePayload.class));
        assertThat(((UsagePayload) rule.getPayload()).getUsageType(), is(UsageType.mustBeEmpty));
    }

    @Test
    public void shouldParseAssertionPayload() {
        Resource model = parseResource("Rules { Rule 'rule1' On Coverage.limitAmount { Assert limitAmount > 5 Error 'code' : 'error' Overridable} }");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPayload(), instanceOf(AssertionPayload.class));
        assertThat(((AssertionPayload)rule.getPayload()).getAssertionExpression().getExpressionString(), equalTo("limitAmount > 5"));
        assertThat(((AssertionPayload)rule.getPayload()).getErrorMessage().getErrorMessage(), equalTo("error"));
        assertThat(((AssertionPayload)rule.getPayload()).getErrorMessage().getErrorCode(), equalTo("code"));
        assertThat(((AssertionPayload)rule.getPayload()).getSeverity(), is(ValidationSeverity.critical));
        assertThat(((AssertionPayload)rule.getPayload()).isOverridable(), is(true));
    }

    @Test
    public void shouldParseDefaultValuePayload() {
        Resource model = parseResource("Rules{Rule 'rule1' On Coverage.limitAmount { Default To 5+10 }}");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPayload(), instanceOf(DefaultValuePayload.class));
        assertThat(((DefaultValuePayload)rule.getPayload()).getValueExpression().getExpressionString(), equalTo("5+10"));
        assertThat(((DefaultValuePayload)rule.getPayload()).getDefaultingType(), is(DefaultingType.defaultValue));
    }

    @Test
    public void shouldParseAccessibilityPayload() {
        Resource model = parseResource("Rules{Rule 'rule1' On Coverage.limitAmount { Set Disabled }}");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPayload(), instanceOf(AccessibilityPayload.class));
        assertThat(((AccessibilityPayload)rule.getPayload()).isAccessible(), is(false));
    }

    @Test
    public void shouldParseRegExpPayload() {
        Resource model = parseResource("Rules{Rule 'rule1' On Coverage.limitAmount { Assert Matches '[a-z]' Info 'code' : 'info' }}");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPayload(), instanceOf(RegExpPayload.class));
        assertThat(((RegExpPayload)rule.getPayload()).getRegExp(), equalTo("[a-z]"));
        assertThat(((RegExpPayload)rule.getPayload()).getErrorMessage().getErrorMessage(), equalTo("info"));
        assertThat(((RegExpPayload)rule.getPayload()).getErrorMessage().getErrorCode(), equalTo("code"));
        assertThat(((RegExpPayload)rule.getPayload()).getSeverity(), is(ValidationSeverity.info));
    }

    @Test
    public void shouldParseLengthPayload() {
        Resource model = parseResource("Rules{Rule 'rule1' On Coverage.limitAmount { Assert Length 10 Warn 'code' : 'warning' Overridable}}");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPayload(), instanceOf(LengthPayload.class));
        assertThat(((LengthPayload)rule.getPayload()).getLength(), equalTo(10));
        assertThat(((LengthPayload)rule.getPayload()).getErrorMessage().getErrorMessage(), equalTo("warning"));
        assertThat(((LengthPayload)rule.getPayload()).getErrorMessage().getErrorCode(), equalTo("code"));
        assertThat(((LengthPayload)rule.getPayload()).getSeverity(), is(ValidationSeverity.warning));
        assertThat(((LengthPayload)rule.getPayload()).isOverridable(), is(true));
    }

    @Test
    public void shouldParseNumberSetPayload() {
        Resource model = parseResource("Rule 'rule1' On Coverage.limitAmount { Assert Number Min -10.15 Max 20 Step 1.1 }");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPayload(), instanceOf(NumberSetPayload.class));
        assertThat(((NumberSetPayload)rule.getPayload()).getMin(), equalTo(new BigDecimal("-10.15")));
        assertThat(((NumberSetPayload)rule.getPayload()).getMax(), equalTo(new BigDecimal("20")));
        assertThat(((NumberSetPayload)rule.getPayload()).getStep(), equalTo(new BigDecimal("1.1")));
    }

    @Test
    public void shouldParseUsagePayload() {
        Resource model = parseResource("Rules{Rule 'rule1' On Coverage.limitAmount { Set Mandatory Overridable}}");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPayload(), instanceOf(UsagePayload.class));
        assertThat(((UsagePayload)rule.getPayload()).getUsageType(), is(UsageType.mandatory));
        assertThat(((UsagePayload)rule.getPayload()).getErrorMessage(), nullValue());
        assertThat(((UsagePayload)rule.getPayload()).getSeverity(), is(ValidationSeverity.critical));
        assertThat(((UsagePayload)rule.getPayload()).isOverridable(), is(true));
    }

    @Test
    public void shouldParseOverrideGroup() {
        Resource model = parseResource("Rule 'rule1' On Coverage.limitAmount { Set Mandatory Overridable 'group'}");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPayload(), instanceOf(UsagePayload.class));
        assertThat(((UsagePayload)rule.getPayload()).isOverridable(), is(true));
        assertThat(((UsagePayload)rule.getPayload()).getOverrideGroup(), is("group"));
    }

    @Test
    public void shouldParseMinSizeRule() {
        Resource model = parseResource("Rule 'rule1' On Coverage.limits { " +
                "Assert Size  Min 1 " +
                "}");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPayload(), instanceOf(SizePayload.class));
        assertThat(((SizePayload) rule.getPayload()).getOrientation(), is(SizeOrientation.MIN));
        assertThat(((SizePayload) rule.getPayload()).getSize(), is(1));
    }

    @Test
    public void shouldParseMaxSizeRule() {
        Resource model = parseResource("Rule 'rule1' On Coverage.limits { " +
                "Assert Size Max 1 " +
                "}");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPayload(), instanceOf(SizePayload.class));
        assertThat(((SizePayload) rule.getPayload()).getOrientation(), is(SizeOrientation.MAX));
        assertThat(((SizePayload) rule.getPayload()).getSize(), is(1));
    }

    @Test
    public void shouldThrowIfDslIsNotParseable() {
        assertThrows(LineParseCancellationException.class,
                () -> parseResource("Rule 'rule1' On Coverage.limits { Assert Size Max 1"));
    }

    @Test
    public void shouldParseCollectionRangeRule() {
        Resource model = parseResource("Rule 'rule1' On Coverage.limits { " +
                "Assert Size Min 1 Max 10" +
                "}");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPayload(), instanceOf(SizeRangePayload.class));
        assertThat(((SizeRangePayload) rule.getPayload()).getMin(), is(1));
        assertThat(((SizeRangePayload) rule.getPayload()).getMax(), is(10));
    }

    @Test
    public void shouldParseSizeEqualsRule() {
        Resource model = parseResource("Rule 'rule1' On Coverage.limits { " +
                "Assert Size 1 " +
                "}");

        Rule rule = model.getRules().get(0);
        assertThat(rule.getPayload(), instanceOf(SizePayload.class));
        assertThat(((SizePayload) rule.getPayload()).getOrientation(), is(SizeOrientation.EQUALS));
        assertThat(((SizePayload) rule.getPayload()).getSize(), is(1));
    }

    @Test
    public void shouldParseValueListRuleForStringValue() {
        Resource model = parseResource("Rule 'valueListStrings' On Coverage.code { " +
            "Assert In \"MED\", \"COLL\" " +
            "}");

        Rule rule = model.getRules().get(0);

        assertNotNull(rule.getPayload());
        assertThat(rule.getPayload(), instanceOf(ValueListPayload.class));

        ValueListPayload valueListPayload = (ValueListPayload) rule.getPayload();

        assertThat(valueListPayload.getPayloadType(), is(PayloadType.VALUE_LIST));
        assertThat(valueListPayload.getSeverity(), is(ValidationSeverity.critical));
        assertThat(valueListPayload.isOverridable(), is(false));

        assertNotNull(valueListPayload.getValueList());

        ValueList valueList = valueListPayload.getValueList();

        assertThat(valueList.getValueType(), is(DataType.STRING));
        assertThat(valueList.getValues(), containsInAnyOrder("MED", "COLL"));
    }

    @Test
    public void shouldParseValueListRuleForDecimalValue() {
        Resource model = parseResource("Rule 'valueListStrings' On Coverage.limitAmt { " +
            "Assert In 10, 500.0, 52.5" +
            "}");

        Rule rule = model.getRules().get(0);

        assertNotNull(rule.getPayload());
        assertThat(rule.getPayload(), instanceOf(ValueListPayload.class));

        ValueListPayload valueListPayload = (ValueListPayload) rule.getPayload();

        assertThat(valueListPayload.getPayloadType(), is(PayloadType.VALUE_LIST));
        assertThat(valueListPayload.getSeverity(), is(ValidationSeverity.critical));
        assertThat(valueListPayload.isOverridable(), is(false));

        assertNotNull(valueListPayload.getValueList());

        ValueList valueList = valueListPayload.getValueList();

        assertThat(valueList.getValueType(), is(DataType.DECIMAL));
        assertThat(valueList.getValues(),
            containsInAnyOrder(BigDecimal.valueOf(10), BigDecimal.valueOf(500.0), BigDecimal.valueOf(52.5)));
    }

    @Test
    public void shouldThrowExceptionIfValueListRuleCannotBeParsed() {
        assertThrows(LineParseCancellationException.class, () -> {
            parseResource("Rule 'valueListStrings' On Coverage.limitAmt { " +
                "Assert In 10, 500.0, \"MED\"" +
                "}");
        });
    }

    @Test
    public void shouldParseDimensions() {
        Resource model = parseResource(
                    "@Dimension('packageCd', 'pizza')" +
                        "@Dimension('version', 1)" +
                        "@Dimension('effectiveDate', Date('2018-01-01')) " +
                        "Rules{" +
                        "Rule 'rule1' On RiskItem.itemName {Set Disabled}" +
                        "Rule 'rule2' On RiskItem.itemName {Set Hidden}" +
                        "}");
        assertThat(model.getRules(), hasSize(2));
        assertThat(model.getRules().get(0).getMetadata().getProperty("packageCd"), equalTo("pizza"));
        assertThat(model.getRules().get(0).getMetadata().getProperty("version").toString(),  equalTo("1"));
        assertThat(model.getRules().get(0).getMetadata().getProperty("effectiveDate"), equalTo(LocalDate.of(2018, 1, 1)));
        assertThat(model.getRules().get(1).getMetadata().getProperty("packageCd"), equalTo("pizza"));
        assertThat(model.getRules().get(1).getMetadata().getProperty("version").toString(), equalTo("1"));
        assertThat(model.getRules().get(1).getMetadata().getProperty("effectiveDate"), equalTo(LocalDate.of(2018, 1, 1)));
    }
}
