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
package kraken.runtime.engine.context.info;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Sample implementation of {@link DataObjectInfoResolver}
 *
 * @author rimas
 * @since 1.0
 */
public class SimpleDataObjectInfoResolver implements DataObjectInfoResolver {

    /**
     * Resolves context name to short class name of object
     *
     * @param data
     * @return
     */
    public String resolveContextNameForObject(Object data) {
        return data.getClass().getSimpleName();
    }

    /**
     * Resolves context id to hexadecimal representation of object hash code
     *
     * @param data
     * @return
     */
    public String resolveContextIdForObject(Object data) {
        return Integer.toHexString(data.hashCode()).toUpperCase();
    }

    @Override
    public Collection<DataErrorDefinition> validateContextDataObject(Object data) {
        ArrayList<DataErrorDefinition> errors = new ArrayList();
        if (data == null) {
            errors.add(new DataErrorDefinition("Data object cannot be null"));
        }
        if (data instanceof Collection) {
            errors.add(new DataErrorDefinition("Data object cannot be Collection"));
        }
        if (data instanceof Map) {
            errors.add(new DataErrorDefinition("Data object cannot be Map"));
        }
        return errors;
    }

}
