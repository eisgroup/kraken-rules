/*
 *  Copyright 2022 EIS Ltd and/or one of its affiliates.
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
package kraken.tracer.observer;

import kraken.tracer.TraceResult;

/**
 * SPI which provides ability to observe {@code TraceResult}. All implementations
 * of {@code this} SPI are invoked upon trace completion.
 *
 * <p>Implementation of {@link TraceObserver} must be registered in the system
 * following guidelines provided in {@link java.util.ServiceLoader}.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public interface TraceObserver {

    /**
     * @param result Trace result.
     */
    void observe(TraceResult result);

}
