package kraken.runtime.engine.evaluation.loop;

import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.model.rule.RuntimeRule;

/**
 * Contains rule with data context.
 * Is used only in {@link OrderedEvaluationLoop}
 *
 * @author psurinin@eisgroup.com
 * @since 1.0.29
 */
public class RuleEvaluationInstance {
    private RuntimeRule rule;
    private DataContext dataContext;

    public RuleEvaluationInstance(RuntimeRule rule, DataContext dataContext) {
        this.rule = rule;
        this.dataContext = dataContext;
    }

    public RuntimeRule getRule() {
        return rule;
    }

    public DataContext getDataContext() {
        return dataContext;
    }
}