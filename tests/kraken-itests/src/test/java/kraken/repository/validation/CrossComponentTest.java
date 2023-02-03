package kraken.repository.validation;

import kraken.context.model.tree.impl.ContextModelTrees;
import kraken.context.model.tree.impl.ContextRepository;
import kraken.el.TargetEnvironment;
import kraken.test.TestResources;
import org.junit.Test;

/**
 * @author psurinin
 */
public class CrossComponentTest {

    private static final TestResources TEST_RESOURCES = TestResources.create(TestResources.Info.POLICY_GENESIS);
    private static final ContextRepository CONTEXT_REPOSITORY = ContextRepository.from(TEST_RESOURCES.getRuntimeKrakenProject());

    @Test
    public void shouldLoadPolicyKrakenArtifactsAndMeasureTime() {
        ContextModelTrees.create(CONTEXT_REPOSITORY, TestResources.Info.POLICY_GENESIS.namespace, TargetEnvironment.JAVA);
    }

    @Test
    public void shouldLoadCrmKrakenArtifacts() {
        TestResources.create(TestResources.Info.CRM_GENESIS).getModelTree();
    }
}
