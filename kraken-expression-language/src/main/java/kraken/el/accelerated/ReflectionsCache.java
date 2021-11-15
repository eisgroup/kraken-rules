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
package kraken.el.accelerated;

import java.beans.*;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author mulevicius
 */
public class ReflectionsCache {

    private static final Map<Class<?>, ClassMethods> methodsByClass = new ConcurrentHashMap<>();

    public static Map<String, Method> getGettersOrCompute(Class<?> type) {
        return methodsByClass.computeIfAbsent(type, ReflectionsCache::buildMethods).getGetters();
    }
    
    public static Map<String, Method> getSettersOrCompute(Class<?> type) {
        return methodsByClass.computeIfAbsent(type, ReflectionsCache::buildMethods).getSetters();
    }

    private static ClassMethods buildMethods(Class<?> type) {
        try {
            Map<String, Method> getters = new HashMap<>();
            Map<String, Method> setters = new HashMap<>();
            for(PropertyDescriptor propertyDescriptor : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
                if(propertyDescriptor.getReadMethod() != null) {
                    getters.put(propertyDescriptor.getName(), propertyDescriptor.getReadMethod());
                }
                if(propertyDescriptor.getWriteMethod() != null) {
                    setters.put(propertyDescriptor.getName(), propertyDescriptor.getWriteMethod());
                }
            }
            return new ClassMethods(getters, setters);
        } catch (IntrospectionException e) {
            throw new IllegalStateException("Failed to introspect class " + type);
        }
    }

    static class ClassMethods {

        private Map<String, Method> getters;

        private Map<String, Method> setters;

        public ClassMethods(Map<String, Method> getters, Map<String, Method> setters) {
            this.getters = getters;
            this.setters = setters;
        }

        public Map<String, Method> getGetters() {
            return getters;
        }

        public Map<String, Method> getSetters() {
            return setters;
        }
    }
}
