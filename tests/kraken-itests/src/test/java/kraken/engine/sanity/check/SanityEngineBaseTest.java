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
package kraken.engine.sanity.check;

import kraken.engine.EngineBaseTest;
import kraken.runtime.engine.context.info.DataObjectInfoResolver;
import kraken.runtime.engine.context.info.SimpleDataObjectInfoResolver;
import kraken.runtime.engine.context.type.ContextTypeAdapter;
import kraken.runtime.engine.context.type.IterableContextTypeAdapter;
import kraken.test.TestResources;
import kraken.testproduct.domain.Policy;
import kraken.testproduct.domain.meta.Identifiable;
import kraken.utils.MockAutoPolicyBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author avasiliauskas
 */
public abstract class SanityEngineBaseTest extends EngineBaseTest {

    @Override
    protected DataObjectInfoResolver getResolver() {
        return new MockInfoResolver();
    }

    @Override
    protected TestResources getResources() {
        return TestResources.create(TestResources.Info.TEST_PRODUCT);
    }

    protected Policy getDataObject() {
        return new MockAutoPolicyBuilder().addEmptyAutoPolicy().build();
    }

    private static final class MockInfoResolver extends SimpleDataObjectInfoResolver {

        @Override
        public String resolveContextNameForObject(Object data) {
            return data.getClass().getSimpleName();
        }

        @Override
        public String resolveContextIdForObject(Object data) {
            return ((Identifiable) data).getId();
        }
    }
}
