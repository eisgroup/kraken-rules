package kraken.runtime.engine;

import kraken.annotations.API;
import kraken.runtime.engine.dto.FieldEvaluationResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Contains evaluation results from specific entry point
 *
 * @author rimas
 * @since 1.0
 */
@API
public class EntryPointResult {

    /**
     * Field evaluation results, mapped to field id string
     */
    private Map<String, FieldEvaluationResult> fieldResults;

    private transient LocalDateTime evaluationTimeStamp;
    /**
     * Default constructor
     */
    public EntryPointResult() {
        fieldResults = new HashMap<>();
        evaluationTimeStamp = LocalDateTime.now();
    }

    public EntryPointResult(Map<String, FieldEvaluationResult> fieldResults, LocalDateTime evaluationTimeStamp) {
        this.fieldResults = new HashMap<>(fieldResults);
        this.evaluationTimeStamp = evaluationTimeStamp;
    }

    /**
     * Flatten contents into single list of {@link RuleEvaluationResult}s
     *
     * @return  list of all contained {@link RuleEvaluationResult}s
     */
    public List<RuleEvaluationResult> getAllRuleResults() {
        return fieldResults.entrySet().stream()
                .flatMap(e -> e.getValue().getRuleResults().stream())
                .collect(Collectors.toList());
    }

    public List<RuleEvaluationResult> getApplicableRuleResults() {
        return fieldResults.entrySet().stream()
                .flatMap(e -> e.getValue().getRuleResults().stream())
                .filter(r -> r.getConditionEvaluationResult().isApplicable())
                .collect(Collectors.toList());
    }

    public LocalDateTime getEvaluationTimeStamp() {
        return evaluationTimeStamp;
    }

    public Map<String, FieldEvaluationResult> getFieldResults() {
        return fieldResults;
    }

}
