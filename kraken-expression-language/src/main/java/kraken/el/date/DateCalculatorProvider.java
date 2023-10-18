/*
 * Copyright 2023 EIS Ltd and/or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package kraken.el.date;

import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * @author Mindaugas Ulevicius
 */
class DateCalculatorProvider {

    private static DateCalculator instance = null;

    /**
     * Resolves instance of {@link DateCalculator}.
     */
    static DateCalculator getInstance() {
        if (instance == null) {
            Iterator<DateCalculator> factories = ServiceLoader.load(DateCalculator.class).iterator();
            if(factories.hasNext()) {
                instance = factories.next();
                if(factories.hasNext()) {
                    throw new IllegalStateException("More than one custom implementation of DateCalculator found. "
                        + "There can only be one implementation of DateCalculator registered in the system.");
                }
            } else {
                instance = new DefaultDateCalculator();
            }
        }
        return instance;
    }
}
