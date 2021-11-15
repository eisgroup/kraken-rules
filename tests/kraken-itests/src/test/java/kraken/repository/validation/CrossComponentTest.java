package kraken.repository.validation;

import kraken.context.model.tree.impl.ContextModelTrees;
import kraken.context.model.tree.impl.ContextRepository;
import kraken.el.TargetEnvironment;
import kraken.test.TestResources;
import org.hamcrest.CoreMatchers;
import org.hamcrest.number.IsCloseTo;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author psurinin
 */
public class CrossComponentTest {

    private static final TestResources TEST_RESOURCES = TestResources.create(TestResources.Info.POLICY_GENESIS);
    private static final ContextRepository CONTEXT_REPOSITORY = ContextRepository.from(TEST_RESOURCES.getRuntimeKrakenProject());

    private static int fib(int n) {
        if (n <= 1)
            return n;
        return fib(n - 1) + fib(n - 2);
    }

    @Test
    public void shouldLoadPolicyKrakenArtifactsAndMeasureTime() {
        ContextModelTrees.create(CONTEXT_REPOSITORY, TestResources.Info.POLICY_GENESIS.namespace, TargetEnvironment.JAVA);
    }

    @Test
    public void shouldLoadCrmKrakenArtifacts() {
        TestResources.create(TestResources.Info.CRM_GENESIS).getModelTree();
    }
}
