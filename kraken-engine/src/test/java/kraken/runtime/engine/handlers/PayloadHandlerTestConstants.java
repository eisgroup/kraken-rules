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
package kraken.runtime.engine.handlers;

import static org.mockito.Mockito.mock;

import java.util.Collections;
import java.util.Map;

import kraken.context.model.tree.ContextModelTreeMetadata;
import kraken.context.model.tree.impl.ContextModelTreeImpl;
import kraken.el.TargetEnvironment;
import kraken.runtime.EvaluationConfig;
import kraken.runtime.EvaluationSession;
import kraken.runtime.expressions.KrakenTypeProvider;

/**
 * @author psurinin
 */
class PayloadHandlerTestConstants {

    static final EvaluationSession SESSION =
            new EvaluationSession(
                new EvaluationConfig("USD"),
                Collections.emptyMap(),
                mock(KrakenTypeProvider.class),
                Map.of(),
                "",
                new ContextModelTreeImpl(
                    Map.of(),
                    Map.of(),
                    new ContextModelTreeMetadata("test", TargetEnvironment.JAVA))
            );
}
