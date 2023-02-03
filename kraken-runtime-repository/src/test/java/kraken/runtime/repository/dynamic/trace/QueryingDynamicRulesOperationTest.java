/*
 * Copyright © 2022 EIS Group and/or one of its affiliates. All rights reserved. Unpublished work under U.S.
 * copyright laws.
 * CONFIDENTIAL AND TRADE SECRET INFORMATION. No portion of this work may be copied, distributed, modified,
 *  or incorporated into any other media without EIS Group prior written consent.
 */
package kraken.runtime.repository.dynamic.trace;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import kraken.runtime.model.rule.RuntimeRule;

/**
 * Unit tests for {@code QueryingDynamicRulesOperation} class.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public class QueryingDynamicRulesOperationTest {

    @Test
    public void shouldCreateCorrectDescriptionForQueryingDynamicRepository() {
        var dynamicRuleOp = new QueryingDynamicRulesOperation("dynamicRepository");

        assertThat(dynamicRuleOp.describe(),
            is("Querying 'dynamicRepository' repository for dynamic rules."));
    }

    @Test
    public void shouldCreateCorrectDescriptionForQueriedDynamicRules() {
        var dynamicRules = new HashMap<String, List<RuntimeRule>>();
        dynamicRules.put("dynamicRuleOne", List.of());
        dynamicRules.put("dynamicRuleTwo", List.of());

        var dynamicRuleOp = new QueryingDynamicRulesOperation("dynamicRepository");

        String expected = "Repository 'dynamicRepository' returned 2 dynamic rules: "
            + System.lineSeparator()
            + "'dynamicRuleOne',"
            + System.lineSeparator()
            + "'dynamicRuleTwo'";

        assertThat(dynamicRuleOp.describeAfter(dynamicRules), is(expected));
    }

}
