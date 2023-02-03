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
package kraken.runtime.engine.context;

import java.util.ArrayList;
import java.util.HashMap;

import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.context.data.DataContextBuilder;
import kraken.runtime.engine.context.data.DataContextBuildingException;
import kraken.runtime.engine.context.info.navpath.DataNavigationContextInstanceInfoResolver;
import kraken.runtime.repository.RuntimeContextRepository;
import kraken.test.domain.simple.Person;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;


/**
 * Unit test for {@link DataContextBuilder}
 *
 * @author rimas
 * @since 1.0
 */
@RunWith(MockitoJUnitRunner.class)
public class DataContextBuilderTest {

    @InjectMocks
    private DataContextBuilder testObject;

    private Person person = new Person();

    @Before
    public void setUp() {
        RuntimeContextRepository contextRepository = mock(RuntimeContextRepository.class);
        DataNavigationContextInstanceInfoResolver resolver = new DataNavigationContextInstanceInfoResolver();
        testObject = new DataContextBuilder(contextRepository, resolver);
    }

    @Test
    public void contextIsBuildFromObject() {
        DataContext dataContext = testObject.buildFromRoot(person);
        assertThat(dataContext.getDataObject(), is(person));
        assertThat(dataContext.getContextName(), is(equalTo("Person")));
        assertThat(dataContext.getContextId(), is(notNullValue()));
        assertThat(dataContext.getContextId(), is(instanceOf(String.class)));
    }

    @Test
    public void nullDataObjectThrowsException() {
        assertThrows(DataContextBuildingException.class,
                () -> testObject.buildFromRoot(null));
    }

    @Test
    public void failsOnCollectionContext() {
        assertThrows(DataContextBuildingException.class,
                () -> testObject.buildFromRoot(new ArrayList<>()));
    }

    @Test
    public void failsOnMapContext() {
        assertThrows(DataContextBuildingException.class,
                () -> testObject.buildFromRoot(new HashMap<>()));
    }

}