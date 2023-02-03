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
package kraken.facade;

import static kraken.testproduct.TestProduct.toEntryPointName;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import kraken.model.EvaluationResponse;
import kraken.model.RawEvaluationResponse;
import kraken.model.ValidationEvaluationResponse;
import kraken.runtime.RuleEngine;
import kraken.runtime.engine.EntryPointResult;
import kraken.runtime.engine.dto.FieldEvaluationResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;
import kraken.runtime.engine.result.reducers.validation.ValidationStatus;
import kraken.runtime.engine.result.reducers.validation.ValidationStatusReducer;
import kraken.testproduct.domain.Policy;
import kraken.utils.GsonUtils;

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class EvaluationFacade {

    private final Gson gson = GsonUtils.prettyGson();

    @Autowired
    private RuleEngine ruleEngine;

    @Operation(
        summary = "Get RAW result after engine evaluation",
        description = "Endpoint for entry point evaluation on model instance. Evaluation will return " +
            "RawEvaluationResponse witch contains model instance before evaluation, " +
            "model instance after evaluation and raw evaluation result. " +
            "Raw evaluation result contains all rule result, field result and failed rule result."
    )
    @PostMapping("/evaluation/{entryPointName}/raw")
    public EvaluationResponse evaluateEngineRawResult(
            @Parameter(required = true, description = "Valid model instance")
            @RequestBody Policy policy,
            @Parameter(required = true, description = "Valid entry point name")
            @PathVariable String entryPointName
    ) {
        Policy modelAfterEvaluation = deepCopy(policy);
        EntryPointResult result =
                ruleEngine.evaluate(modelAfterEvaluation, toEntryPointName(entryPointName));
        List<RuleEvaluationResult> allRuleResults =
                new ArrayList<>(result.getAllRuleResults());
        Map<String, FieldEvaluationResult> fieldResults =
                new TreeMap<>(result.getFieldResults());
        return new RawEvaluationResponse(policy, modelAfterEvaluation, allRuleResults, fieldResults);
    }

    @Operation(
        summary = "Get Validation status result after engine evaluation",
        description = "Endpoint for entry point evaluation on model instance. Evaluation will return " +
            "ValidationEvaluationResponse witch contains model instance before evaluation, " +
            "model instance after evaluation and validations status results."
    )
    @PostMapping("/evaluation/{entryPointName}/validation")
    public EvaluationResponse evaluateEngineValidationResult(
            @Parameter(required = true, description = "model instance")
            @RequestBody Policy policy,
            @Parameter(required = true, description = "entrypoint name")
            @PathVariable String entryPointName
    ) {
        Policy modelAfterEvaluation = deepCopy(policy);
        final EntryPointResult result = ruleEngine.evaluate(modelAfterEvaluation, toEntryPointName(entryPointName));
        final ValidationStatus reduced = new ValidationStatusReducer().reduce(result);
        return new ValidationEvaluationResponse(policy, modelAfterEvaluation, reduced);
    }

    private Policy deepCopy(Policy policy) {
        return gson.fromJson(gson.toJson(policy), Policy.class);
    }
}
