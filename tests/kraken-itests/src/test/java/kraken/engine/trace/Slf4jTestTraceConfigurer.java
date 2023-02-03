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
package kraken.engine.trace;

import java.util.List;

import kraken.tracer.TracerConfigurer;
import kraken.tracer.observer.Slf4jTraceObserver;
import kraken.tracer.observer.TraceObserver;

/**
 * @author mulevicius
 */
public class Slf4jTestTraceConfigurer implements TracerConfigurer {

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public List<TraceObserver> traceObservers() {
        return List.of(Slf4jTraceObserver.INSTANCE);
    }
}
