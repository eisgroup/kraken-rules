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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Registers, loads and provides instances of {@link AcceleratedPropertyHandler} existing in system
 *
 * @author mulevicius
 */
public class AcceleratedPropertyHandlerProvider {

    private static final List<AcceleratedPropertyHandler> PROPERTY_HANDLERS = new ArrayList<>();

    static {
        ServiceLoader.load(AcceleratedPropertyHandler.class).forEach(h -> PROPERTY_HANDLERS.add(h));
        Collections.sort(PROPERTY_HANDLERS, (o1, o2) -> {
            if(o1.getType().equals(o2.getType())) {
                return 0;
            }
            if(o1.getType().isAssignableFrom(o2.getType())) {
                return 1;
            }
            return -1;
        });
    }

    public static Collection<AcceleratedPropertyHandler> getPropertyHandlers() {
        return PROPERTY_HANDLERS;
    }

    /**
     * @param object that should be handled
     * @return first instance of {@link AcceleratedPropertyHandler} that can handle provided object
     */
    public static AcceleratedPropertyHandler findPropertyHandlerFor(Object object) {
        for(AcceleratedPropertyHandler acceleratedPropertyHandler : PROPERTY_HANDLERS) {
            if(acceleratedPropertyHandler.getType().isInstance(object)) {
                return acceleratedPropertyHandler;
            }
        }
        return null;
    }

}
