package kraken.runtime.engine.core;

import kraken.model.payload.PayloadType;
import kraken.runtime.engine.evaluation.loop.OrderedEvaluationLoop;
import kraken.runtime.model.rule.RuntimeRule;
import kraken.runtime.order.GraphFactory;
import kraken.runtime.repository.RuntimeContextRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Creates {@link EntryPointEvaluation}. Default {@link RuntimeRule}s are sorted topologically.
 * Default rules validated not to be applied twice or more on oe field.
 * Default rules appears first in the list. After sorted default rules comes other rules.
 * Order of not default rules is not important. {@link OrderedEvaluationLoop}
 * will process ordered rules.
 *
 * @see GraphFactory
 * @see OrderedEvaluationLoop
 *
 * @author psurinin@eisgroup.com
 * @since 1.0.29
 */
public class EntryPointOrderedEvaluationFactory {

    private final GraphFactory graphFactory;
    private static final String DEFAULT = "default";
    private static final String NOT_DEFAULT = "not_default";
    private static final Function<RuntimeRule, String> PAYLOAD_TYPE_LABEL =
            rule -> rule.getPayload().getType().equals(PayloadType.DEFAULT)
                    ? DEFAULT
                    : NOT_DEFAULT;

    public EntryPointOrderedEvaluationFactory(RuntimeContextRepository contextRepository) {
        this.graphFactory = new GraphFactory(contextRepository);
    }

    /**
     * Creates topologically sorted default rules at the start of rules list in
     * {@link EntryPointEvaluation} and other rules after default.
     *
     * @param entryPointData with rules to sort
     * @param entryPointName to put in {@link EntryPointEvaluation}
     * @param isDelta flag to decide filter out static rules and send only {@link RuntimeRule#isDimensional()}
     *                or send all rules. If {@code true} it will filter out static rules.
     * @return EntryPointEvaluation with sorted rules.
     */
    public EntryPointEvaluation create(EntryPointData entryPointData, String entryPointName, boolean isDelta) {
        Map<String, List<RuntimeRule>> rulesByPayloadLabel =
                entryPointData.getIncludedRules().values().stream().collect(Collectors.groupingBy(PAYLOAD_TYPE_LABEL));
        List<RuntimeRule> defaultRules = rulesByPayloadLabel.get(DEFAULT);
        List<RuntimeRule> notDefaultRules = rulesByPayloadLabel.get(NOT_DEFAULT);
        ArrayList<RuntimeRule> rules = new ArrayList<>();
        var order = new HashMap<String, Integer>();

        if (defaultRules != null) {
            List<RuntimeRule> orderedRules = graphFactory.getOrderedRules(defaultRules);
            rules.addAll(orderedRules);
            for (int i = 0; i < orderedRules.size(); i++) {
                order.put(orderedRules.get(i).getName(), i);
            }
        }
        if (notDefaultRules != null) {
            rules.addAll(notDefaultRules);
        }

        var rulesInBundle = new ArrayList<RuntimeRule>();

        for (int i = 0; i < rules.size(); i++) {
            var rule = rules.get(i);
            if (isDelta && !rule.isDimensional()) {
                continue;
            }
            rulesInBundle.add(rule);
        }
        return new EntryPointEvaluation(entryPointName, rulesInBundle, order, isDelta);
    }
}
