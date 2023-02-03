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
package kraken.tracer;

import java.util.List;

import kraken.tracer.observer.TraceObserver;

/**
 * SPI For turning tracing on or and providing default {@code TraceObserver}'s. Configurer
 * is queried once on loading of {@code Tracer} class. It cannot be used to configure tracing
 * after {@code Tracer} class is loaded.
 *
 * <p>Implementation of {@link TracerConfigurer} must be registered in the system following
 * guidelines provided in {@link java.util.ServiceLoader}. Only one {@code TracerConfigurer}
 * is allowed on application class path.
 *
 * @author Tomas Dapkunas
 * @since 1.33.0
 */
public interface TracerConfigurer {

    /**
     * Allows to turn {@code Tracer} on or off based on returned value.
     */
    boolean isEnabled();

    /**
     * A list of trace observers to register if and only if {@link #isEnabled()}
     * return {@code true}.
     *
     * @return Trace observers to register in Tracer.
     */
    List<TraceObserver> traceObservers();

}
