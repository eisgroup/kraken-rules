package kraken.runtime.engine.evaluation.loop;

import kraken.runtime.engine.context.data.DataContext;
import kraken.runtime.engine.dto.FieldEvaluationResult;
import kraken.runtime.engine.dto.RuleEvaluationResult;

import java.util.Objects;

/**
 * Contains rule result with data context.
 * Is used only in {@link OrderedEvaluationLoop}
 *
 * @author psurinin@eisgroup.com
 * @since 1.0.29
 *
 * @see OrderedEvaluationLoop
 */
class RuleEvaluationResultInstance {
        DataContext dataContext;
        RuleEvaluationResult result;

        RuleEvaluationResultInstance(DataContext dataContext, RuleEvaluationResult result) {
            this.dataContext = dataContext;
            this.result = result;
        }

        public DataContext getDataContext() {
            return dataContext;
        }

        public RuleEvaluationResult getResult() {
            return result;
        }

        public String id() {
            return dataContext.getContextName() + ":" + dataContext.getContextId() + ":" + result.getRuleInfo().getTargetPath();
        }

        @Override
        public int hashCode() {
            return Objects.hash(id());
        }
    }