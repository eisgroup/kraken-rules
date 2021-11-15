/*
 *  Copyright 2017 EIS Ltd and/or one of its affiliates.
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
package kraken.el.functions;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import kraken.el.InvocationContextHolder;
import org.junit.Before;
import org.junit.Test;

import static kraken.el.functions.TypeProvider.TYPE_PROVIDER_PROPERTY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author mulevicius
 */
public class TypeFunctionsTest {

    @Before
    public void setUp() {
        InvocationContextHolder.setInvocationContext(new InvocationContextHolder.InvocationContext(null,
                Map.of(TYPE_PROVIDER_PROPERTY, new TypeProvider() {
                    @Override
                    public String getTypeOf(Object object) {
                        return object.getClass().getSimpleName();
                    }

                    @Override
                    public Collection<String> getInheritedTypesOf(Object object) {
                        return Arrays.stream(object.getClass().getInterfaces())
                                .map(c -> c.getSimpleName())
                                .collect(Collectors.toList());
                    }
                }
        )));
    }

    @Test
    public void shouldReturnTypeOfObject() {
        String type = TypeFunctions.getType(new Coverage());
        assertThat(type, is(equalTo("Coverage")));
    }

    @Test
    public void shouldReturnNullIfObjectIsNull() {
        String type = TypeFunctions.getType(null);
        assertThat(type, is(nullValue()));
    }

    static class Coverage {

    }

}
