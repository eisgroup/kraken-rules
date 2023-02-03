package kraken;

import kraken.el.TargetEnvironment;
import kraken.context.model.tree.ContextModelTree;
import kraken.context.model.tree.repository.StaticContextModelTreeRepository;
import kraken.test.TestResources;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class StaticContextModelTreeImplRepositoryTest {

    @Test
    public void shouldLoadResources() {
        final StaticContextModelTreeRepository repository = StaticContextModelTreeRepository.initialize();
        final ContextModelTree modelTree = repository.get(TestResources.Info.TEST_PRODUCT.namespace, TargetEnvironment.JAVA);

        // configuration in pom.xml is present to generate it in compile phase
        assertThat(modelTree, notNullValue());
        assertThat(modelTree.getMetadata().getNamespace(), is(TestResources.Info.TEST_PRODUCT.namespace));
        assertThat(modelTree.getMetadata().getTargetEnvironment(), is(TargetEnvironment.JAVA));
    }

}
