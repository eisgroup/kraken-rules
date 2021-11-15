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
package kraken.model;

import kraken.runtime.engine.dto.FieldEvaluationResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.testproduct.domain.Policy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RawEvaluationResponse extends EvaluationResponse {

    private List<RuleEvaluationResult> allRuleResults;

    private Map<String, FieldEvaluationResult> fieldResults;

    public RawEvaluationResponse(
            Policy modelBeforeEvaluation,
            Policy modelAfterEvaluation,
            List<RuleEvaluationResult> allRuleResults,
            Map<String, FieldEvaluationResult> fieldResults
    ) {
        super(modelBeforeEvaluation, modelAfterEvaluation);
        this.allRuleResults = allRuleResults;
        this.fieldResults = fieldResults.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            e.getValue();
//                            e.getValue().getRuleResults().forEach(rr -> rr.);
                            return e.getValue();
                        }
                ));
    }

    public List<RuleEvaluationResult> getAllRuleResults() {
        return allRuleResults;
    }

    public void setAllRuleResults(List<RuleEvaluationResult> allRuleResults) {
        this.allRuleResults = allRuleResults;
    }

    public Map<String, FieldEvaluationResult> getFieldResults() {
        return fieldResults;
    }

    public void setFieldResults(Map<String, FieldEvaluationResult> fieldResults) {
        this.fieldResults = fieldResults;
    }
}
