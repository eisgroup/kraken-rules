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
package kraken.runtime;

import java.time.LocalDateTime;
import java.util.Map;

import kraken.runtime.expressions.KrakenTypeProvider;
import kraken.runtime.utils.TokenGenerator;

/**
 * Session contains information on current Kraken evaluation invocation
 *
 * @author psurinin
 */
public final class EvaluationSession {

    private static final TokenGenerator TOKEN_GENERATOR = new TokenGenerator();

    private EvaluationConfig evaluationConfig;

    private String sessionToken;

    private LocalDateTime timestamp;

    private Map<String, Object> expressionContext;

    private KrakenTypeProvider krakenTypeProvider;

    public EvaluationSession(EvaluationConfig evaluationConfig,
                             Map<String, Object> expressionContext,
                             KrakenTypeProvider krakenTypeProvider) {
        this.evaluationConfig = evaluationConfig;
        this.timestamp = LocalDateTime.now();
        this.sessionToken = TOKEN_GENERATOR.generateNewToken(timestamp);
        this.expressionContext = expressionContext;
        this.krakenTypeProvider = krakenTypeProvider;
    }

    public EvaluationConfig getEvaluationConfig() {
        return evaluationConfig;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getExpressionContext() {
        return expressionContext;
    }

    public KrakenTypeProvider getKrakenTypeProvider() {
        return krakenTypeProvider;
    }

}
