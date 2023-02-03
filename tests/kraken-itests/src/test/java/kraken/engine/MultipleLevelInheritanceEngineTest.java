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
package kraken.engine;

import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.context.info.DataObjectInfoResolver;
import kraken.runtime.engine.context.info.SimpleDataObjectInfoResolver;
import kraken.runtime.engine.context.type.ContextTypeAdapter;
import kraken.runtime.engine.context.type.IterableContextTypeAdapter;
import kraken.runtime.engine.events.RuleEvent;
import kraken.runtime.engine.events.ValueChangedEvent;
import kraken.runtime.engine.result.DefaultValuePayloadResult;
import kraken.test.TestResources;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static kraken.testing.matchers.KrakenMatchers.hasNoIgnoredRules;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author psurinin
 */
public class MultipleLevelInheritanceEngineTest extends EngineBaseTest {
    @Override
    protected TestResources getResources() {
        return TestResources.create(TestResources.Info.NOT_ROOT_INH);
    }

    private Policy getDataObject() {
        return new CanadaPersonalAutoPolicy();
    }

    @Test
    public void shouldSetDefaultValueWithRuleOnInheritedContext() {
        EntryPointResult results = engine.evaluate(getDataObject(), "not-root-policy");
        assertThat(results.getAllRuleResults(), hasSize(1));
        List<RuleEvent> events = ((DefaultValuePayloadResult) results.getAllRuleResults().get(0).getPayloadResult())
                .getEvents();
        assertThat(events, hasSize(1));
        assertThat(((ValueChangedEvent) events.get(0)).getNewValue(), is("mock"));
        assertThat(results, hasNoIgnoredRules());
    }

    @Test
    public void shouldSetDefaultValueWithRuleOnInheritedChildContext() {
        EntryPointResult results = engine.evaluate(getDataObject(), "not-root-risk");
        assertThat(results.getAllRuleResults(), hasSize(1));
        List<RuleEvent> events = ((DefaultValuePayloadResult) results.getAllRuleResults().get(0).getPayloadResult())
                .getEvents();
        assertThat(events, hasSize(1));
        assertThat(((ValueChangedEvent) events.get(0)).getNewValue(), is("mock"));
        assertThat(results, hasNoIgnoredRules());
    }

    protected interface Policy {
        RiskItem getRiskItem();
    }

    public static class AutoPolicy implements Policy{
        private String mock = "";
        private RiskItem riskItem = new Vehicle();

        @Override
        public RiskItem getRiskItem() {
            return riskItem;
        }

        public void setRiskItem(RiskItem riskItem) {
            this.riskItem = riskItem;
        }

        public String getMock() {
            return mock;
        }

        public void setMock(String mock) {
            this.mock = mock;
        }
    }

    static class PersonalAutoPolicy extends AutoPolicy{

    }

    static class CanadaPersonalAutoPolicy extends PersonalAutoPolicy{
    }

    public interface RiskItem {
        String getMock();
    }

    public static class Vehicle implements RiskItem {
        private String mock = "";

        public String getMock() {
            return mock;
        }

        public void setMock(String mock) {
            this.mock = mock;
        }
    }
}
