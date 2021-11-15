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
package kraken.runtime.engine.dto;

import kraken.model.Rule;
import kraken.model.payload.PayloadType;

/**
 * @author mulevicius
 */
public class RuleInfo {

    private String ruleName;
    private String context;
    private String targetPath;
    private PayloadType payloadType;

    public RuleInfo(String ruleName, String context, String targetPath, PayloadType payloadType) {
        this.ruleName = ruleName;
        this.context = context;
        this.targetPath = targetPath;
        this.payloadType = payloadType;
    }

    /**
     *
     * @return  simple name of evaluated Rule
     *          note, that the name does NOT contain namespace prefix
     */
    public String getRuleName() {
        return ruleName;
    }

    /**
     *
     * @return name of Context Definition that the Rule is applied on
     */
    public String getContext() {
        return context;
    }

    /**
     *
     * @return field name in Context Definition that the Rule is applied on; this is equal to {@link Rule#getTargetPath()}
     */
    public String getTargetPath() {
        return targetPath;
    }

    /**
     *
     * @return a type of Payload that was executed
     */
    public PayloadType getPayloadType() {
        return payloadType;
    }

}
