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

package kraken.service;

import kraken.model.EntryPointName;
import kraken.model.entrypoint.EntryPoint;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static kraken.model.EntryPointName.QA1;
import static kraken.model.EntryPointName.QA2;
import static kraken.utils.TestUtils.createMockRules;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author psurinin@eisgroup.com
 * @since 1.1.0
 */
public class ReloadableStorageTest {

    private ReloadableStorage storage;

    @Before
    public void before() {
        storage = new ReloadableStorage("Test");
    }

    @Test
    public void shouldBeEmpty() {
        assertThat(storage.getEntryPoints().count(), is(6L));
        assertThat(storage.getEntryPoints().mapToLong(ep -> ep.getRuleNames().size()).sum(), is(0L));
        assertThat(storage.getRules().count(), is(0L));
    }

    @Test
    public void shouldAddRule() {
        storage.add(QA1, createMockRules("TestRule"));

        assertThat(storage.getEntryPoints().count(), is(6L));
        assertThat(storage.getEntryPoints().mapToLong(ep -> ep.getRuleNames().size()).sum(), is(1L));
        assertThat(storage.getRules().count(), is(1L));

        assertThat(getEntryPoint(QA1).getRuleNames(), hasSize(1));
        assertThat(getEntryPoint(QA1).getRuleNames().get(0), is("TestRule"));
    }

    @Test
    public void shouldRemoveRule() {
        storage.add(QA1, createMockRules("TestRule1", "TestRule2"));

        assertThat(storage.getRules().count(), is(2L));
        assertThat(getEntryPoint(QA1).getRuleNames(), hasSize(2));

        storage.remove(QA1, List.of("TestRule2"));
        assertThat(storage.getRules().count(), is(1L));
        assertThat(getEntryPoint(QA1).getRuleNames(), hasSize(1));
    }

    @Test
    public void shouldClearOneEntryPoint() {
        storage.add(QA1, createMockRules("TestRule1", "TestRule2"));
        storage.add(QA2, createMockRules("TestRule1", "TestRule2"));

        assertThat(storage.getRules().count(), is(2L));
        assertThat(getEntryPoint(QA1).getRuleNames(), hasSize(2));
        assertThat(getEntryPoint(QA2).getRuleNames(), hasSize(2));

        storage.clear(QA1);
        assertThat(storage.getRules().count(), is(2L));
        assertThat(getEntryPoint(QA1).getRuleNames(), hasSize(0));
        assertThat(getEntryPoint(QA2).getRuleNames(), hasSize(2));
    }

    @Test
    public void shouldClearAllEntryPointsAndRemoveRules() {
        storage.add(QA1, createMockRules("TestRule1", "TestRule2"));
        storage.add(QA2, createMockRules("TestRule1", "TestRule2"));

        assertThat(storage.getRules().count(), is(2L));
        assertThat(getEntryPoint(QA1).getRuleNames(), hasSize(2));
        assertThat(getEntryPoint(QA2).getRuleNames(), hasSize(2));

        storage.clear();

        assertThat(storage.getRules().count(), is(0L));
        assertThat(getEntryPoint(QA1).getRuleNames(), hasSize(0));
        assertThat(getEntryPoint(QA2).getRuleNames(), hasSize(0));
    }

    @Test
    public void shouldOverrideAndHaveTwoRuleNames() {
        storage.add(QA1, createMockRules("TestRule1", "TestRule2"));
        storage.add(QA1, createMockRules("TestRule1", "TestRule2"));

        assertThat(storage.getRules().count(), is(2L));
        assertThat(getEntryPoint(QA1).getRuleNames(), hasSize(2));
    }

    private EntryPoint getEntryPoint(EntryPointName entryPointName) {
        return storage.getEntryPoints()
                .filter(entryPoint -> entryPoint.getName().equals(entryPointName.name()))
                .findFirst()
                .orElseThrow();
    }

}
