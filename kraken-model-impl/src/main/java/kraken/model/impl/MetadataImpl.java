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
package kraken.model.impl;

import kraken.model.Metadata;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MetadataImpl implements Metadata {
    private String type = Metadata.class.getSimpleName();
    private Map<String, Object> properties = new HashMap<>();

    @Override
    public String getType() {
        return type;
    }

    @Override
    public Object getProperty(String propertyName) {
        return properties.get(propertyName);
    }

    @Override
    public void setProperty(String propertyName, Object propertyValue) {
        properties.put(propertyName, propertyValue);
    }

    @Override
    public boolean hasProperty(String propertyName) {
        return properties.containsKey(propertyName);
    }

    @Override
    public Map<String, Object> asMap() {
        return Collections.unmodifiableMap(properties);
    }

    // added getter to support model introspection
    public Map<String, Object> getProperties() {
        return properties;
    }
}
