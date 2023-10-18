package kraken.runtime.engine;

import kraken.annotations.API;
import kraken.runtime.engine.dto.FieldEvaluationResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

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
    private final Map<String, FieldEvaluationResult> fieldResults;

    private final transient LocalDateTime evaluationTimeStamp;

    private final transient ZoneId ruleTimezoneId;

    public EntryPointResult() {
        this(LocalDateTime.now(), ZoneId.systemDefault());
    }

    public EntryPointResult(@Nonnull LocalDateTime evaluationTimeStamp,
                            @Nonnull ZoneId ruleTimezoneId) {
        this(Map.of(), evaluationTimeStamp, ruleTimezoneId);
    }

    public EntryPointResult(@Nonnull Map<String, FieldEvaluationResult> fieldResults,
                            @Nonnull LocalDateTime evaluationTimeStamp,
                            @Nonnull ZoneId ruleTimezoneId) {
        this.fieldResults = new HashMap<>(fieldResults);
        this.evaluationTimeStamp = Objects.requireNonNull(evaluationTimeStamp);
        this.ruleTimezoneId = Objects.requireNonNull(ruleTimezoneId);
    }

    /**
     * Flatten contents into single list of {@link RuleEvaluationResult}s
     *
     * @return  list of all contained {@link RuleEvaluationResult}s
     */
    @Nonnull
    public List<RuleEvaluationResult> getAllRuleResults() {
        return fieldResults.entrySet().stream()
                .flatMap(e -> e.getValue().getRuleResults().stream())
                .collect(Collectors.toList());
    }

    @Nonnull
    public List<RuleEvaluationResult> getApplicableRuleResults() {
        return fieldResults.entrySet().stream()
                .flatMap(e -> e.getValue().getRuleResults().stream())
                .filter(r -> r.getConditionEvaluationResult().isApplicable())
                .collect(Collectors.toList());
    }

    @Nonnull
    public Map<String, FieldEvaluationResult> getFieldResults() {
        return fieldResults;
    }

    @Nonnull
    public LocalDateTime getEvaluationTimeStamp() {
        return evaluationTimeStamp;
    }

    @Nonnull
    public ZoneId getRuleTimezoneId() {
        return ruleTimezoneId;
    }
}
