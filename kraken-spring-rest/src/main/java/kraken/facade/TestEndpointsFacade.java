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

import io.swagger.v3.oas.annotations.Operation;
import kraken.runtime.EvaluationConfig;
import kraken.runtime.RuleEngine;
import kraken.testing.reducer.*;
import kraken.testproduct.domain.Policy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Function;

/**
 * @author psurinin@eisgroup.com
 * @since 1.0.38
 */

@RestController
@CrossOrigin(origins = "*", maxAge = 3600)
public class TestEndpointsFacade {

    private static <M extends ResultMetadataContainer> Function<Collection<M>, Collection<M>> filterIfPresent(String ruleName) {
        if (ruleName == null) {
            return ms -> ms;
        }
        return ms -> KrakenReducers.forOneRule(ms, ruleName);
    }

    private static final EvaluationConfig EVALUATION_CONFIG = new EvaluationConfig("EUR");

    @Autowired
    private RuleEngine ruleEngine;

    @Operation(
        summary = "Evaluate rules and reduce to see only Accessibility results",
        description = "Rule name can be passed as a filter"
    )
    @PostMapping("/evaluate/accessibility/{entryPointName}")
    public Collection<AccessibilityMetadata> accessibility(
            @RequestBody Policy data,
            @PathVariable String entryPointName,
            @RequestParam(required = false) String ruleName
    ) {
        return Optional.of(ruleEngine.evaluate(data, entryPointName, EVALUATION_CONFIG))
                .map(KrakenReducers.ACCESSIBILITY::reduce)
                .map(filterIfPresent(ruleName))
                .orElseThrow();
    }

    @Operation(
        summary = "Evaluate rules and reduce to see only Visibility results",
        description = "Rule name can be passed as a filter"
    )
    @PostMapping("/evaluate/visibility/{entryPointName}")
    public Collection<VisibilityMetadata> visibility(
            @RequestBody Policy data,
            @PathVariable String entryPointName,
            @RequestParam(required = false) String ruleName
    ) {
        return Optional.of(ruleEngine.evaluate(data, entryPointName, EVALUATION_CONFIG))
                .map(KrakenReducers.VISIBILITY::reduce)
                .map(filterIfPresent(ruleName))
                .orElseThrow();
    }

    @Operation(
        summary = "Evaluate rules and reduce to see only Validation results",
        description = "Rule name can be passed as a filter"
    )
    @PostMapping("/evaluate/validation/{entryPointName}")
    public Collection<ValidationMetadata> validation(
            @RequestBody Policy data,
            @PathVariable String entryPointName,
            @RequestParam(required = false) String ruleName
    ) {
        return Optional.of(ruleEngine.evaluate(data, entryPointName, EVALUATION_CONFIG))
                .map(KrakenReducers.VALIDATION::reduce)
                .map(filterIfPresent(ruleName))
                .orElseThrow();
    }

    @Operation(
        summary = "Evaluate rules and reduce to see only Default results",
        description = "Rule name can be passed as a filter"
    )
    @PostMapping("/evaluate/default/{entryPointName}")
    public Collection<DefaultValueMetadata> defaultEndpoint(
            @RequestBody Policy data,
            @PathVariable String entryPointName,
            @RequestParam(required = false) String ruleName
    ) {
        return Optional.of(ruleEngine.evaluate(data, entryPointName, EVALUATION_CONFIG))
                .map(KrakenReducers.DEFAULT::reduce)
                .map(filterIfPresent(ruleName))
                .orElseThrow();
    }

}
