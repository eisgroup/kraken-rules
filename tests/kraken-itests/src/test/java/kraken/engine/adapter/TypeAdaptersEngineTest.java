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
package kraken.engine.adapter;

import kraken.engine.EngineBaseTest;
import kraken.engine.adapter.test.domain.*;
import kraken.engine.adapter.test.domain.wrappers.ExtendedWrapper;
import kraken.engine.adapter.test.domain.wrappers.InfoAdapterHolder;
import kraken.engine.adapter.test.domain.wrappers.Wrapper;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.context.info.DataObjectInfoResolver;
import kraken.runtime.engine.context.info.SimpleDataObjectInfoResolver;
import kraken.runtime.engine.context.info.iterators.ContextInstanceIterator;
import kraken.runtime.engine.context.type.ContextTypeAdapter;
import kraken.runtime.engine.context.type.IterableContextTypeAdapter;
import kraken.test.TestResources;
import org.junit.Test;

import java.util.*;

import static kraken.testing.matchers.KrakenMatchers.hasNoIgnoredRules;
import static kraken.testing.matchers.KrakenMatchers.hasValidationFailures;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author psurinin
 * @since 1.0
 */
public final class TypeAdaptersEngineTest extends EngineBaseTest {

    @Override
    protected DataObjectInfoResolver getResolver() {
        return new SimpleDataObjectInfoResolver();
    }

    @Override
    protected TestResources getResources() {
        return TestResources.create(TestResources.Info.TYPE_ADAPTER);
    }

    @Override
    protected Object getDataObject() {
        return createMockPolicyWithWrappers();
    }

    @Override
    protected ArrayList<IterableContextTypeAdapter> getIterableTypeAdapters() {
        final ArrayList<IterableContextTypeAdapter> adapters = new ArrayList<>();
        adapters.add(new SetIterableTypeAdapter());
        return adapters;
    }

    @Override
    protected ArrayList<ContextTypeAdapter> getInstanceTypeAdapters() {
        final ArrayList<ContextTypeAdapter> contextTypeAdapters = new ArrayList<>();
        contextTypeAdapters.add(new ExtendedWraperTypeAdapter());
        contextTypeAdapters.add(new WraperTypeAdapter());
        return contextTypeAdapters;
    }

    @Test
    public void shouldGetAllFailedResultsOnWrappedInstances() {
        final EntryPointResult result = engine.evaluate(dataObject, "Adapter");
        // assert
        assertThat(result, hasValidationFailures(4));
        assertThat(result, hasNoIgnoredRules());
    }

    private Policy createMockPolicyWithWrappers() {
        final Policy mockPolicy = new Policy("1");
        final InfoAdapterHolder holder = new InfoAdapterHolder();
        holder.coverageAWrapper = new Wrapper<>(new CoverageA("1", 1));
        holder.extendedWrapper = new ExtendedWrapper<>(new CoverageB("1", 1));
        holder.riskItem = Optional.of(new RiskItem("BMW"));
        holder.wrappedInsureds = new Wrapper<>(new HashSet<>(Collections.singletonList(Optional.of(new Insured("Tree")))));
        mockPolicy.setTypeAdapterRiskItem(holder);
        return mockPolicy;
    }

    private static final class WraperTypeAdapter implements ContextTypeAdapter{
        @Override
        public Object getValue(Object object) {
            return ((Wrapper) object).unwrap();
        }
        @Override
        public boolean isApplicable(Object object) {
            return object instanceof Wrapper;
        }
    }

    private static final class ExtendedWraperTypeAdapter implements ContextTypeAdapter{
        @Override
        public Object getValue(Object object) {
            return ((ExtendedWrapper) object).get();
        }
        @Override
        public boolean isApplicable(Object object) {
            return object instanceof ExtendedWrapper;
        }
    }

    private static final class SetIterableTypeAdapter implements IterableContextTypeAdapter {
        @Override
        public ContextInstanceIterator createIterator(Object object) {
            final Set set = (Set) object;
            final Iterator iterator = set.iterator();
            return new ContextInstanceIterator() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Object next() {
                    return iterator.next();
                }

                @Override
                public Object key() {
                    return 1;
                }
                @Override
                public Object getValue(Object index) {
                    return set.iterator().next();
                }
            };
        }
        @Override
        public boolean isApplicable(Object object) {
            return object instanceof Set;
        }
    }
}
