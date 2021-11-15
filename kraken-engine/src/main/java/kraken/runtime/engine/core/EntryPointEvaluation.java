package kraken.runtime.engine.core;

import java.util.*;

import kraken.runtime.model.rule.RuntimeRule;

/**
 * Represents rule data for a specific entry point.
 *
 * @author rimas
 * @since 1.0
 */
@SuppressWarnings("WeakerAccess")
public class EntryPointEvaluation {

    private final String entryPointName;
    private final ArrayList<RuntimeRule> rules;
    private final Map<String, Integer> rulesOrder;
    private final boolean delta;

    public EntryPointEvaluation(
            String entryPointName,
            ArrayList<RuntimeRule> rules,
            Map<String, Integer> rulesOrder,
            boolean delta
    ) {
        this.entryPointName = entryPointName;
        this.rules = rules;
        this.rulesOrder = rulesOrder;
        this.delta = delta;
    }

    public String getEntryPointName() {
        return entryPointName;
    }

    public ArrayList<RuntimeRule> getRules() {
        return rules;
    }

    public Map<String, Integer> getRulesOrder() {
        return rulesOrder;
    }

    public boolean isDelta() {
        return delta;
    }
}