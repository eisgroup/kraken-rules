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
package kraken.engine.evaluation;

import kraken.engine.EngineBaseTest;
import kraken.runtime.engine.context.info.DataObjectInfoResolver;
import kraken.runtime.engine.context.info.SimpleDataObjectInfoResolver;
import kraken.runtime.engine.context.type.ContextTypeAdapter;
import kraken.runtime.engine.context.type.IterableContextTypeAdapter;
import kraken.test.TestResources;
import kraken.utils.MockAutoPolicyBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author avasiliauskas
 */
public abstract class EvaluationEngineBaseTest extends EngineBaseTest {

    @Override
    protected DataObjectInfoResolver getResolver() {
        return new SimpleDataObjectInfoResolver();
    }

    @Override
    protected Object getDataObject() {
        return new MockAutoPolicyBuilder().addEmptyAutoPolicy().build();
    }

    @Override
    protected List<IterableContextTypeAdapter> getIterableTypeAdapters() {
        return new ArrayList<>();
    }

    @Override
    protected List<ContextTypeAdapter> getInstanceTypeAdapters() {
        return new ArrayList<>();
    }

    @Override
    protected TestResources getResources() {
        return  TestResources.create(TestResources.Info.TEST_PRODUCT);
    }

}
